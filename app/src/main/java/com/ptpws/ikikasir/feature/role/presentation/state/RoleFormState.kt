package com.ptpws.ikikasir.feature.role.presentation.state

data class RoleFormState(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdAt: Long = 0L,
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
        "Laporan Keuangan" to false,
        "Manajemen Pengguna" to false,
        "Audit Log" to false
    ),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
