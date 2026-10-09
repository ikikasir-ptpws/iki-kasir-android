package com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.keuangan.domain.model.DailySalesExportEntry
import com.ptpws.ikikasir.feature.keuangan.domain.model.FinancialReportExportData
import com.ptpws.ikikasir.feature.keuangan.domain.model.PaymentMethodExportEntry
import com.ptpws.ikikasir.feature.keuangan.domain.model.TopProductExportEntry
import com.ptpws.ikikasir.feature.keuangan.domain.usecase.ExportLaporanKeuanganToExcelUseCase
import com.ptpws.ikikasir.feature.keuangan.presentation.state.DailySalesEntry
import com.ptpws.ikikasir.feature.keuangan.presentation.state.LaporanKeuanganState
import com.ptpws.ikikasir.feature.keuangan.presentation.state.MetodePembayaranReport
import com.ptpws.ikikasir.feature.keuangan.presentation.state.PeriodeFilter
import com.ptpws.ikikasir.feature.keuangan.presentation.state.TopProdukReport
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetAllTransaksiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LaporanKeuanganViewModel @Inject constructor(
    private val getAllTransaksiUseCase: GetAllTransaksiUseCase,
    private val exportLaporanKeuanganToExcelUseCase: ExportLaporanKeuanganToExcelUseCase,
    private val logActivityUseCase: LogActivityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LaporanKeuanganState(isLoading = true))
    val state: StateFlow<LaporanKeuanganState> = _state.asStateFlow()

    private var rawTransaksiList: List<PenjualanTransaksi> = emptyList()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllTransaksiUseCase().collect { list ->
                rawTransaksiList = list
                calculateAnalytics()
            }
        }
    }

    fun setPeriode(periode: PeriodeFilter) {
        _state.update { it.copy(selectedPeriode = periode) }
        calculateAnalytics()
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        val sDate = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(startMillis))
        val eDate = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(endMillis))
        val chipLabel = if (sDate == eDate) {
            SimpleDateFormat("dd MMM", Locale("id", "ID")).format(Date(startMillis))
        } else {
            "${SimpleDateFormat("dd MMM", Locale("id", "ID")).format(Date(startMillis))} - ${SimpleDateFormat("dd MMM", Locale("id", "ID")).format(Date(endMillis))}"
        }
        val fullLabel = if (sDate == eDate) sDate else "$sDate - $eDate"

        _state.update {
            it.copy(
                selectedPeriode = PeriodeFilter.KUSTOM,
                customStartDateMillis = startMillis,
                customEndDateMillis = endMillis,
                customDateLabel = chipLabel,
                dateRangeLabel = fullLabel
            )
        }
        calculateAnalytics()
    }

    fun exportToExcel(context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportError = null) }
            val currentFiltered = getFilteredTransactions()
            val st = _state.value

            val exportData = FinancialReportExportData(
                filterLabel = st.dateRangeLabel,
                totalOmzet = st.totalOmzet,
                totalLabaKotor = st.totalLabaKotor,
                marginKotorPersen = st.marginKotorPersen,
                totalLabaBersih = st.totalLabaBersih,
                profitMarginPersen = st.profitMarginPersen,
                totalTransaksiCount = st.totalTransaksiCount,
                totalProdukTerjual = st.totalProdukTerjual,
                rataRataTransaksi = st.rataRataTransaksi,
                totalDiskon = st.totalDiskon,
                totalPajak = st.totalPajak,
                totalHpp = st.totalHpp,
                dailySales = st.dailySales.map {
                    DailySalesExportEntry(
                        dayLabel = it.dayLabel,
                        dateLabel = it.dateLabel,
                        totalOmzet = it.totalOmzet,
                        totalLaba = it.totalLaba,
                        jumlahTransaksi = it.jumlahTransaksi,
                        totalHpp = it.totalHpp,
                        totalDiskon = it.totalDiskon
                    )
                },
                topProdukList = st.topProdukList.map {
                    TopProductExportEntry(
                        rank = it.rank,
                        productId = it.productId,
                        namaProduk = it.namaProduk,
                        unitTerjual = it.unitTerjual,
                        totalOmzet = it.totalOmzet,
                        kontribusiPersen = it.kontribusiPersen
                    )
                },
                metodePembayaranList = st.metodePembayaranList.map {
                    PaymentMethodExportEntry(
                        metode = it.metode,
                        totalNominal = it.totalNominal,
                        jumlahTransaksi = it.jumlahTransaksi,
                        persentase = it.persentase
                    )
                }
            )

            val result = exportLaporanKeuanganToExcelUseCase(
                context = context,
                reportData = exportData,
                transaksiList = if (currentFiltered.isNotEmpty()) currentFiltered else rawTransaksiList
            )

            result.fold(
                onSuccess = { uri ->
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportedFileUri = uri,
                            exportError = null,
                            exportMessage = "Laporan Keuangan berhasil diekspor!"
                        )
                    }
                    logActivityUseCase(
                        title = "Ekspor Laporan Keuangan",
                        description = "Laporan Keuangan (${st.dateRangeLabel}) berhasil diunduh ke Excel.",
                        category = "LAPORAN",
                        action = "EXPORT",
                        isWarning = false
                    )
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportError = error.message ?: "Gagal mengekspor laporan keuangan.",
                            exportMessage = "Gagal mengekspor: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun exportLaporan(context: Context, onResult: (Uri?) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportError = null) }
            val currentFiltered = getFilteredTransactions()
            val st = _state.value

            val exportData = FinancialReportExportData(
                filterLabel = st.dateRangeLabel,
                totalOmzet = st.totalOmzet,
                totalLabaKotor = st.totalLabaKotor,
                marginKotorPersen = st.marginKotorPersen,
                totalLabaBersih = st.totalLabaBersih,
                profitMarginPersen = st.profitMarginPersen,
                totalTransaksiCount = st.totalTransaksiCount,
                totalProdukTerjual = st.totalProdukTerjual,
                rataRataTransaksi = st.rataRataTransaksi,
                totalDiskon = st.totalDiskon,
                totalPajak = st.totalPajak,
                totalHpp = st.totalHpp,
                dailySales = st.dailySales.map {
                    DailySalesExportEntry(
                        dayLabel = it.dayLabel,
                        dateLabel = it.dateLabel,
                        totalOmzet = it.totalOmzet,
                        totalLaba = it.totalLaba,
                        jumlahTransaksi = it.jumlahTransaksi,
                        totalHpp = it.totalHpp,
                        totalDiskon = it.totalDiskon
                    )
                },
                topProdukList = st.topProdukList.map {
                    TopProductExportEntry(
                        rank = it.rank,
                        productId = it.productId,
                        namaProduk = it.namaProduk,
                        unitTerjual = it.unitTerjual,
                        totalOmzet = it.totalOmzet,
                        kontribusiPersen = it.kontribusiPersen
                    )
                },
                metodePembayaranList = st.metodePembayaranList.map {
                    PaymentMethodExportEntry(
                        metode = it.metode,
                        totalNominal = it.totalNominal,
                        jumlahTransaksi = it.jumlahTransaksi,
                        persentase = it.persentase
                    )
                }
            )

            val result = exportLaporanKeuanganToExcelUseCase(
                context = context,
                reportData = exportData,
                transaksiList = if (currentFiltered.isNotEmpty()) currentFiltered else rawTransaksiList
            )

            result.fold(
                onSuccess = { uri ->
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportedFileUri = uri,
                            exportError = null,
                            exportMessage = "Laporan Keuangan berhasil diekspor!"
                        )
                    }
                    logActivityUseCase(
                        title = "Ekspor Laporan Keuangan",
                        description = "Laporan Keuangan (${st.dateRangeLabel}) berhasil diunduh ke Excel.",
                        category = "LAPORAN",
                        action = "EXPORT",
                        isWarning = false
                    )
                    onResult(uri)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportError = error.message ?: "Gagal mengekspor laporan keuangan.",
                            exportMessage = "Gagal mengekspor: ${error.message}"
                        )
                    }
                    onResult(null)
                }
            )
        }
    }

    fun clearExportState() {
        _state.update { it.copy(exportedFileUri = null, exportError = null) }
    }

    fun clearExportMessage() {
        _state.update { it.copy(exportMessage = null) }
    }

    private fun getFilteredTransactions(): List<PenjualanTransaksi> {
        if (rawTransaksiList.isEmpty()) return emptyList()

        val periode = _state.value.selectedPeriode
        val now = System.currentTimeMillis()

        return when (periode) {
            PeriodeFilter.SEMUA -> {
                rawTransaksiList
            }
            PeriodeFilter.HARI_INI -> {
                val start = getStartOfDay(now)
                val end = getEndOfDay(now)
                rawTransaksiList.filter { it.createdAt.toDate().time in start..end }
            }
            PeriodeFilter.TUJUH_HARI -> {
                val start = getStartOfDay(now - 6L * 86400000L)
                val end = getEndOfDay(now)
                rawTransaksiList.filter { it.createdAt.toDate().time in start..end }
            }
            PeriodeFilter.TIGA_PULUH_HARI -> {
                val start = getStartOfDay(now - 29L * 86400000L)
                val end = getEndOfDay(now)
                rawTransaksiList.filter { it.createdAt.toDate().time in start..end }
            }
            PeriodeFilter.BULAN_INI -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = cal.timeInMillis
                val end = getEndOfDay(now)
                rawTransaksiList.filter { it.createdAt.toDate().time in start..end }
            }
            PeriodeFilter.KUSTOM -> {
                val start = _state.value.customStartDateMillis?.let { getStartOfDay(it) } ?: 0L
                val end = _state.value.customEndDateMillis?.let { getEndOfDay(it) } ?: Long.MAX_VALUE
                rawTransaksiList.filter { it.createdAt.toDate().time in start..end }
            }
        }
    }

    private fun calculateAnalytics() {
        val filtered = getFilteredTransactions()
        val periode = _state.value.selectedPeriode

        val rangeLabel = computeRangeLabel(periode)

        // Hitung Metrik Riil dari database (100% data riil)
        var omzet = 0.0
        var diskon = 0.0
        var pajak = 0.0
        var hpp = 0.0
        var totalQtyTerjual = 0

        val productMap = mutableMapOf<String, ProductAccumulator>()
        val paymentMap = mutableMapOf<String, PaymentAccumulator>()

        for (tx in filtered) {
            val txTotal = if (tx.total > 0) tx.total else tx.subtotal
            omzet += txTotal
            diskon += tx.discount
            pajak += tx.ppnAmount

            // Hitung HPP, kuantitas produk terjual, dan agregat produk
            if (tx.items.isNotEmpty()) {
                for (item in tx.items) {
                    val qty = item.quantity
                    totalQtyTerjual += qty

                    val unitCost = if (item.produk.costPrice > 0) item.produk.costPrice else (item.price * 0.6)
                    hpp += (unitCost * qty)

                    val key = if (item.produk.id.isNotBlank()) item.produk.id else item.name
                    val acc = productMap.getOrPut(key) {
                        ProductAccumulator(
                            productId = item.produk.id,
                            name = item.name.ifBlank { item.produk.name },
                            imageUrl = item.produk.imageUrl,
                            categoryId = item.produk.categoryId
                        )
                    }
                    acc.qty += qty
                    acc.omzet += item.totalPrice
                }
            } else if (txTotal > 0) {
                totalQtyTerjual += 1
            }

            // Agregat metode pembayaran
            val rawMethod = tx.paymentMethod.trim()
            val normalizedMethod = when {
                rawMethod.contains("tunai", true) || rawMethod.contains("cash", true) -> "Tunai (Cash)"
                rawMethod.contains("qris", true) -> "QRIS / E-Wallet"
                rawMethod.contains("transfer", true) -> "Transfer Bank"
                rawMethod.contains("debit", true) || rawMethod.contains("kartu", true) -> "Kartu Debit/Kredit"
                rawMethod.isNotBlank() -> rawMethod
                else -> "Tunai (Cash)"
            }
            val pAcc = paymentMap.getOrPut(normalizedMethod) { PaymentAccumulator(normalizedMethod) }
            pAcc.count += 1
            pAcc.total += txTotal
        }

        val labaKotor = (omzet - hpp).coerceAtLeast(0.0)
        val marginKotor = if (omzet > 0) ((labaKotor / omzet) * 100).toFloat() else 0f
        val labaBersih = (omzet - hpp - diskon).coerceAtLeast(0.0)
        val profitMargin = if (omzet > 0) ((labaBersih / omzet) * 100).toFloat() else 0f
        val aov = if (filtered.isNotEmpty()) omzet / filtered.size else 0.0

        // Buat Daily Sales buckets untuk 7 hari atau periode aktif
        val dailyList = buildDailySalesBuckets(filtered)

        // Buat Top 5 Produk
        val topList = productMap.values
            .sortedByDescending { it.qty }
            .take(5)
            .mapIndexed { index, p ->
                TopProdukReport(
                    rank = index + 1,
                    productId = p.productId,
                    namaProduk = p.name,
                    imageUrl = p.imageUrl,
                    categoryId = p.categoryId,
                    unitTerjual = p.qty,
                    totalOmzet = p.omzet,
                    kontribusiPersen = if (omzet > 0) ((p.omzet / omzet) * 100).toFloat() else 0f
                )
            }

        // Buat Metode Pembayaran
        val paymentList = paymentMap.values
            .sortedByDescending { it.total }
            .map { p ->
                MetodePembayaranReport(
                    metode = p.methodName,
                    totalNominal = p.total,
                    jumlahTransaksi = p.count,
                    persentase = if (omzet > 0) ((p.total / omzet) * 100).toFloat() else 0f
                )
            }

        _state.update {
            it.copy(
                isLoading = false,
                isDemoData = false,
                dateRangeLabel = rangeLabel,
                totalOmzet = omzet,
                totalLabaKotor = labaKotor,
                marginKotorPersen = marginKotor,
                totalLabaBersih = labaBersih,
                profitMarginPersen = profitMargin,
                totalTransaksiCount = filtered.size,
                totalProdukTerjual = totalQtyTerjual,
                rataRataTransaksi = aov,
                totalDiskon = diskon,
                totalPajak = pajak,
                totalHpp = hpp,
                dailySales = dailyList,
                topProdukList = topList,
                metodePembayaranList = paymentList
            )
        }
    }

    private fun buildDailySalesBuckets(transactions: List<PenjualanTransaksi>): List<DailySalesEntry> {
        val result = mutableListOf<DailySalesEntry>()
        val dayNameFmt = SimpleDateFormat("EEE", Locale("id", "ID"))
        val dateFmt = SimpleDateFormat("dd MMM", Locale("id", "ID"))

        // Ambil 7 hari ke belakang
        for (i in 6 downTo 0) {
            val targetCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val start = getStartOfDay(targetCal.timeInMillis)
            val end = getEndOfDay(targetCal.timeInMillis)

            val dayTxs = transactions.filter { it.createdAt.toDate().time in start..end }
            val dayOmzet = dayTxs.sumOf { if (it.total > 0) it.total else it.subtotal }
            val dayHpp = dayTxs.sumOf { tx ->
                tx.items.sumOf { item ->
                    (if (item.produk.costPrice > 0) item.produk.costPrice else item.price * 0.6) * item.quantity
                }
            }
            val dayDiscount = dayTxs.sumOf { it.discount }
            val dayLaba = (dayOmzet - dayHpp - dayDiscount).coerceAtLeast(0.0)

            result.add(
                DailySalesEntry(
                    dayLabel = dayNameFmt.format(targetCal.time),
                    dateLabel = dateFmt.format(targetCal.time),
                    timestampMillis = targetCal.timeInMillis,
                    totalOmzet = dayOmzet,
                    totalLaba = dayLaba,
                    jumlahTransaksi = dayTxs.size,
                    totalHpp = dayHpp,
                    totalDiskon = dayDiscount
                )
            )
        }
        return result
    }

    private fun computeRangeLabel(periode: PeriodeFilter): String {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val now = Date()
        return when (periode) {
            PeriodeFilter.SEMUA -> "Semua Periode Transaksi"
            PeriodeFilter.HARI_INI -> "Hari Ini (${dateFmt.format(now)})"
            PeriodeFilter.TUJUH_HARI -> "7 Hari Terakhir"
            PeriodeFilter.TIGA_PULUH_HARI -> "30 Hari Terakhir"
            PeriodeFilter.BULAN_INI -> "Bulan Ini (${SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(now)})"
            PeriodeFilter.KUSTOM -> {
                val s = _state.value.customStartDateMillis
                val e = _state.value.customEndDateMillis
                if (s != null && e != null) {
                    val sStr = dateFmt.format(Date(s))
                    val eStr = dateFmt.format(Date(e))
                    if (sStr == eStr) sStr else "$sStr - $eStr"
                } else "Rentang Kustom"
            }
        }
    }

    private fun getStartOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private class ProductAccumulator(
        val productId: String,
        val name: String,
        val imageUrl: String,
        val categoryId: String,
        var qty: Int = 0,
        var omzet: Double = 0.0
    )

    private class PaymentAccumulator(
        val methodName: String,
        var count: Int = 0,
        var total: Double = 0.0
    )
}
