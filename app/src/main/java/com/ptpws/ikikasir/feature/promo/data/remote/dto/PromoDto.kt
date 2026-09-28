package com.ptpws.ikikasir.feature.promo.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.model.PromoProductItem

/**
 * Firestore DTO for "promos" collection.
 * items field is stored as Array<Map> matching Firestore schema:
 *   items: [ { productId, productName, price, imageUrl }, ... ]
 */
data class PromoDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("nama") @set:PropertyName("nama") var nama: String = "",
    @get:PropertyName("tipePromo") @set:PropertyName("tipePromo") var tipePromo: String = "",
    @get:PropertyName("items") @set:PropertyName("items") var items: List<Map<String, Any>> = emptyList(),
    @get:PropertyName("diskonType") @set:PropertyName("diskonType") var diskonType: String = "Rp",
    @get:PropertyName("nilaiDiskon") @set:PropertyName("nilaiDiskon") var nilaiDiskon: Double = 0.0,
    @get:PropertyName("tanggalMulai") @set:PropertyName("tanggalMulai") var tanggalMulai: String = "",
    @get:PropertyName("tanggalBerakhir") @set:PropertyName("tanggalBerakhir") var tanggalBerakhir: String = "",
    @get:PropertyName("deskripsi") @set:PropertyName("deskripsi") var deskripsi: String = "",
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Timestamp = Timestamp.now(),
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Timestamp = Timestamp.now()
) {
    fun toDomain(): Promo {
        val domainItems = items.map { map ->
            PromoProductItem(
                productId = map["productId"]?.toString() ?: "",
                productName = map["productName"]?.toString() ?: "",
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                imageUrl = map["imageUrl"]?.toString() ?: ""
            )
        }
        return Promo(
            id = id,
            nama = nama,
            tipePromo = tipePromo,
            items = domainItems,
            diskonType = diskonType,
            nilaiDiskon = nilaiDiskon,
            tanggalMulai = tanggalMulai,
            tanggalBerakhir = tanggalBerakhir,
            deskripsi = deskripsi,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = true
        )
    }
}

fun Promo.toDto(): PromoDto {
    val itemsMapList = items.map { item ->
        mapOf(
            "productId" to item.productId,
            "productName" to item.productName,
            "price" to item.price,
            "imageUrl" to item.imageUrl
        )
    }
    return PromoDto(
        id = id,
        nama = nama,
        tipePromo = tipePromo,
        items = itemsMapList,
        diskonType = diskonType,
        nilaiDiskon = nilaiDiskon,
        tanggalMulai = tanggalMulai,
        tanggalBerakhir = tanggalBerakhir,
        deskripsi = deskripsi,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
