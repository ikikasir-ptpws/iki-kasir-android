package com.ptpws.ikikasir.feature.kategori.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori

@Entity(tableName = "kategori")
data class KategoriEntity(
    @PrimaryKey
    val id: String,
    val nama: String,
    val deskripsi: String,
    val iconName: String,
    val colorHex: String,
    val jumlahProduk: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true,
    val isDeleted: Boolean = false
) {
    fun toDomain(): Kategori {
        return Kategori(
            id = id,
            nama = nama,
            deskripsi = deskripsi,
            iconName = iconName,
            colorHex = colorHex,
            jumlahProduk = jumlahProduk,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }
}

fun Kategori.toEntity(isSynced: Boolean = true, isDeleted: Boolean = false): KategoriEntity {
    return KategoriEntity(
        id = id,
        nama = nama,
        deskripsi = deskripsi,
        iconName = iconName,
        colorHex = colorHex,
        jumlahProduk = jumlahProduk,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}
