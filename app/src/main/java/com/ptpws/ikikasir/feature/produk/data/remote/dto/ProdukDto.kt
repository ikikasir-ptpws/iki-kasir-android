package com.ptpws.ikikasir.feature.produk.data.remote.dto

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.feature.produk.data.local.entity.ProdukEntity
import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class ProdukDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("price") @set:PropertyName("price")
    var price: Double = 0.0,

    @get:PropertyName("stock") @set:PropertyName("stock")
    var stock: Int = 0,

    @get:PropertyName("categoryId") @set:PropertyName("categoryId")
    var categoryId: String = "",

    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl")
    var imageUrl: String = "",

    @get:PropertyName("discount") @set:PropertyName("discount")
    var discount: Double = 0.0,

    @get:PropertyName("discountType") @set:PropertyName("discountType")
    var discountType: String = "",

    @get:PropertyName("barcode") @set:PropertyName("barcode")
    var barcode: String = "",

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = 0L,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Long = 0L
) {
    fun toDomain(): Produk {
        return Produk(
            id = id,
            name = name,
            price = price,
            stock = stock,
            categoryId = categoryId,
            imageUrl = imageUrl,
            discount = discount,
            discountType = discountType,
            barcode = barcode,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = true
        )
    }

    fun toEntity(): ProdukEntity {
        return ProdukEntity(
            id = id,
            name = name,
            price = price,
            stock = stock,
            categoryId = categoryId,
            imageUrl = imageUrl,
            discount = discount,
            discountType = discountType,
            barcode = barcode,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = true,
            isDeleted = false
        )
    }
}

fun Produk.toDto(): ProdukDto {
    return ProdukDto(
        id = id,
        name = name,
        price = price,
        stock = stock,
        categoryId = categoryId,
        imageUrl = imageUrl,
        discount = discount,
        discountType = discountType,
        barcode = barcode,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}