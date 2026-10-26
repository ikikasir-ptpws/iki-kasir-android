package com.ptpws.ikikasir.feature.produk.presentation.state

import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class ProdukFormState(
    val id: String = "",
    val name: String = "",
    val costPrice: String = "",
    val sellingPrice: String = "",
    val stock: String = "",
    val lowStockThreshold: String = "5",
    val categoryId: String = "",
    val imageUrl: String = "",
    val discount: String = "",
    val discountType: String = "PERCENT",
    val barcode: String = "",
    val isVisibleInCashier: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isEditMode: Boolean = false
) {
    val price: String get() = sellingPrice

    fun toProduk(existingCreatedAt: Timestamp? = null): Produk {
        return Produk(
            id = id.ifBlank { java.util.UUID.randomUUID().toString() },
            name = name.trim(),
            costPrice = parsePriceStringToDouble(costPrice),
            sellingPrice = parsePriceStringToDouble(sellingPrice),
            stock = stock.toIntOrNull() ?: 0,
            lowStockThreshold = lowStockThreshold.toIntOrNull() ?: 5,
            categoryId = categoryId.trim(),
            imageUrl = imageUrl.trim(),
            discount = parsePriceStringToDouble(discount),
            discountType = discountType.trim(),
            barcode = barcode.trim(),
            isVisibleInCashier = isVisibleInCashier,
            createdAt = existingCreatedAt ?: Timestamp.now(),
            updatedAt = Timestamp.now(),
            isSynced = false
        )
    }
}

fun parsePriceStringToDouble(input: String): Double {
    if (input.isBlank()) return 0.0
    val cleanDigits = input.replace("[^0-9]".toRegex(), "")
    return cleanDigits.toDoubleOrNull() ?: 0.0
}