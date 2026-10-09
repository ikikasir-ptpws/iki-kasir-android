package com.ptpws.ikikasir.core.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.keuangan.domain.model.FinancialReportExportData
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val EXCEL_MIME_TYPE =
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@Singleton
class ExcelExportService @Inject constructor() {

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("id-ID"))
    private val fileNameDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    suspend fun exportTransaksi(
        context: Context,
        transaksiList: List<PenjualanTransaksi>,
        filterLabel: String = "Semua"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val sortedTransactions = transaksiList.sortedByDescending { it.createdAt.seconds }
            val rows = mutableListOf<List<ExcelCell>>()

            // Row 1: Brand Title
            rows += listOf(ExcelCell("IKI KASIR - POINT OF SALE", style = "BrandTitle"))

            // Row 2: Main Report Title
            rows += listOf(ExcelCell("LAPORAN RIWAYAT TRANSAKSI", style = "ReportTitle"))

            // Row 3: Subtitle Metadata Info
            val subtitle = "Periode Filter: $filterLabel   |   Dicetak: ${dateTimeFormat.format(Date())}   |   Total Data: ${transaksiList.size} Transaksi"
            rows += listOf(ExcelCell(subtitle, style = "Subtitle"))

            // Row 4: Empty separator
            rows += listOf(emptyList())

            // Compute KPI Metrics
            var totalPendapatan = 0.0
            var totalDiskon = 0.0
            var totalPpn = 0.0
            sortedTransactions.forEach { tx ->
                val total = tx.total.takeIf { it > 0 }
                    ?: (tx.subtotal - tx.discount + tx.ppnAmount).coerceAtLeast(0.0)
                totalPendapatan += total
                totalDiskon += tx.discount
                totalPpn += tx.ppnAmount
            }

            // Row 5: KPI Card Labels (A5:C5, D5:G5, H5:L5)
            rows += listOf(
                ExcelCell("TOTAL TRANSAKSI", "KpiLabel"), ExcelCell("", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("TOTAL PENDAPATAN", "KpiLabel"), ExcelCell("", "KpiLabel"), ExcelCell("", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("TOTAL DISKON & PPN", "KpiLabel"), ExcelCell("", "KpiLabel"), ExcelCell("", "KpiLabel"), ExcelCell("", "KpiLabel"), ExcelCell("", "KpiLabel")
            )

            // Row 6: KPI Card Values
            rows += listOf(
                ExcelCell("${transaksiList.size} Transaksi", "KpiValue"), ExcelCell("", "KpiValue"), ExcelCell("", "KpiValue"),
                ExcelCell(totalPendapatan.formatRupiah(), "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency"),
                ExcelCell("Diskon: ${totalDiskon.formatRupiah()}   |   PPN: ${totalPpn.formatRupiah()}", "KpiValue"), ExcelCell("", "KpiValue"), ExcelCell("", "KpiValue"), ExcelCell("", "KpiValue"), ExcelCell("", "KpiValue")
            )

            // Row 7: Empty separator
            rows += listOf(emptyList())

            // Row 8: Table Column Headers (1-indexed Row 8)
            rows += listOf(
                ExcelCell("No", "Header"),
                ExcelCell("No. Transaksi", "Header"),
                ExcelCell("Tgl & Jam", "Header"),
                ExcelCell("Kasir", "HeaderLeft"),
                ExcelCell("Pelanggan", "HeaderLeft"),
                ExcelCell("No. Meja", "Header"),
                ExcelCell("Subtotal", "HeaderRight"),
                ExcelCell("Diskon", "HeaderRight"),
                ExcelCell("PPN", "HeaderRight"),
                ExcelCell("Total", "HeaderRight"),
                ExcelCell("Metode Bayar", "Header"),
                ExcelCell("Status", "Header")
            )

            val columnWidths = listOf(8, 26, 20, 20, 22, 12, 22, 18, 18, 22, 18, 16)

            var grandSubtotal = 0.0
            var grandDiskon = 0.0
            var grandPpn = 0.0
            var grandTotal = 0.0

            // Data Rows (Row 9+)
            sortedTransactions.forEachIndexed { index, tx ->
                val total = tx.total.takeIf { it > 0 }
                    ?: (tx.subtotal - tx.discount + tx.ppnAmount).coerceAtLeast(0.0)
                val isSuccess = tx.status.equals("COMPLETED", true) || tx.status.equals("LUNAS", true)
                val cashier = tx.createdBy.ifBlank { "-" }
                val customer = tx.customerName.ifBlank { "-" }
                val table = tx.tableNumber.ifBlank { "-" }
                val displayStatus = if (isSuccess) "Berhasil" else tx.status

                val rowStyle = if (index % 2 == 0) "Data" else "DataAlt"
                val centerStyle = if (index % 2 == 0) "DataCenter" else "DataCenterAlt"
                val numberStyle = if (index % 2 == 0) "Number" else "NumberAlt"

                rows += listOf(
                    ExcelCell((index + 1).toString(), centerStyle),
                    ExcelCell(tx.transactionNumber, centerStyle),
                    ExcelCell(dateTimeFormat.format(tx.createdAt.toDate()), centerStyle),
                    ExcelCell(cashier, rowStyle),
                    ExcelCell(customer, rowStyle),
                    ExcelCell(table, centerStyle),
                    ExcelCell(tx.subtotal.formatRupiah(), numberStyle),
                    ExcelCell(tx.discount.formatRupiah(), numberStyle),
                    ExcelCell(tx.ppnAmount.formatRupiah(), numberStyle),
                    ExcelCell(total.formatRupiah(), numberStyle),
                    ExcelCell(tx.paymentMethod, centerStyle),
                    ExcelCell(displayStatus, if (isSuccess) "SuccessPill" else "DangerPill")
                )
                grandSubtotal += tx.subtotal
                grandDiskon += tx.discount
                grandPpn += tx.ppnAmount
                grandTotal += total
            }

            // Row Footer: Grand Total
            val totalRowNumber = rows.size + 1
            rows += listOf(
                ExcelCell("TOTAL KESELURUHAN", "TotalLabel"), ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"),
                ExcelCell(grandSubtotal.formatRupiah(), "TotalValue"),
                ExcelCell(grandDiskon.formatRupiah(), "TotalValue"),
                ExcelCell(grandPpn.formatRupiah(), "TotalValue"),
                ExcelCell(grandTotal.formatRupiah(), "TotalValue"),
                ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel")
            )

            val mergeRanges = listOf(
                "A5:C5", "D5:G5", "H5:L5",
                "A6:C6", "D6:G6", "H6:L6",
                "A$totalRowNumber:F$totalRowNumber",
                "K$totalRowNumber:L$totalRowNumber"
            )

            val autoFilterEndRow = totalRowNumber - 1
            val autoFilterRange = "A8:L$autoFilterEndRow"

            saveAndShare(
                context,
                createSpreadsheetXlsx(
                    listOf(
                        ExcelSheet(
                            name = "Riwayat Transaksi",
                            rows = rows,
                            columnWidths = columnWidths,
                            headerRowIndex = 7, // 0-indexed row 8
                            mergeRanges = mergeRanges,
                            autoFilterRange = autoFilterRange
                        )
                    )
                ),
                "Transaksi_${fileNameDateFormat.format(Date())}.xlsx"
            )
        }
    }

    suspend fun exportLaporanKeuangan(
        context: Context,
        reportData: FinancialReportExportData,
        transaksiList: List<PenjualanTransaksi>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val sortedTransactions = transaksiList.sortedByDescending { it.createdAt.seconds }
            val sheets = mutableListOf<ExcelSheet>()

            // ════════════════════════════════════════════════════════════════
            // ── SHEET 1: RINGKASAN FINANSIAL & P&L STATEMENT ──
            // ════════════════════════════════════════════════════════════════
            val summaryRows = mutableListOf<List<ExcelCell>>()
            summaryRows += listOf(ExcelCell("IKI KASIR - SMART POINT OF SALE", "BrandTitle"))
            summaryRows += listOf(ExcelCell("LAPORAN KEUANGAN & LABA RUGI (P&L)", "ReportTitle"))
            summaryRows += listOf(
                ExcelCell(
                    "Periode: ${reportData.filterLabel}   |   Dicetak: ${dateTimeFormat.format(Date())}   |   Status: 100% Data Riil Transaksi",
                    "Subtitle"
                )
            )
            summaryRows += listOf(emptyList())

            // Section 1: KPI Finansial Utama
            summaryRows += listOf(ExcelCell("RINGKASAN METRIK FINANSIAL UTAMA", "BrandTitle"))
            summaryRows += listOf(
                ExcelCell("TOTAL OMZET (REVENUE)", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("LABA KOTOR (GROSS PROFIT)", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("LABA BERSIH (NET PROFIT)", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("BIAYA MODAL (HPP)", "KpiLabel"), ExcelCell("", "KpiLabel")
            )
            summaryRows += listOf(
                ExcelCell(reportData.totalOmzet.formatRupiah(), "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency"),
                ExcelCell(reportData.totalLabaKotor.formatRupiah(), "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency"),
                ExcelCell(reportData.totalLabaBersih.formatRupiah(), "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency"),
                ExcelCell(reportData.totalHpp.formatRupiah(), "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency")
            )
            summaryRows += listOf(
                ExcelCell("VOLUME PENJUALAN", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("TOTAL PESANAN", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("TOTAL PPN (PAJAK)", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("TOTAL DISKON & PROMO", "KpiLabel"), ExcelCell("", "KpiLabel")
            )
            summaryRows += listOf(
                ExcelCell("${reportData.totalProdukTerjual} Produk Terjual", "KpiValue"), ExcelCell("", "KpiValue"),
                ExcelCell("${reportData.totalTransaksiCount} Order Selesai", "KpiValue"), ExcelCell("", "KpiValue"),
                ExcelCell(reportData.totalPajak.formatRupiah(), "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency"),
                ExcelCell(reportData.totalDiskon.formatRupiah(), "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency")
            )
            summaryRows += listOf(emptyList())

            // Section 2: Struktur Laba Rugi (P&L)
            summaryRows += listOf(ExcelCell("STRUKTUR LABA & BEBAN (PROFIT & LOSS STATEMENT)", "BrandTitle"))
            summaryRows += listOf(
                ExcelCell("No", "Header"),
                ExcelCell("Komponen Arus Finansial", "HeaderLeft"), ExcelCell("", "HeaderLeft"),
                ExcelCell("Rasio Terhadap Omzet", "Header"),
                ExcelCell("Nominal (IDR)", "HeaderRight"), ExcelCell("", "HeaderRight"),
                ExcelCell("Keterangan Operasional", "HeaderLeft"), ExcelCell("", "HeaderLeft")
            )

            val pnlItems = listOf(
                Triple("Penjualan Kotor (Gross Sales)", reportData.totalOmzet + reportData.totalDiskon, "Basis nilai transaksi kotor sebelum diskon"),
                Triple("Potongan Diskon & Promo", -reportData.totalDiskon, "Total potongan harga dan voucher promo yang diberikan"),
                Triple("Penjualan Bersih (Net Sales / Omzet)", reportData.totalOmzet, "Total pendapatan riil yang masuk ke kasir"),
                Triple("Harga Pokok Penjualan (HPP / Biaya Modal)", -reportData.totalHpp, "Biaya modal produk yang terjual"),
                Triple("SUBTOTAL LABA KOTOR (GROSS PROFIT)", reportData.totalLabaKotor, "Laba kotor setelah dikurangi biaya modal HPP"),
                Triple("Pajak Pertambahan Nilai (PPN)", reportData.totalPajak, "Pajak kasir yang berhasil dihimpun"),
                Triple("TOTAL LABA BERSIH (NET PROFIT)", reportData.totalLabaBersih, "Keuntungan bersih akhir toko pada periode ini")
            )

            pnlItems.forEachIndexed { idx, (itemLabel, itemVal, itemDesc) ->
                val isHighlight = idx == 4 || idx == 6
                val usedRowStyle = if (isHighlight) "TotalLabel" else if (idx % 2 == 0) "Data" else "DataAlt"
                val usedCenterStyle = if (isHighlight) "TotalLabel" else if (idx % 2 == 0) "DataCenter" else "DataCenterAlt"
                val usedNumStyle = if (isHighlight) "TotalValue" else if (idx % 2 == 0) "Number" else "NumberAlt"

                val ratioText = when (idx) {
                    0, 2 -> "100.0%"
                    4 -> "Margin ${String.format(Locale.US, "%.1f", reportData.marginKotorPersen)}%"
                    6 -> "Margin ${String.format(Locale.US, "%.1f", reportData.profitMarginPersen)}%"
                    else -> if (reportData.totalOmzet > 0) {
                        "${String.format(Locale.US, "%.1f", (kotlin.math.abs(itemVal) / reportData.totalOmzet) * 100)}%"
                    } else "0.0%"
                }

                summaryRows += listOf(
                    ExcelCell((idx + 1).toString(), usedCenterStyle),
                    ExcelCell(itemLabel, usedRowStyle), ExcelCell("", usedRowStyle),
                    ExcelCell(ratioText, usedCenterStyle),
                    ExcelCell(itemVal.formatRupiah(), usedNumStyle), ExcelCell("", usedNumStyle),
                    ExcelCell(itemDesc, usedRowStyle), ExcelCell("", usedRowStyle)
                )
            }
            summaryRows += listOf(emptyList())

            // Section 3: Kanal Pembayaran
            summaryRows += listOf(ExcelCell("DISTRIBUSI KANAL PEMBAYARAN KASIR", "BrandTitle"))
            val payHeaderRow = summaryRows.size + 1
            summaryRows += listOf(
                ExcelCell("No", "Header"),
                ExcelCell("Metode Pembayaran", "HeaderLeft"), ExcelCell("", "HeaderLeft"),
                ExcelCell("Jumlah Transaksi", "Header"),
                ExcelCell("Total Diterima (IDR)", "HeaderRight"), ExcelCell("", "HeaderRight"),
                ExcelCell("Kontribusi Pembayaran (%)", "Header"), ExcelCell("", "Header")
            )

            var payTotalNominal = 0.0
            var payTotalCount = 0
            if (reportData.metodePembayaranList.isEmpty()) {
                summaryRows += listOf(
                    ExcelCell("1", "DataCenter"),
                    ExcelCell("Belum ada transaksi pembayaran", "Data"), ExcelCell("", "Data"),
                    ExcelCell("0 Transaksi", "DataCenter"),
                    ExcelCell(0.0.formatRupiah(), "Number"), ExcelCell("", "Number"),
                    ExcelCell("0.0%", "DataCenter"), ExcelCell("", "DataCenter")
                )
            } else {
                reportData.metodePembayaranList.forEachIndexed { idx, pm ->
                    val rowStyle = if (idx % 2 == 0) "Data" else "DataAlt"
                    val centerStyle = if (idx % 2 == 0) "DataCenter" else "DataCenterAlt"
                    val numStyle = if (idx % 2 == 0) "Number" else "NumberAlt"

                    summaryRows += listOf(
                        ExcelCell((idx + 1).toString(), centerStyle),
                        ExcelCell(pm.metode, rowStyle), ExcelCell("", rowStyle),
                        ExcelCell("${pm.jumlahTransaksi} Transaksi", centerStyle),
                        ExcelCell(pm.totalNominal.formatRupiah(), numStyle), ExcelCell("", numStyle),
                        ExcelCell("${String.format(Locale.US, "%.1f", pm.persentase)}%", centerStyle), ExcelCell("", centerStyle)
                    )
                    payTotalNominal += pm.totalNominal
                    payTotalCount += pm.jumlahTransaksi
                }
            }

            val payTotalRowIdx = summaryRows.size + 1
            summaryRows += listOf(
                ExcelCell("TOTAL KANAL PEMBAYARAN", "TotalLabel"),
                ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"),
                ExcelCell("$payTotalCount Transaksi", "TotalLabel"),
                ExcelCell(payTotalNominal.formatRupiah(), "TotalValue"), ExcelCell("", "TotalValue"),
                ExcelCell("100.0%", "TotalLabel"), ExcelCell("", "TotalLabel")
            )

            val summaryMergeRanges = mutableListOf<String>()
            summaryMergeRanges += listOf(
                "A6:B6", "C6:D6", "E6:F6", "G6:H6",
                "A7:B7", "C7:D7", "E7:F7", "G7:H7",
                "A8:B8", "C8:D8", "E8:F8", "G8:H8",
                "A9:B9", "C9:D9", "E9:F9", "G9:H9",
                "B12:C12", "E12:F12", "G12:H12",
                "B13:C13", "E13:F13", "G13:H13",
                "B14:C14", "E14:F14", "G14:H14",
                "B15:C15", "E15:F15", "G15:H15",
                "B16:C16", "E16:F16", "G16:H16",
                "B17:C17", "E17:F17", "G17:H17",
                "B18:C18", "E18:F18", "G18:H18",
                "B19:C19", "E19:F19", "G19:H19",
                "B$payHeaderRow:C$payHeaderRow", "E$payHeaderRow:F$payHeaderRow", "G$payHeaderRow:H$payHeaderRow"
            )
            val pCount = maxOf(reportData.metodePembayaranList.size, 1)
            for (i in 1..pCount) {
                val r = payHeaderRow + i
                summaryMergeRanges += listOf("B$r:C$r", "E$r:F$r", "G$r:H$r")
            }
            summaryMergeRanges += listOf(
                "A$payTotalRowIdx:C$payTotalRowIdx", "E$payTotalRowIdx:F$payTotalRowIdx", "G$payTotalRowIdx:H$payTotalRowIdx"
            )

            sheets += ExcelSheet(
                name = "Ringkasan Finansial",
                rows = summaryRows,
                columnWidths = listOf(8, 26, 20, 22, 22, 14, 20, 26),
                headerRowIndex = 11,
                mergeRanges = summaryMergeRanges
            )

            // ════════════════════════════════════════════════════════════════
            // ── SHEET 2: TREN PENJUALAN HARIAN ──
            // ════════════════════════════════════════════════════════════════
            val dailyRows = mutableListOf<List<ExcelCell>>()
            dailyRows += listOf(ExcelCell("IKI KASIR - SMART POINT OF SALE", "BrandTitle"))
            dailyRows += listOf(ExcelCell("TREN PENJUALAN HARIAN", "ReportTitle"))
            dailyRows += listOf(ExcelCell("Periode: ${reportData.filterLabel}   |   Dicetak: ${dateTimeFormat.format(Date())}", "Subtitle"))
            dailyRows += listOf(emptyList())

            dailyRows += listOf(
                ExcelCell("No", "Header"),
                ExcelCell("Hari", "Header"),
                ExcelCell("Tanggal", "Header"),
                ExcelCell("Order Selesai", "Header"),
                ExcelCell("Omzet Penjualan (IDR)", "HeaderRight"),
                ExcelCell("Biaya Modal (HPP)", "HeaderRight"),
                ExcelCell("Diskon Promo (IDR)", "HeaderRight"),
                ExcelCell("Estimasi Laba Bersih", "HeaderRight")
            )

            var dailyTotalOmzet = 0.0
            var dailyTotalHpp = 0.0
            var dailyTotalDiskon = 0.0
            var dailyTotalLaba = 0.0
            var dailyTotalTx = 0

            reportData.dailySales.forEachIndexed { idx, d ->
                val rowStyle = if (idx % 2 == 0) "Data" else "DataAlt"
                val centerStyle = if (idx % 2 == 0) "DataCenter" else "DataCenterAlt"
                val numStyle = if (idx % 2 == 0) "Number" else "NumberAlt"

                dailyRows += listOf(
                    ExcelCell((idx + 1).toString(), centerStyle),
                    ExcelCell(d.dayLabel, centerStyle),
                    ExcelCell(d.dateLabel, centerStyle),
                    ExcelCell("${d.jumlahTransaksi} Order", centerStyle),
                    ExcelCell(d.totalOmzet.formatRupiah(), numStyle),
                    ExcelCell(d.totalHpp.formatRupiah(), numStyle),
                    ExcelCell(d.totalDiskon.formatRupiah(), numStyle),
                    ExcelCell(d.totalLaba.formatRupiah(), numStyle)
                )
                dailyTotalOmzet += d.totalOmzet
                dailyTotalHpp += d.totalHpp
                dailyTotalDiskon += d.totalDiskon
                dailyTotalLaba += d.totalLaba
                dailyTotalTx += d.jumlahTransaksi
            }

            val dailyTotalRow = dailyRows.size + 1
            dailyRows += listOf(
                ExcelCell("TOTAL TREN HARIAN", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("$dailyTotalTx Order", "TotalLabel"),
                ExcelCell(dailyTotalOmzet.formatRupiah(), "TotalValue"),
                ExcelCell(dailyTotalHpp.formatRupiah(), "TotalValue"),
                ExcelCell(dailyTotalDiskon.formatRupiah(), "TotalValue"),
                ExcelCell(dailyTotalLaba.formatRupiah(), "TotalValue")
            )

            sheets += ExcelSheet(
                name = "Tren Penjualan Harian",
                rows = dailyRows,
                columnWidths = listOf(8, 14, 18, 18, 24, 22, 20, 24),
                headerRowIndex = 4,
                mergeRanges = listOf("A$dailyTotalRow:C$dailyTotalRow"),
                autoFilterRange = "A5:H${dailyTotalRow - 1}"
            )

            // ════════════════════════════════════════════════════════════════
            // ── SHEET 3: TOP PRODUK TERLARIS ──
            // ════════════════════════════════════════════════════════════════
            val topRows = mutableListOf<List<ExcelCell>>()
            topRows += listOf(ExcelCell("IKI KASIR - SMART POINT OF SALE", "BrandTitle"))
            topRows += listOf(ExcelCell("LEADERBOARD 5 PRODUK TERLARIS", "ReportTitle"))
            topRows += listOf(ExcelCell("Periode: ${reportData.filterLabel}   |   Dicetak: ${dateTimeFormat.format(Date())}", "Subtitle"))
            topRows += listOf(emptyList())

            topRows += listOf(
                ExcelCell("Peringkat", "Header"),
                ExcelCell("ID Produk", "Header"),
                ExcelCell("Nama Produk", "HeaderLeft"),
                ExcelCell("Unit Terjual (Qty)", "Header"),
                ExcelCell("Total Penjualan (IDR)", "HeaderRight"),
                ExcelCell("Kontribusi Omzet (%)", "Header")
            )

            var topTotalQty = 0
            var topTotalOmzet = 0.0

            if (reportData.topProdukList.isEmpty()) {
                topRows += listOf(
                    ExcelCell("1", "DataCenter"),
                    ExcelCell("-", "DataCenter"),
                    ExcelCell("Belum ada produk yang terjual", "Data"),
                    ExcelCell("0 Unit", "DataCenter"),
                    ExcelCell(0.0.formatRupiah(), "Number"),
                    ExcelCell("0.0%", "DataCenter")
                )
            } else {
                reportData.topProdukList.forEachIndexed { idx, p ->
                    val rowStyle = if (idx % 2 == 0) "Data" else "DataAlt"
                    val centerStyle = if (idx % 2 == 0) "DataCenter" else "DataCenterAlt"
                    val numStyle = if (idx % 2 == 0) "Number" else "NumberAlt"

                    topRows += listOf(
                        ExcelCell("#${p.rank}", centerStyle),
                        ExcelCell(p.productId.ifBlank { "-" }, centerStyle),
                        ExcelCell(p.namaProduk, rowStyle),
                        ExcelCell("${p.unitTerjual} Unit", centerStyle),
                        ExcelCell(p.totalOmzet.formatRupiah(), numStyle),
                        ExcelCell("${String.format(Locale.US, "%.1f", p.kontribusiPersen)}%", centerStyle)
                    )
                    topTotalQty += p.unitTerjual
                    topTotalOmzet += p.totalOmzet
                }
            }

            val topTotalRow = topRows.size + 1
            topRows += listOf(
                ExcelCell("TOTAL TOP PRODUK", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("$topTotalQty Unit", "TotalLabel"),
                ExcelCell(topTotalOmzet.formatRupiah(), "TotalValue"),
                ExcelCell("100.0%", "TotalLabel")
            )

            sheets += ExcelSheet(
                name = "Produk Terlaris",
                rows = topRows,
                columnWidths = listOf(12, 18, 34, 20, 24, 22),
                headerRowIndex = 4,
                mergeRanges = listOf("A$topTotalRow:C$topTotalRow"),
                autoFilterRange = "A5:F${topTotalRow - 1}"
            )

            // ════════════════════════════════════════════════════════════════
            // ── SHEET 4: RINCIAN TRANSAKSI PENJUALAN ──
            // ════════════════════════════════════════════════════════════════
            val txRows = mutableListOf<List<ExcelCell>>()
            txRows += listOf(ExcelCell("IKI KASIR - SMART POINT OF SALE", "BrandTitle"))
            txRows += listOf(ExcelCell("RINCIAN TRANSAKSI PENJUALAN KASIR", "ReportTitle"))
            txRows += listOf(ExcelCell("Periode: ${reportData.filterLabel}   |   Dicetak: ${dateTimeFormat.format(Date())}   |   Total Data: ${sortedTransactions.size} Transaksi", "Subtitle"))
            txRows += listOf(emptyList())

            txRows += listOf(
                ExcelCell("No", "Header"),
                ExcelCell("No. Transaksi", "Header"),
                ExcelCell("Tgl & Jam", "Header"),
                ExcelCell("Kasir", "HeaderLeft"),
                ExcelCell("Pelanggan", "HeaderLeft"),
                ExcelCell("No. Meja", "Header"),
                ExcelCell("Subtotal", "HeaderRight"),
                ExcelCell("Diskon", "HeaderRight"),
                ExcelCell("PPN", "HeaderRight"),
                ExcelCell("Total", "HeaderRight"),
                ExcelCell("Metode Bayar", "Header"),
                ExcelCell("Status", "Header")
            )

            var grandSubtotal = 0.0
            var grandDiskon = 0.0
            var grandPpn = 0.0
            var grandTotal = 0.0

            sortedTransactions.forEachIndexed { index, tx ->
                val total = tx.total.takeIf { it > 0 }
                    ?: (tx.subtotal - tx.discount + tx.ppnAmount).coerceAtLeast(0.0)
                val isSuccess = tx.status.equals("COMPLETED", true) || tx.status.equals("LUNAS", true)
                val cashier = tx.createdBy.ifBlank { "-" }
                val customer = tx.customerName.ifBlank { "-" }
                val table = tx.tableNumber.ifBlank { "-" }
                val displayStatus = if (isSuccess) "Berhasil" else tx.status

                val rowStyle = if (index % 2 == 0) "Data" else "DataAlt"
                val centerStyle = if (index % 2 == 0) "DataCenter" else "DataCenterAlt"
                val numberStyle = if (index % 2 == 0) "Number" else "NumberAlt"

                txRows += listOf(
                    ExcelCell((index + 1).toString(), centerStyle),
                    ExcelCell(tx.transactionNumber, centerStyle),
                    ExcelCell(dateTimeFormat.format(tx.createdAt.toDate()), centerStyle),
                    ExcelCell(cashier, rowStyle),
                    ExcelCell(customer, rowStyle),
                    ExcelCell(table, centerStyle),
                    ExcelCell(tx.subtotal.formatRupiah(), numberStyle),
                    ExcelCell(tx.discount.formatRupiah(), numberStyle),
                    ExcelCell(tx.ppnAmount.formatRupiah(), numberStyle),
                    ExcelCell(total.formatRupiah(), numberStyle),
                    ExcelCell(tx.paymentMethod, centerStyle),
                    ExcelCell(displayStatus, if (isSuccess) "SuccessPill" else "DangerPill")
                )
                grandSubtotal += tx.subtotal
                grandDiskon += tx.discount
                grandPpn += tx.ppnAmount
                grandTotal += total
            }

            val txTotalRow = txRows.size + 1
            txRows += listOf(
                ExcelCell("TOTAL KESELURUHAN", "TotalLabel"), ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"),
                ExcelCell(grandSubtotal.formatRupiah(), "TotalValue"),
                ExcelCell(grandDiskon.formatRupiah(), "TotalValue"),
                ExcelCell(grandPpn.formatRupiah(), "TotalValue"),
                ExcelCell(grandTotal.formatRupiah(), "TotalValue"),
                ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel")
            )

            sheets += ExcelSheet(
                name = "Rincian Transaksi",
                rows = txRows,
                columnWidths = listOf(8, 26, 20, 20, 22, 12, 22, 18, 18, 22, 18, 16),
                headerRowIndex = 4,
                mergeRanges = listOf("A$txTotalRow:F$txTotalRow", "K$txTotalRow:L$txTotalRow"),
                autoFilterRange = "A5:L${txTotalRow - 1}"
            )

            saveAndShare(
                context,
                createSpreadsheetXlsx(sheets),
                "Laporan Keuangan_${fileNameDateFormat.format(Date())}.xlsx"
            )
        }
    }

    suspend fun exportAntrean(
        context: Context,
        historyList: List<QueueHistory>,
        transaksiMap: Map<String, PenjualanTransaksi>,
        filterLabel: String = "Semua"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val sortedHistory = historyList.sortedByDescending { it.completedAt.seconds }
            val rows = mutableListOf<List<ExcelCell>>()

            // Row 1: Brand Title
            rows += listOf(ExcelCell("IKI KASIR - KITCHEN & QUEUE", style = "BrandTitle"))

            // Row 2: Main Report Title
            rows += listOf(ExcelCell("LAPORAN RIWAYAT ANTREAN", style = "ReportTitle"))

            // Row 3: Subtitle Metadata Info
            val subtitle = "Periode Filter: $filterLabel   |   Dicetak: ${dateTimeFormat.format(Date())}   |   Total Data: ${historyList.size} Antrean"
            rows += listOf(ExcelCell(subtitle, style = "Subtitle"))

            // Row 4: Empty separator
            rows += listOf(emptyList())

            // Compute KPI Metrics
            var totalSelesai = 0
            var totalBatal = 0
            var grandTotalSelesai = 0.0

            sortedHistory.forEach { history ->
                val isDone = history.status.equals("DONE", true) || history.status.equals("SELESAI", true)
                if (isDone) {
                    totalSelesai++
                    val transaction = transaksiMap[history.transactionId]
                    val total = transaction?.let {
                        it.total.takeIf { v -> v > 0 } ?: (it.subtotal - it.discount + it.ppnAmount).coerceAtLeast(0.0)
                    } ?: 0.0
                    grandTotalSelesai += total
                } else {
                    totalBatal++
                }
            }

            // Row 5: KPI Card Labels (A5:B5, C5:D5, E5:F5, G5:I5)
            rows += listOf(
                ExcelCell("TOTAL ANTREAN", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("SELESAI", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("DIBATALKAN", "KpiLabel"), ExcelCell("", "KpiLabel"),
                ExcelCell("TOTAL OMSET SELESAI", "KpiLabel"), ExcelCell("", "KpiLabel"), ExcelCell("", "KpiLabel")
            )

            // Row 6: KPI Card Values
            rows += listOf(
                ExcelCell("${historyList.size} Antrean", "KpiValue"), ExcelCell("", "KpiValue"),
                ExcelCell("$totalSelesai Antrean", "KpiValueSuccess"), ExcelCell("", "KpiValueSuccess"),
                ExcelCell("$totalBatal Antrean", "KpiValueDanger"), ExcelCell("", "KpiValueDanger"),
                ExcelCell(grandTotalSelesai.formatRupiah(), "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency"), ExcelCell("", "KpiValueCurrency")
            )

            // Row 7: Empty separator
            rows += listOf(emptyList())

            // Row 8: Table Column Headers (1-indexed Row 8)
            rows += listOf(
                ExcelCell("No", "Header"),
                ExcelCell("ID Transaksi", "Header"),
                ExcelCell("No. Antrean", "Header"),
                ExcelCell("Pelanggan", "HeaderLeft"),
                ExcelCell("No. Meja", "Header"),
                ExcelCell("Status", "Header"),
                ExcelCell("Produk (Qty)", "HeaderLeft"),
                ExcelCell("Total", "HeaderRight"),
                ExcelCell("Waktu Selesai", "Header")
            )

            val columnWidths = listOf(8, 26, 15, 22, 12, 16, 52, 22, 20)

            // Data Rows (Row 9+)
            sortedHistory.forEachIndexed { index, history ->
                val transaction = transaksiMap[history.transactionId]
                val isDone = history.status.equals("DONE", true) || history.status.equals("SELESAI", true)
                val displayStatus = if (isDone) "Selesai" else "Dibatalkan"
                val customer = history.customerName.ifBlank { "-" }
                val table = history.tableNumber.ifBlank { "-" }
                val products = transaction?.items?.joinToString("\n") { "${it.name} (${it.quantity})" } ?: "-"
                val total = transaction?.let {
                    it.total.takeIf { value -> value > 0 }
                        ?: (it.subtotal - it.discount + it.ppnAmount).coerceAtLeast(0.0)
                } ?: 0.0

                val rowStyle = if (index % 2 == 0) "Data" else "DataAlt"
                val centerStyle = if (index % 2 == 0) "DataCenter" else "DataCenterAlt"
                val numberStyle = if (index % 2 == 0) "Number" else "NumberAlt"

                rows += listOf(
                    ExcelCell((index + 1).toString(), centerStyle),
                    ExcelCell(history.transactionId, centerStyle),
                    ExcelCell(history.queueSequence.toString(), centerStyle),
                    ExcelCell(customer, rowStyle),
                    ExcelCell(table, centerStyle),
                    ExcelCell(displayStatus, if (isDone) "SuccessPill" else "DangerPill"),
                    ExcelCell(products, rowStyle),
                    ExcelCell(total.formatRupiah(), numberStyle),
                    ExcelCell(dateTimeFormat.format(history.completedAt.toDate()), centerStyle)
                )
            }

            // Row Footer: Grand Total
            val totalRowNumber = rows.size + 1
            rows += listOf(
                ExcelCell("TOTAL OMSET ANTREAN SELESAI", "TotalLabel"), ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"), ExcelCell("", "TotalLabel"),
                ExcelCell(grandTotalSelesai.formatRupiah(), "TotalValue"),
                ExcelCell("", "TotalLabel")
            )

            val mergeRanges = listOf(
                "A5:B5", "C5:D5", "E5:F5", "G5:I5",
                "A6:B6", "C6:D6", "E6:F6", "G6:I6",
                "A$totalRowNumber:G$totalRowNumber"
            )

            val autoFilterEndRow = totalRowNumber - 1
            val autoFilterRange = "A8:I$autoFilterEndRow"

            saveAndShare(
                context,
                createSpreadsheetXlsx(
                    listOf(
                        ExcelSheet(
                            name = "Riwayat Antrean",
                            rows = rows,
                            columnWidths = columnWidths,
                            headerRowIndex = 7, // 0-indexed row 8
                            mergeRanges = mergeRanges,
                            autoFilterRange = autoFilterRange
                        )
                    )
                ),
                "AntreanHistory_${fileNameDateFormat.format(Date())}.xlsx"
            )
        }
    }

    private fun createSpreadsheetXlsx(sheets: List<ExcelSheet>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.addTextEntry(
                "[Content_Types].xml",
                buildString {
                    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
                    append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
                    append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
                    append("""<Default Extension="xml" ContentType="application/xml"/>""")
                    append("""<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
                    append("""<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>""")
                    sheets.indices.forEach { index ->
                        append("""<Override PartName="/xl/worksheets/sheet${index + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>""")
                    }
                    append("</Types>")
                }
            )
            zip.addTextEntry(
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>"""
            )
            zip.addTextEntry(
                "xl/workbook.xml",
                buildString {
                    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
                    append("""<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
                    append("<sheets>")
                    sheets.forEachIndexed { index, sheet ->
                        append("""<sheet name="${sheet.name.xmlEscape()}" sheetId="${index + 1}" r:id="rId${index + 1}"/>""")
                    }
                    append("</sheets></workbook>")
                }
            )
            zip.addTextEntry(
                "xl/_rels/workbook.xml.rels",
                buildString {
                    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
                    append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
                    sheets.indices.forEach { index ->
                        append("""<Relationship Id="rId${index + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet${index + 1}.xml"/>""")
                    }
                    append("""<Relationship Id="rId${sheets.size + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>""")
                    append("</Relationships>")
                }
            )
            zip.addTextEntry("xl/styles.xml", createStylesXml())
            sheets.forEachIndexed { index, sheet ->
                val columnCount = maxOf(sheet.rows.maxOfOrNull { it.size } ?: 1, sheet.columnWidths.size)
                zip.addTextEntry(
                    "xl/worksheets/sheet${index + 1}.xml",
                    createWorksheetXml(sheet, columnName(columnCount))
                )
            }
        }
        return output.toByteArray()
    }

    private fun createWorksheetXml(
        sheet: ExcelSheet,
        lastColumn: String
    ): String = buildString {
        val rows = sheet.rows
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        append("""<sheetPr><pageSetUpPr fitToPage="1"/></sheetPr>""")
        append("""<dimension ref="A1:$lastColumn${rows.size}"/>""")
        append("""<sheetViews><sheetView showGridLines="1" zoomScale="90" workbookViewId="0"><pane ySplit="${sheet.headerRowIndex + 1}" topLeftCell="A${sheet.headerRowIndex + 2}" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews>""")
        append("<cols>")
        sheet.columnWidths.forEachIndexed { index, width ->
            append("""<col min="${index + 1}" max="${index + 1}" width="$width" customWidth="1"/>""")
        }
        append("</cols><sheetData>")
        rows.forEachIndexed { rowIndex, row ->
            val rowNumber = rowIndex + 1
            val isHeader = rowIndex == sheet.headerRowIndex
            val isTotal = row.firstOrNull()?.value?.startsWith("TOTAL") == true
            val rowHeight = when {
                rowIndex == 0 -> """ ht="22" customHeight="1""""
                rowIndex == 1 -> """ ht="28" customHeight="1""""
                rowIndex == 2 -> """ ht="18" customHeight="1""""
                rowIndex == 3 -> """ ht="12" customHeight="1""""
                rowIndex == 4 -> """ ht="18" customHeight="1""""
                rowIndex == 5 -> """ ht="28" customHeight="1""""
                rowIndex == 6 -> """ ht="14" customHeight="1""""
                isHeader -> """ ht="32" customHeight="1""""
                isTotal -> """ ht="32" customHeight="1""""
                row.isNotEmpty() -> """ ht="${row.requiredHeight(sheet.columnWidths)}" customHeight="1""""
                else -> ""
            }
            append("""<row r="$rowNumber"$rowHeight>""")
            row.forEachIndexed { columnIndex, cell ->
                val cellRef = "${columnName(columnIndex + 1)}${rowIndex + 1}"
                val styleIndex = cell.style.toStyleIndex()
                if (cell.type == "Number") {
                    val numericValue = cell.value.toDoubleOrNull() ?: 0.0
                    val numStr = if (numericValue % 1.0 == 0.0) numericValue.toLong().toString() else numericValue.toString()
                    append("""<c r="$cellRef" s="$styleIndex"><v>$numStr</v></c>""")
                } else {
                    append("""<c r="$cellRef" s="$styleIndex" t="inlineStr"><is><t xml:space="preserve">${cell.value.xmlEscape()}</t></is></c>""")
                }
            }
            append("</row>")
        }
        append("</sheetData>")
        sheet.autoFilterRange?.let { append("""<autoFilter ref="$it"/>""") }
        if (sheet.mergeRanges.isNotEmpty()) {
            append("""<mergeCells count="${sheet.mergeRanges.size}">""")
            sheet.mergeRanges.forEach { range -> append("""<mergeCell ref="$range"/>""") }
            append("</mergeCells>")
        }
        append("""<printOptions horizontalCentered="1"/>""")
        append("""<pageMargins left="0.25" right="0.25" top="0.5" bottom="0.5" header="0.2" footer="0.2"/>""")
        append("""<pageSetup orientation="landscape" fitToWidth="1" fitToHeight="0" paperSize="9"/>""")
        append("</worksheet>")
    }

    private fun List<ExcelCell>.requiredHeight(columnWidths: List<Int>): Int {
        val visualLines = mapIndexed { index, cell ->
            val width = (columnWidths.getOrNull(index) ?: 12).minus(2).coerceAtLeast(1)
            cell.value.split('\n').sumOf { line ->
                ((line.length + width - 1) / width).coerceAtLeast(1)
            }
        }.maxOrNull() ?: 1
        return (visualLines * 16 + 10).coerceIn(24, 409)
    }

    private fun createStylesXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
        <numFmts count="2">
        <numFmt numFmtId="164" formatCode="#,##0"/>
        <numFmt numFmtId="165" formatCode="&quot;Rp &quot;#,##0;-&quot;Rp &quot;#,##0;&quot;Rp &quot;0"/>
        </numFmts>
        <fonts count="11">
        <font><sz val="10"/><color rgb="FF1E293B"/><name val="Calibri"/></font>
        <font><b/><sz val="10"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
        <font><b/><sz val="10"/><color rgb="FF0F172A"/><name val="Calibri"/></font>
        <font><b/><sz val="16"/><color rgb="FF0F172A"/><name val="Calibri"/></font>
        <font><b/><sz val="11"/><color rgb="FF4F46E5"/><name val="Calibri"/></font>
        <font><sz val="9.5"/><color rgb="FF64748B"/><name val="Calibri"/></font>
        <font><b/><sz val="9.5"/><color rgb="FF065F46"/><name val="Calibri"/></font>
        <font><b/><sz val="9.5"/><color rgb="FF991B1B"/><name val="Calibri"/></font>
        <font><b/><sz val="8.5"/><color rgb="FF64748B"/><name val="Calibri"/></font>
        <font><b/><sz val="13"/><color rgb="FF0F172A"/><name val="Calibri"/></font>
        <font><b/><sz val="10.5"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
        </fonts>
        <fills count="10">
        <fill><patternFill patternType="none"/></fill>
        <fill><patternFill patternType="gray125"/></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FF1E293B"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFFFFFFF"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFF8FAFC"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFD1FAE5"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFFEE2E2"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FF0F172A"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFF1F5F9"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFEEF2FF"/><bgColor indexed="64"/></patternFill></fill>
        </fills>
        <borders count="5">
        <border><left/><right/><top/><bottom/><diagonal/></border>
        <border>
        <left style="thin"><color rgb="FFE2E8F0"/></left>
        <right style="thin"><color rgb="FFE2E8F0"/></right>
        <top style="thin"><color rgb="FFE2E8F0"/></top>
        <bottom style="thin"><color rgb="FFE2E8F0"/></bottom>
        <diagonal/>
        </border>
        <border>
        <left style="thin"><color rgb="FF334155"/></left>
        <right style="thin"><color rgb="FF334155"/></right>
        <top style="thin"><color rgb="FF334155"/></top>
        <bottom style="medium"><color rgb="FF0F172A"/></bottom>
        <diagonal/>
        </border>
        <border>
        <left style="none"/><right style="none"/>
        <top style="thin"><color rgb="FF475569"/></top>
        <bottom style="double"><color rgb="FF0F172A"/></bottom>
        <diagonal/>
        </border>
        <border>
        <left style="thin"><color rgb="FFCBD5E1"/></left>
        <right style="thin"><color rgb="FFCBD5E1"/></right>
        <top style="thin"><color rgb="FFCBD5E1"/></top>
        <bottom style="thin"><color rgb="FFCBD5E1"/></bottom>
        <diagonal/>
        </border>
        </borders>
        <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
        <cellXfs count="24">
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
        <xf numFmtId="0" fontId="4" fillId="0" borderId="0" xfId="0" applyFont="1" applyAlignment="1"><alignment vertical="center"/></xf>
        <xf numFmtId="0" fontId="3" fillId="0" borderId="0" xfId="0" applyFont="1" applyAlignment="1"><alignment vertical="center"/></xf>
        <xf numFmtId="0" fontId="5" fillId="0" borderId="0" xfId="0" applyFont="1" applyAlignment="1"><alignment vertical="center"/></xf>
        <xf numFmtId="0" fontId="8" fillId="8" borderId="4" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
        <xf numFmtId="0" fontId="9" fillId="8" borderId="4" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
        <xf numFmtId="165" fontId="9" fillId="8" borderId="4" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
        <xf numFmtId="0" fontId="6" fillId="5" borderId="4" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
        <xf numFmtId="0" fontId="7" fillId="6" borderId="4" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
        <xf numFmtId="0" fontId="1" fillId="2" borderId="2" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
        <xf numFmtId="0" fontId="1" fillId="2" borderId="2" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="left" vertical="center" wrapText="1" indent="1"/></xf>
        <xf numFmtId="0" fontId="1" fillId="2" borderId="2" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center" wrapText="1" indent="1"/></xf>
        <xf numFmtId="0" fontId="0" fillId="3" borderId="1" xfId="0" applyBorder="1" applyAlignment="1"><alignment horizontal="left" vertical="center" wrapText="1" indent="1"/></xf>
        <xf numFmtId="0" fontId="0" fillId="4" borderId="1" xfId="0" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="left" vertical="center" wrapText="1" indent="1"/></xf>
        <xf numFmtId="0" fontId="0" fillId="3" borderId="1" xfId="0" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
        <xf numFmtId="0" fontId="0" fillId="4" borderId="1" xfId="0" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
        <xf numFmtId="165" fontId="0" fillId="3" borderId="1" xfId="0" applyNumberFormat="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center" indent="1"/></xf>
        <xf numFmtId="165" fontId="0" fillId="4" borderId="1" xfId="0" applyNumberFormat="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center" indent="1"/></xf>
        <xf numFmtId="164" fontId="0" fillId="3" borderId="1" xfId="0" applyNumberFormat="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center" indent="1"/></xf>
        <xf numFmtId="164" fontId="0" fillId="4" borderId="1" xfId="0" applyNumberFormat="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center" indent="1"/></xf>
        <xf numFmtId="0" fontId="6" fillId="5" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
        <xf numFmtId="0" fontId="7" fillId="6" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
        <xf numFmtId="0" fontId="10" fillId="7" borderId="3" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
        <xf numFmtId="165" fontId="10" fillId="7" borderId="3" xfId="0" applyNumberFormat="1" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="right" vertical="center" indent="1"/></xf>
        </cellXfs>
        </styleSheet>
    """.trimIndent()

    private fun String.toStyleIndex(): Int = when (this) {
        "BrandTitle" -> 1
        "ReportTitle" -> 2
        "Subtitle" -> 3
        "KpiLabel" -> 4
        "KpiValue" -> 5
        "KpiValueCurrency" -> 6
        "KpiValueSuccess" -> 7
        "KpiValueDanger" -> 8
        "Header" -> 9
        "HeaderLeft" -> 10
        "HeaderRight" -> 11
        "Data" -> 12
        "DataAlt" -> 13
        "DataCenter" -> 14
        "DataCenterAlt" -> 15
        "Number" -> 16
        "NumberAlt" -> 17
        "Integer" -> 18
        "IntegerAlt" -> 19
        "SuccessPill" -> 20
        "DangerPill" -> 21
        "TotalLabel" -> 22
        "TotalValue" -> 23
        else -> 0
    }

    private fun columnName(column: Int): String {
        var value = column
        val name = StringBuilder()
        while (value > 0) {
            val remainder = (value - 1) % 26
            name.insert(0, ('A'.code + remainder).toChar())
            value = (value - 1) / 26
        }
        return name.toString()
    }

    private fun ZipOutputStream.addTextEntry(path: String, content: String) {
        putNextEntry(ZipEntry(path))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun saveAndShare(context: Context, content: ByteArray, fileName: String): Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, EXCEL_MIME_TYPE)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/IkiKasir")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Tidak dapat membuat file di folder Download/IkiKasir.")

            try {
                val output = resolver.openOutputStream(uri, "w")
                    ?: throw IOException("Tidak dapat menulis file ekspor.")
                output.use { it.write(content) }
                val published = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                if (resolver.update(uri, published, null, null) == 0) {
                    throw IOException("File ekspor gagal disimpan ke folder Download.")
                }
                return uri
            } catch (error: Exception) {
                resolver.delete(uri, null, null)
                throw error
            }
        }

        val directory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir,
            "IkiKasir/Export"
        )
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Tidak dapat membuat folder ekspor.")
        }
        val file = File(directory, fileName)
        FileOutputStream(file).use { it.write(content) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun Double.toExcelNumber(): String =
        if (!isFinite()) "0"
        else if (this % 1.0 == 0.0) toLong().toString()
        else toString()

    private fun Double.formatRupiah(): String {
        if (!isFinite()) return "Rp 0"
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
        val absValue = kotlin.math.abs(this)
        val formatted = formatter.format(absValue)
        return if (this < -0.00001) "-Rp $formatted" else "Rp $formatted"
    }

    private fun String.xmlEscape(): String = buildString(length) {
        this@xmlEscape.forEach { char ->
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> if (char == '\t' || char == '\n' || char == '\r' || char >= ' ') append(char)
            }
        }
    }

    private data class ExcelCell(
        val value: String,
        val style: String = "Data",
        val type: String = "String"
    )

    private data class ExcelSheet(
        val name: String,
        val rows: List<List<ExcelCell>>,
        val columnWidths: List<Int>,
        val headerRowIndex: Int = 7,
        val mergeRanges: List<String> = emptyList(),
        val autoFilterRange: String? = null
    )
}
