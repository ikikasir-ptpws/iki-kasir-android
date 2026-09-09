package com.ptpws.ikikasir.feature.produk.presentation.state

import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class ProdukFormState(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val stock: String = "",
    val categoryId: String = "",
    val imageUrl: String = "",
    val discount: String = "",
    val discountType: String = "PERCENT",
    val barcode: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isEditMode: Boolean = false
) {
    fun toProduk(existingCreatedAt: Long? = null): Produk {
        return Produk(
            id = id.ifBlank { java.util.UUID.randomUUID().toString() },
            name = name.trim(),
            price = price.toDoubleOrNull() ?: 0.0,
            stock = stock.toIntOrNull() ?: 0,
            categoryId = categoryId.trim(),
            imageUrl = imageUrl.trim(),
            discount = discount.toDoubleOrNull() ?: 0.0,
            discountType = discountType.trim(),
            barcode = barcode.trim(),
            createdAt = existingCreatedAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
    }
}