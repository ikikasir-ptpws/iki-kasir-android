package com.ptpws.ikikasir.feature.kategori.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori

@Entity(tableName = "categories")
data class KategoriEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "iconUrl")
    val iconUrl: String,

    @ColumnInfo(name = "isVisibleInCashier")
    val isVisibleInCashier: Boolean = true,

    @ColumnInfo(name = "productCount")
    val productCount: Int = 0,

    @ColumnInfo(name = "lowStockCount")
    val lowStockCount: Int = 0,

    @ColumnInfo(name = "outOfStockCount")
    val outOfStockCount: Int = 0,

    // Room menyimpan sebagai Long via TimestampConverter, tapi tipenya Timestamp
    @ColumnInfo(name = "createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true,

    @ColumnInfo(name = "isDeleted")
    val isDeleted: Boolean = false
) {
    // Backward-compatibility properties
    val nama: String get() = name
    val deskripsi: String get() = description
    val icon: String get() = iconUrl

    val iconName: String get() {
        return if (iconUrl.contains("|")) {
            iconUrl.split("|").firstOrNull() ?: "LocalCafe"
        } else {
            if (iconUrl.isBlank()) "LocalCafe" else iconUrl
        }
    }

    val colorHex: String get() {
        return if (iconUrl.contains("|")) {
            iconUrl.split("|").getOrNull(1) ?: "#4F46E5"
        } else {
            "#4F46E5"
        }
    }

    val jumlahProduk: Int get() = productCount

    fun toDomain(): Kategori {
        return Kategori(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            isVisibleInCashier = isVisibleInCashier,
            productCount = productCount,
            lowStockCount = lowStockCount,
            outOfStockCount = outOfStockCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }
}

fun Kategori.toEntity(isSynced: Boolean = true, isDeleted: Boolean = false): KategoriEntity {
    return KategoriEntity(
        id = id,
        name = name,
        description = description,
        iconUrl = iconUrl,
        isVisibleInCashier = isVisibleInCashier,
        productCount = productCount,
        lowStockCount = lowStockCount,
        outOfStockCount = outOfStockCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}