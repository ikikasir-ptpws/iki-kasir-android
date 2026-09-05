package com.ptpws.ikikasir.feature.kategori.data.remote.dto

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.feature.kategori.data.local.entity.KategoriEntity
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori

data class KategoriDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("icon") @set:PropertyName("icon")
    var icon: String = "Restaurant",

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = 0L,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Long = 0L
) {
    fun toDomain(): Kategori {
        return Kategori(
            id = id,
            name = name,
            description = description,
            icon = icon,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = true
        )
    }

    fun toEntity(): KategoriEntity {
        return KategoriEntity(
            id = id,
            name = name,
            description = description,
            icon = icon,
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
        name = name,
        description = description,
        icon = icon,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
