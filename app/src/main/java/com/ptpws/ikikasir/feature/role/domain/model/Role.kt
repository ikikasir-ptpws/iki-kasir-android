package com.ptpws.ikikasir.feature.role.domain.model

import com.google.firebase.Timestamp

data class Role(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val menuAccess: Map<String, Boolean> = mapOf(
        "Dashboard" to false,
        "Produk" to false,
        "Kategori Produk" to false,
        "Manajemen Stok" to false,
        "Kasir" to false,
        "Transaksi" to false,
        "Antrean" to false,
        "Riwayat Antrean" to false,
        "Laporan Keuangan" to false,
        "Manajemen Pengguna" to false,
        "Manajemen Role" to false,
        "Member" to false,
        "Pengaturan Menu" to false,
        "Audit Log" to false
    ),
    val userCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
)
