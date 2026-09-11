package com.ptpws.ikikasir.feature.produk.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.produk.domain.model.Produk

@Entity(tableName = "products")
data class ProdukEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "categoryId")
    val categoryId: String,

    @ColumnInfo(name = "imageUrl")
    val imageUrl: String,

    @ColumnInfo(name = "barcode")
    val barcode: String,

    @ColumnInfo(name = "costPrice")
    val costPrice: Double = 0.0,

    @ColumnInfo(name = "sellingPrice")
    val sellingPrice: Double = 0.0,

    @ColumnInfo(name = "stock")
    val stock: Int,

    @ColumnInfo(name = "lowStockThreshold")
    val lowStockThreshold: Int = 5,

    @ColumnInfo(name = "discount")
    val discount: Double,

    @ColumnInfo(name = "discountType")
    val discountType: String,

    @ColumnInfo(name = "isVisibleInCashier")
    val isVisibleInCashier: Boolean = true,

    @ColumnInfo(name = "createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true,

    @ColumnInfo(name = "isDeleted")
    val isDeleted: Boolean = false
) {
    val price: Double get() = sellingPrice

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
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}