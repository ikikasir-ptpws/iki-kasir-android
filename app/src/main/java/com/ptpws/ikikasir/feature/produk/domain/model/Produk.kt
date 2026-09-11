package com.ptpws.ikikasir.feature.produk.domain.model

import com.google.firebase.Timestamp

data class Produk(
    val id: String = "",
    val name: String = "",
    val categoryId: String = "",
    val imageUrl: String = "",
    val barcode: String = "",
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stock: Int = 0,
    val lowStockThreshold: Int = 5,
    val discount: Double = 0.0,
    val discountType: String = "PERCENT",
    val isVisibleInCashier: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
) {
    val price: Double get() = sellingPrice
}