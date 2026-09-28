package com.ptpws.ikikasir.feature.promo.domain.model

import com.google.firebase.Timestamp

data class Promo(
    val id: String = "",
    val name: String = "",
    val promoType: String = "",
    val items: List<PromoProductItem> = emptyList(),
    val discountType: String = "Rp", // "Rp" or "%"
    val discountValue: Double = 0.0,
    val startDate: String = "",
    val endDate: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
) {
    // Compatibility getters for code using Indonesian field names
    val nama: String get() = name
    val tipePromo: String get() = promoType
    val diskonType: String get() = discountType
    val nilaiDiskon: Double get() = discountValue
    val tanggalMulai: String get() = startDate
    val tanggalBerakhir: String get() = endDate
}
