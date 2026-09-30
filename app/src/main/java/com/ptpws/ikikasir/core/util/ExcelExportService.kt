package com.ptpws.ikikasir.core.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val EXCEL_MIME_TYPE = "application/vnd.ms-excel"
private const val SPREADSHEET_NAMESPACE = "urn:schemas-microsoft-com:office:spreadsheet"

@Singleton
class ExcelExportService @Inject constructor() {

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
    private val fileNameDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    suspend fun exportTransaksi(
        context: Context,
        transaksiList: List<PenjualanTransaksi>,
        filterLabel: String = "Semua"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val rows = mutableListOf<List<ExcelCell>>()
            rows += listOf(ExcelCell("LAPORAN RIWAYAT TRANSAKSI", style = "Title"))
            rows += listOf(
                ExcelCell(
                    "Filter: $filterLabel  |  Dicetak: ${dateTimeFormat.format(Date())}  |  Total Data: ${transaksiList.size} transaksi",
                    style = "Subtitle"
                )
            )
            rows.add(emptyList())
            rows += listOf(
                "No", "No. Transaksi", "Tgl & Jam", "Kasir", "Pelanggan", "No. Meja",
                "Subtotal", "Diskon", "PPN", "Total", "Metode Bayar", "Status"
            ).map { ExcelCell(it, style = "Header") }

            var grandSubtotal = 0.0
            var grandDiskon = 0.0
            var grandPpn = 0.0
            var grandTotal = 0.0

            transaksiList.sortedByDescending { it.createdAt.seconds }.forEachIndexed { index, tx ->
                val total = tx.total.takeIf { it > 0 }
                    ?: (tx.subtotal - tx.discount + tx.ppnAmount).coerceAtLeast(0.0)
                val isSuccess = tx.status.equals("COMPLETED", true) || tx.status.equals("LUNAS", true)
                val rowStyle = if (index % 2 == 0) "Data" else "DataAlt"
                val numberStyle = if (index % 2 == 0) "Number" else "NumberAlt"

                rows += listOf(
                    ExcelCell((index + 1).toString(), numberStyle, "Number"),
                    ExcelCell(tx.transactionNumber, rowStyle),
                    ExcelCell(dateTimeFormat.format(tx.createdAt.toDate()), rowStyle),
                    ExcelCell(tx.createdBy.ifBlank { "-" }, rowStyle),
                    ExcelCell(tx.customerName.ifBlank { "-" }, rowStyle),
                    ExcelCell(tx.tableNumber.ifBlank { "-" }, rowStyle),
                    ExcelCell(tx.subtotal.toExcelNumber(), numberStyle, "Number"),
                    ExcelCell(tx.discount.toExcelNumber(), numberStyle, "Number"),
                    ExcelCell(tx.ppnAmount.toExcelNumber(), numberStyle, "Number"),
                    ExcelCell(total.toExcelNumber(), numberStyle, "Number"),
                    ExcelCell(tx.paymentMethod, rowStyle),
                    ExcelCell(if (isSuccess) "Berhasil" else tx.status, if (isSuccess) "Success" else "Failure")
                )

                grandSubtotal += tx.subtotal
                grandDiskon += tx.discount
                grandPpn += tx.ppnAmount
                grandTotal += total
            }

            rows.add(emptyList())
            rows += listOf(
                ExcelCell("TOTAL KESELURUHAN", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell(grandSubtotal.toExcelNumber(), "TotalValue", "Number"),
                ExcelCell(grandDiskon.toExcelNumber(), "TotalValue", "Number"),
                ExcelCell(grandPpn.toExcelNumber(), "TotalValue", "Number"),
                ExcelCell(grandTotal.toExcelNumber(), "TotalValue", "Number"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel")
            )

            saveAndShare(
                context,
                createSpreadsheetXml("Riwayat Transaksi", rows, listOf(45, 130, 105, 100, 100, 70, 85, 75, 70, 85, 95, 75)),
                "Transaksi_${fileNameDateFormat.format(Date())}.xls"
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
            val rows = mutableListOf<List<ExcelCell>>()
            rows += listOf(ExcelCell("LAPORAN RIWAYAT ANTREAN", style = "Title"))
            rows += listOf(
                ExcelCell(
                    "Filter: $filterLabel  |  Dicetak: ${dateTimeFormat.format(Date())}  |  Total Data: ${historyList.size} antrean",
                    style = "Subtitle"
                )
            )
            rows.add(emptyList())
            rows += listOf(
                "No", "ID Transaksi", "No. Antrean", "Pelanggan", "No. Meja",
                "Status", "Produk (Qty)", "Total", "Waktu Selesai"
            ).map { ExcelCell(it, style = "Header") }

            var totalSelesai = 0
            var totalBatal = 0

            historyList.sortedByDescending { it.completedAt.seconds }.forEachIndexed { index, history ->
                val transaction = transaksiMap[history.transactionId]
                val isDone = history.status.equals("DONE", true) || history.status.equals("SELESAI", true)
                val products = transaction?.items?.joinToString("; ") { "${it.name} (${it.quantity})" } ?: "-"
                val total = transaction?.let {
                    it.total.takeIf { value -> value > 0 }
                        ?: (it.subtotal - it.discount + it.ppnAmount).coerceAtLeast(0.0)
                } ?: 0.0
                val rowStyle = if (index % 2 == 0) "Data" else "DataAlt"
                val numberStyle = if (index % 2 == 0) "Number" else "NumberAlt"

                rows += listOf(
                    ExcelCell((index + 1).toString(), numberStyle, "Number"),
                    ExcelCell(history.transactionId, rowStyle),
                    ExcelCell(history.queueSequence.toString(), numberStyle, "Number"),
                    ExcelCell(history.customerName.ifBlank { "-" }, rowStyle),
                    ExcelCell(history.tableNumber.ifBlank { "-" }, rowStyle),
                    ExcelCell(if (isDone) "Selesai" else "Dibatalkan", if (isDone) "Success" else "Failure"),
                    ExcelCell(products, rowStyle),
                    ExcelCell(total.toExcelNumber(), numberStyle, "Number"),
                    ExcelCell(dateTimeFormat.format(history.completedAt.toDate()), rowStyle)
                )

                if (isDone) totalSelesai++ else totalBatal++
            }

            rows.add(emptyList())
            rows += listOf(
                ExcelCell("RINGKASAN", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("Selesai: $totalSelesai", "TotalValue"),
                ExcelCell("Dibatalkan: $totalBatal", "TotalValue"),
                ExcelCell("Total: ${historyList.size}", "TotalValue"),
                ExcelCell("", "TotalLabel")
            )

            saveAndShare(
                context,
                createSpreadsheetXml("Riwayat Antrean", rows, listOf(45, 130, 70, 100, 70, 75, 200, 85, 105)),
                "AntreanHistory_${fileNameDateFormat.format(Date())}.xls"
            )
        }
    }

    private fun createSpreadsheetXml(
        sheetName: String,
        rows: List<List<ExcelCell>>,
        columnWidths: List<Int>
    ): ByteArray {
        val columnCount = rows.maxOfOrNull { it.size } ?: 1
        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8"?>""")
            append("""<?mso-application progid="Excel.Sheet"?>""")
            append("""<Workbook xmlns="$SPREADSHEET_NAMESPACE" xmlns:ss="$SPREADSHEET_NAMESPACE">""")
            appendStyles()
            append("""<Worksheet ss:Name="${sheetName.xmlEscape()}"><Table ss:ExpandedColumnCount="$columnCount" ss:ExpandedRowCount="${rows.size}">""")
            columnWidths.forEach { width ->
                append("""<Column ss:Width="$width"/>""")
            }
            rows.forEach { row ->
                append("<Row>")
                row.forEach { cell ->
                    append("""<Cell ss:StyleID="${cell.style}"><Data ss:Type="${cell.type}">${cell.value.xmlEscape()}</Data></Cell>""")
                }
                append("</Row>")
            }
            append("</Table><WorksheetOptions xmlns=")
            append('"').append("urn:schemas-microsoft-com:office:excel").append('"')
            append("><FreezePanes/><FrozenNoSplit/><SplitHorizontal>4</SplitHorizontal><TopRowBottomPane>4</TopRowBottomPane></WorksheetOptions>")
            append("</Worksheet></Workbook>")
        }.toByteArray(Charsets.UTF_8)
    }

    private fun StringBuilder.appendStyles() {
        append("<Styles>")
        append("""<Style ss:ID="Default" ss:Name="Normal"><Alignment ss:Vertical="Center"/><Font ss:FontName="Calibri" ss:Size="10"/></Style>""")
        append("""<Style ss:ID="Title"><Alignment ss:Horizontal="Center" ss:Vertical="Center"/><Font ss:FontName="Calibri" ss:Size="16" ss:Bold="1" ss:Color="#FFFFFF"/><Interior ss:Color="#4338CA" ss:Pattern="Solid"/></Style>""")
        append("""<Style ss:ID="Subtitle"><Font ss:FontName="Calibri" ss:Size="10" ss:Color="#1E3A8A"/><Interior ss:Color="#DBEAFE" ss:Pattern="Solid"/></Style>""")
        append("""<Style ss:ID="Header"><Alignment ss:Horizontal="Center" ss:Vertical="Center"/><Font ss:FontName="Calibri" ss:Size="10" ss:Bold="1" ss:Color="#FFFFFF"/><Interior ss:Color="#1E3A8A" ss:Pattern="Solid"/></Style>""")
        append("""<Style ss:ID="Data"><Borders><Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="0.5" ss:Color="#D1D5DB"/></Borders></Style>""")
        append("""<Style ss:ID="DataAlt"><Interior ss:Color="#EFF6FF" ss:Pattern="Solid"/></Style>""")
        append("""<Style ss:ID="Number"><Alignment ss:Horizontal="Right"/><NumberFormat ss:Format="#,##0.00"/></Style>""")
        append("""<Style ss:ID="NumberAlt"><Alignment ss:Horizontal="Right"/><NumberFormat ss:Format="#,##0.00"/><Interior ss:Color="#EFF6FF" ss:Pattern="Solid"/></Style>""")
        append("""<Style ss:ID="Success"><Alignment ss:Horizontal="Center"/><Font ss:Bold="1" ss:Color="#166534"/><Interior ss:Color="#DCFCE7" ss:Pattern="Solid"/></Style>""")
        append("""<Style ss:ID="Failure"><Alignment ss:Horizontal="Center"/><Font ss:Bold="1" ss:Color="#991B1B"/><Interior ss:Color="#FEE2E2" ss:Pattern="Solid"/></Style>""")
        append("""<Style ss:ID="TotalLabel"><Alignment ss:Horizontal="Center"/><Font ss:Bold="1" ss:Color="#FFFFFF"/><Interior ss:Color="#1E3A8A" ss:Pattern="Solid"/></Style>""")
        append("""<Style ss:ID="TotalValue"><Alignment ss:Horizontal="Right"/><Font ss:Bold="1" ss:Color="#FFFFFF"/><Interior ss:Color="#4338CA" ss:Pattern="Solid"/></Style>""")
        append("</Styles>")
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
        if (isFinite()) toString() else "0"

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
}
