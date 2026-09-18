package com.ptpws.ikikasir.feature.antrean.domain.model

import com.google.firebase.Timestamp

data class Antrean(
    val id: String = "",
    val transactionId: String = "",
    val queueSequence: Int = 0,
    val status: String = AntreanStatus.WAITING,
    val customerName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
)

object AntreanStatus {
    const val WAITING = "WAITING"
    const val DONE = "DONE"
    const val CANCELLED = "CANCELLED"
}
