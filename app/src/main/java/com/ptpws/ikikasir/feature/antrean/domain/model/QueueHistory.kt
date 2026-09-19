package com.ptpws.ikikasir.feature.antrean.domain.model

import com.google.firebase.Timestamp
import java.util.UUID

data class QueueHistory(
    val id: String = UUID.randomUUID().toString(),
    val transactionId: String,
    val status: String, // "DONE", "CANCELLED"
    val customerName: String = "",
    val queueSequence: Int = 1,
    val completedAt: Timestamp = Timestamp.now(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)
