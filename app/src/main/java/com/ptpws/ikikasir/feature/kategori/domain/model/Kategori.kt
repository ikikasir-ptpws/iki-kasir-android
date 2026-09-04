package com.ptpws.ikikasir.feature.kategori.domain.model

data class Kategori(
    val id: String = "",
    val nama: String = "",
    val deskripsi: String = "",
    val iconName: String = "Category",
    val colorHex: String = "#4F46E5",
    val jumlahProduk: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)
