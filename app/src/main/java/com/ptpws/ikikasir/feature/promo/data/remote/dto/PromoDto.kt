package com.ptpws.ikikasir.feature.promo.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.model.PromoProductItem

/**
 * Firestore DTO for "promos" collection using English property names.
 */
data class PromoDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("promoType") @set:PropertyName("promoType") var promoType: String = "",
    @get:PropertyName("items") @set:PropertyName("items") var items: List<Map<String, Any>> = emptyList(),
    @get:PropertyName("discountType") @set:PropertyName("discountType") var discountType: String = "Rp",
    @get:PropertyName("discountValue") @set:PropertyName("discountValue") var discountValue: Double = 0.0,
    @get:PropertyName("startDate") @set:PropertyName("startDate") var startDate: String = "",
    @get:PropertyName("endDate") @set:PropertyName("endDate") var endDate: String = "",
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
            name = name,
            promoType = promoType,
            items = domainItems,
            discountType = discountType,
            discountValue = discountValue,
            startDate = startDate,
            endDate = endDate,
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
        name = name,
        promoType = promoType,
        items = itemsMapList,
        discountType = discountType,
        discountValue = discountValue,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
