package com.ptpws.ikikasir.feature.kategori.domain.model

data class Kategori(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "Restaurant",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
) {
    // Backward-compatible properties & aliases
    val nama: String get() = name
    val deskripsi: String get() = description
    val iconName: String get() = icon
    val colorHex: String get() = "#4F46E5"
    val jumlahProduk: Int get() = 0
}
