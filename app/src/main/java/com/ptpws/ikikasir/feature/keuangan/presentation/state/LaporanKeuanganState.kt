package com.ptpws.ikikasir.feature.keuangan.presentation.state

import android.net.Uri

enum class PeriodeFilter(val label: String) {
    SEMUA("Semua"),
    HARI_INI("Hari Ini"),
    TUJUH_HARI("7 Hari Terakhir"),
    TIGA_PULUH_HARI("30 Hari Terakhir"),
    BULAN_INI("Bulan Ini"),
    KUSTOM("Pilih Tanggal")
}

data class TopProdukReport(
    val rank: Int,
    val productId: String,
    val namaProduk: String,
    val imageUrl: String,
    val categoryId: String,
    val unitTerjual: Int,
    val totalOmzet: Double,
    val kontribusiPersen: Float
)

data class MetodePembayaranReport(
    val metode: String,
    val totalNominal: Double,
    val jumlahTransaksi: Int,
    val persentase: Float
)

data class DailySalesEntry(
    val dayLabel: String,      // Misal: "Sen", "Sel", "Rab"
    val dateLabel: String,     // Misal: "07 Okt"
    val timestampMillis: Long,
    val totalOmzet: Double,
    val totalLaba: Double,
    val jumlahTransaksi: Int,
    val totalHpp: Double = 0.0,
    val totalDiskon: Double = 0.0
)

data class LaporanKeuanganState(
    val isLoading: Boolean = false,
    val selectedPeriode: PeriodeFilter = PeriodeFilter.SEMUA,
    val customStartDateMillis: Long? = null,
    val customEndDateMillis: Long? = null,
    val customDateLabel: String = "",
    val dateRangeLabel: String = "Semua Periode Transaksi",
    val isDemoData: Boolean = false,

    // KPI Finansial Utama
    val totalOmzet: Double = 0.0,
    val totalLabaKotor: Double = 0.0,
    val marginKotorPersen: Float = 0f,
    val totalLabaBersih: Double = 0.0,
    val profitMarginPersen: Float = 0f,
    val totalTransaksiCount: Int = 0,
    val totalProdukTerjual: Int = 0,
    val rataRataTransaksi: Double = 0.0, // AOV
    val totalDiskon: Double = 0.0,
    val totalPajak: Double = 0.0,
    val totalHpp: Double = 0.0,
    val totalRefundNominal: Double = 0.0,
    val totalRefundCount: Int = 0,

    // Komponen Rinci
    val dailySales: List<DailySalesEntry> = emptyList(),
    val topProdukList: List<TopProdukReport> = emptyList(),
    val metodePembayaranList: List<MetodePembayaranReport> = emptyList(),

    // State Interaksi & Ekspor
    val isExporting: Boolean = false,
    val exportedFileUri: Uri? = null,
    val exportError: String? = null,
    val exportMessage: String? = null
)
