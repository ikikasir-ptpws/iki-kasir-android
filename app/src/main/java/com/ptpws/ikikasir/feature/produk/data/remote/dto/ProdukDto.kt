package com.ptpws.ikikasir.feature.produk.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.ptpws.ikikasir.feature.produk.data.local.entity.ProdukEntity
import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class ProdukDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("categoryId") @set:PropertyName("categoryId")
    var categoryId: String = "",

    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl")
    var imageUrl: String = "",

    @get:PropertyName("barcode") @set:PropertyName("barcode")
    var barcode: String = "",

    @get:PropertyName("costPrice") @set:PropertyName("costPrice")
    var costPrice: Double = 0.0,

    @get:PropertyName("sellingPrice") @set:PropertyName("sellingPrice")
    var sellingPrice: Double = 0.0,

    @get:PropertyName("stock") @set:PropertyName("stock")
    var stock: Int = 0,

    @get:PropertyName("lowStockThreshold") @set:PropertyName("lowStockThreshold")
    var lowStockThreshold: Int = 5,

    @get:PropertyName("discount") @set:PropertyName("discount")
    var discount: Double = 0.0,

    @get:PropertyName("discountType") @set:PropertyName("discountType")
    var discountType: String = "PERCENT",

    @get:PropertyName("isVisibleInCashier") @set:PropertyName("isVisibleInCashier")
    var isVisibleInCashier: Boolean = true,

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    @get:ServerTimestamp
    var createdAt: Timestamp? = null,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @get:ServerTimestamp
    var updatedAt: Timestamp? = null
) {
    fun toDomain(): Produk {
        return Produk(
            id = id,
            name = name,
            categoryId = categoryId,
            imageUrl = imageUrl,
            barcode = barcode,
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            stock = stock,
            lowStockThreshold = lowStockThreshold,
            discount = discount,
            discountType = discountType,
            isVisibleInCashier = isVisibleInCashier,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true
        )
    }

    fun toEntity(): ProdukEntity {
        return ProdukEntity(
            id = id,
            name = name,
            categoryId = categoryId,
            imageUrl = imageUrl,
            barcode = barcode,
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            stock = stock,
            lowStockThreshold = lowStockThreshold,
            discount = discount,
            discountType = discountType,
            isVisibleInCashier = isVisibleInCashier,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true,
            isDeleted = false
        )
    }
}

fun Produk.toDto(): ProdukDto {
    return ProdukDto(
        id = id,
        name = name,
        categoryId = categoryId,
        imageUrl = imageUrl,
        barcode = barcode,
        costPrice = costPrice,
        sellingPrice = sellingPrice,
        stock = stock,
        lowStockThreshold = lowStockThreshold,
        discount = discount,
        discountType = discountType,
        isVisibleInCashier = isVisibleInCashier,
        createdAt = null,
        updatedAt = null
    )
}