package com.ptpws.ikikasir.feature.produk.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ptpws.ikikasir.feature.produk.domain.model.Produk

@Entity(tableName = "products")
data class ProdukEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "price")
    val price: Double,

    @ColumnInfo(name = "stock")
    val stock: Int,

    @ColumnInfo(name = "categoryId")
    val categoryId: String,

    @ColumnInfo(name = "imageUrl")
    val imageUrl: String,

    @ColumnInfo(name = "discount")
    val discount: Double,

    @ColumnInfo(name = "discountType")
    val discountType: String,

    @ColumnInfo(name = "barcode")
    val barcode: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true,

    @ColumnInfo(name = "isDeleted")
    val isDeleted: Boolean = false
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
            isSynced = isSynced
        )
    }
}

fun Produk.toEntity(isSynced: Boolean = true, isDeleted: Boolean = false): ProdukEntity {
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
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}