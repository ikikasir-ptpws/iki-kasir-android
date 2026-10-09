package com.ptpws.ikikasir.feature.keuangan.domain.model

data class FinancialReportExportData(
    val filterLabel: String,
    val totalOmzet: Double,
    val totalLabaKotor: Double,
    val marginKotorPersen: Float,
    val totalLabaBersih: Double,
    val profitMarginPersen: Float,
    val totalTransaksiCount: Int,
    val totalProdukTerjual: Int,
    val rataRataTransaksi: Double,
    val totalDiskon: Double,
    val totalPajak: Double,
    val totalHpp: Double,
    val dailySales: List<DailySalesExportEntry> = emptyList(),
    val topProdukList: List<TopProductExportEntry> = emptyList(),
    val metodePembayaranList: List<PaymentMethodExportEntry> = emptyList()
)

data class DailySalesExportEntry(
    val dayLabel: String,
    val dateLabel: String,
    val totalOmzet: Double,
    val totalLaba: Double,
    val jumlahTransaksi: Int,
    val totalHpp: Double = 0.0,
    val totalDiskon: Double = 0.0
)

data class TopProductExportEntry(
    val rank: Int,
    val productId: String,
    val namaProduk: String,
    val unitTerjual: Int,
    val totalOmzet: Double,
    val kontribusiPersen: Float
)

data class PaymentMethodExportEntry(
    val metode: String,
    val totalNominal: Double,
    val jumlahTransaksi: Int,
    val persentase: Float
)
