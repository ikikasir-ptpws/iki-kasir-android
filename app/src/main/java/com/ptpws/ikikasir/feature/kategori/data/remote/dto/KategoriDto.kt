package com.ptpws.ikikasir.feature.kategori.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.ptpws.ikikasir.feature.kategori.data.local.entity.KategoriEntity
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori

data class KategoriDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("iconUrl") @set:PropertyName("iconUrl")
    var iconUrl: String = "",

    @get:PropertyName("isVisibleInCashier") @set:PropertyName("isVisibleInCashier")
    var isVisibleInCashier: Boolean = true,

    @get:PropertyName("productCount") @set:PropertyName("productCount")
    var productCount: Int = 0,

    @get:PropertyName("lowStockCount") @set:PropertyName("lowStockCount")
    var lowStockCount: Int = 0,

    @get:PropertyName("outOfStockCount") @set:PropertyName("outOfStockCount")
    var outOfStockCount: Int = 0,

    // @ServerTimestamp: jika null saat write, Firestore otomatis isi dengan waktu server
    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    @get:ServerTimestamp
    var createdAt: Timestamp? = null,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @get:ServerTimestamp
    var updatedAt: Timestamp? = null
) {
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
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true
        )
    }

    fun toEntity(): KategoriEntity {
        return KategoriEntity(
            id = id,
            name = name,
            description = description,
            iconUrl = iconUrl,
            isVisibleInCashier = isVisibleInCashier,
            productCount = productCount,
            lowStockCount = lowStockCount,
            outOfStockCount = outOfStockCount,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true,
            isDeleted = false
        )
    }
}

/**
 * Konversi domain model ke DTO untuk dikirim ke Firestore.
 * createdAt/updatedAt di-set null agar @ServerTimestamp bekerja otomatis.
 */
fun Kategori.toDto(): KategoriDto {
    return KategoriDto(
        id = id,
        name = name,
        description = description,
        iconUrl = iconUrl,
        isVisibleInCashier = isVisibleInCashier,
        productCount = productCount,
        lowStockCount = lowStockCount,
        outOfStockCount = outOfStockCount,
        createdAt = null,
        updatedAt = null
    )
}