package com.ptpws.ikikasir.feature.kategori.domain.model

import com.google.firebase.Timestamp

data class Kategori(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "LocalCafe|#4F46E5",
    val isVisibleInCashier: Boolean = true,
    val productCount: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
) {
    // Backward-compatible properties & aliases
    val nama: String get() = name
    val deskripsi: String get() = description
    val icon: String get() = iconUrl

    val iconName: String get() {
        return if (iconUrl.contains("|")) {
            iconUrl.split("|").firstOrNull() ?: "LocalCafe"
        } else {
            if (iconUrl.isBlank()) "LocalCafe" else iconUrl
        }
    }

    val colorHex: String get() {
        return if (iconUrl.contains("|")) {
            iconUrl.split("|").getOrNull(1) ?: "#4F46E5"
        } else {
            "#4F46E5"
        }
    }

    val jumlahProduk: Int get() = productCount
}