package com.ptpws.ikikasir.feature.laporanpenjualan.domain.model

data class ProdukTerjualItem(
    val productId: String = "",
    val namaProduk: String = "",
    val kategori: String = "",
    val imageUrl: String = "",
    val hargaSatuan: Double = 0.0,
    val unitTerjual: Int = 0,
    val totalOmzet: Double = 0.0,
    val kontribusiPersen: Double = 0.0,
    val rank: Int = 1
)

data class LaporanPenjualanSummary(
    val totalUnitTerjual: Int = 0,
    val totalOmzet: Double = 0.0,
    val totalProdukUnik: Int = 0,
    val produkTerlarisNama: String = "-",
    val produkTerlarisQty: Int = 0,
    val filterLabel: String = "Hari Ini",
    val items: List<ProdukTerjualItem> = emptyList()
)

enum class PeriodeLaporanPenjualan(val displayName: String) {
    SEMUA("Semua"),
    HARI_INI("Hari Ini"),
    TUJUH_HARI("7 Hari Terakhir"),
    TIGA_PULUH_HARI("30 Hari Terakhir"),
    BULAN_INI("Bulan Ini"),
    KUSTOM("Pilih Tanggal")
}

enum class SortByLaporanPenjualan(val displayName: String) {
    TERBANYAK_QTY("Qty Terbanyak"),
    TERBESAR_OMZET("Omzet Tertinggi"),
    NAMA_AZ("Nama (A-Z)")
}
