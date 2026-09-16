package com.ptpws.ikikasir.feature.manajemenstok.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.ptpws.ikikasir.feature.manajemenstok.data.local.entity.StockMovementEntity
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.MovementType
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.StockMovement

/**
 * Firestore DTO untuk koleksi "stock_movements".
 * Struktur field sesuai schema:
 *  - productId, productName, barcode, type ("IN"/"OUT"),
 *    quantity, stockBefore, stockAfter, source,
 *    userId (User UID dari Firebase Auth), createdBy (Nama Kasir), createdAt, updatedAt
 */
data class StockMovementDto(
    @get:Exclude @set:Exclude
    var movementId: String = "",

    @get:PropertyName("productId") @set:PropertyName("productId")
    var productId: String = "",

    @get:PropertyName("productName") @set:PropertyName("productName")
    var productName: String = "",

    @get:PropertyName("barcode") @set:PropertyName("barcode")
    var barcode: String? = null,

    /** "IN" or "OUT" */
    @get:PropertyName("type") @set:PropertyName("type")
    var type: String = "IN",

    @get:PropertyName("quantity") @set:PropertyName("quantity")
    var quantity: Int = 0,

    @get:PropertyName("stockBefore") @set:PropertyName("stockBefore")
    var stockBefore: Int = 0,

    @get:PropertyName("stockAfter") @set:PropertyName("stockAfter")
    var stockAfter: Int = 0,

    /** Source of movement: "MANUAL_RESTOCK", "MANUAL_UPDATE", "SALE", etc. */
    @get:PropertyName("source") @set:PropertyName("source")
    var source: String = "",

    /** User UID dari profile Firebase Auth */
    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",

    /** Nama kasir yang mencatat pergerakan stok */
    @get:PropertyName("createdBy") @set:PropertyName("createdBy")
    var createdBy: String = "",

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    @get:ServerTimestamp
    var createdAt: Timestamp? = null,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @get:ServerTimestamp
    var updatedAt: Timestamp? = null
) {
    fun toEntity(): StockMovementEntity {
        return StockMovementEntity(
            movementId = movementId,
            productId = productId,
            productName = productName,
            barcode = barcode,
            type = type,
            quantity = quantity,
            stockBefore = stockBefore,
            stockAfter = stockAfter,
            source = source,
            userId = userId,
            createdBy = createdBy,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true
        )
    }
}

fun StockMovement.toDto(): StockMovementDto {
    return StockMovementDto(
        movementId = movementId,
        productId = productId,
        productName = productName,
        barcode = barcode,
        type = type.name,           // "IN" or "OUT"
        quantity = quantity,
        stockBefore = stockBefore,
        stockAfter = stockAfter,
        source = source,
        userId = userId,
        createdBy = createdBy,
        createdAt = null,           // let Firestore @ServerTimestamp handle
        updatedAt = null
    )
}
