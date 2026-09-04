package com.ptpws.ikikasir.feature.kategori.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.feature.kategori.data.local.entity.KategoriEntity
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori

data class KategoriDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("nama") @set:PropertyName("nama")
    var nama: String = "",

    @get:PropertyName("deskripsi") @set:PropertyName("deskripsi")
    var deskripsi: String = "",

    @get:PropertyName("iconName") @set:PropertyName("iconName")
    var iconName: String = "Category",

    @get:PropertyName("colorHex") @set:PropertyName("colorHex")
    var colorHex: String = "#4F46E5",

    @get:PropertyName("jumlahProduk") @set:PropertyName("jumlahProduk")
    var jumlahProduk: Int = 0,

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = 0L,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Long = 0L
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
            isSynced = true
        )
    }

    fun toEntity(): KategoriEntity {
        return KategoriEntity(
            id = id,
            nama = nama,
            deskripsi = deskripsi,
            iconName = iconName,
            colorHex = colorHex,
            jumlahProduk = jumlahProduk,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = true,
            isDeleted = false
        )
    }
}

fun Kategori.toDto(): KategoriDto {
    return KategoriDto(
        id = id,
        nama = nama,
        deskripsi = deskripsi,
        iconName = iconName,
        colorHex = colorHex,
        jumlahProduk = jumlahProduk,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
