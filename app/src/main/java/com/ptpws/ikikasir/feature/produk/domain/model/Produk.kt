package com.ptpws.ikikasir.feature.produk.domain.model

data class Produk(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val categoryId: String = "",
    val imageUrl: String = "",
    val discount: Double = 0.0,
    val discountType: String = "",
    val barcode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)