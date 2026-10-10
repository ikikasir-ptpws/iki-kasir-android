package com.ptpws.ikikasir.feature.role.domain.model

data class Role(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val menuAccess: Map<String, Boolean> = mapOf(
        "Dashboard" to true,
        "Profil" to true,
        "Produk" to false,
        "Kategori Produk" to false,
        "Manajemen Stok" to false,
        "Kasir" to false,
        "Transaksi" to false,
        "Promo" to false,
        "Antrean" to false,
        "Riwayat Antrean" to false,
        "Laporan Penjualan" to false,
        "Laporan Keuangan" to false,
        "Manajemen Pengguna" to false,
        "Audit Log" to false
    ),
    val userCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isSynced: Boolean = true
)
