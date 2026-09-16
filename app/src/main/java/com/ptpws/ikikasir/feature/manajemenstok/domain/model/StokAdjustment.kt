package com.ptpws.ikikasir.feature.manajemenstok.domain.model

import com.google.firebase.Timestamp

/**
 * Tipe pergerakan stok.
 * IN = stok masuk (restock / pembelian)
 * OUT = stok keluar (penjualan / pengurangan manual)
 */
enum class MovementType {
    IN,
    OUT
}

data class StockMovement(
    val movementId: String = "",
    val productId: String = "",
    val productName: String = "",
    val barcode: String? = null,
    val type: MovementType = MovementType.IN,
    val quantity: Int = 0,
    val stockBefore: Int = 0,
    val stockAfter: Int = 0,
    val source: String = "",       // e.g., "MANUAL_RESTOCK", "MANUAL_UPDATE", "SALE"
    val userId: String = "",       // Firebase Auth User UID
    val createdBy: String = "",    // Nama kasir / pencatat
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = false
)
