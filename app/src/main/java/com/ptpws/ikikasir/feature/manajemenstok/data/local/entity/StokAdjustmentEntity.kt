package com.ptpws.ikikasir.feature.manajemenstok.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.MovementType
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.StockMovement

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey
    @ColumnInfo(name = "movementId")
    val movementId: String,

    @ColumnInfo(name = "productId")
    val productId: String,

    @ColumnInfo(name = "productName")
    val productName: String,

    @ColumnInfo(name = "barcode")
    val barcode: String? = null,

    /** Stored as "IN" or "OUT" */
    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "stockBefore")
    val stockBefore: Int,

    @ColumnInfo(name = "stockAfter")
    val stockAfter: Int,

    /** Source of the movement, e.g. "MANUAL_RESTOCK", "MANUAL_UPDATE", "SALE" */
    @ColumnInfo(name = "source")
    val source: String = "",

    /** Nama kasir yang melakukan perubahan */
    @ColumnInfo(name = "userId")
    val userId: String = "",

    @ColumnInfo(name = "createdBy")
    val createdBy: String = "",

    @ColumnInfo(name = "createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false
) {
    fun toDomain(): StockMovement {
        return StockMovement(
            movementId = movementId,
            productId = productId,
            productName = productName,
            barcode = barcode,
            type = MovementType.valueOf(type),
            quantity = quantity,
            stockBefore = stockBefore,
            stockAfter = stockAfter,
            source = source,
            userId = userId,
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }
}

fun StockMovement.toEntity(isSynced: Boolean = false): StockMovementEntity {
    return StockMovementEntity(
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
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}
