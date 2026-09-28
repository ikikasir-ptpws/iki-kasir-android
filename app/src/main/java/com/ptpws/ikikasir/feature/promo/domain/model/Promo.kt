package com.ptpws.ikikasir.feature.promo.domain.model

import com.google.firebase.Timestamp

data class Promo(
    val id: String = "",
    val nama: String = "",
    val tipePromo: String = "",
    val items: List<PromoProductItem> = emptyList(),
    val diskonType: String = "Rp", // "Rp" or "%"
    val nilaiDiskon: Double = 0.0,
    val tanggalMulai: String = "",
    val tanggalBerakhir: String = "",
    val deskripsi: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
)
