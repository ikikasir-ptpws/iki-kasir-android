package com.ptpws.ikikasir.feature.kategori.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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

    @ColumnInfo(name = "icon")
    val icon: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true,

    @ColumnInfo(name = "isDeleted")
    val isDeleted: Boolean = false
) {
    // Backward-compatibility properties
    val nama: String get() = name
    val deskripsi: String get() = description
    val iconName: String get() = icon
    val colorHex: String get() = "#4F46E5"
    val jumlahProduk: Int get() = 0

    fun toDomain(): Kategori {
        return Kategori(
            id = id,
            name = name,
            description = description,
            icon = icon,
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
        icon = icon,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}
