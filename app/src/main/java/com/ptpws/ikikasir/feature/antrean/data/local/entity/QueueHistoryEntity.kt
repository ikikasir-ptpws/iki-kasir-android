package com.ptpws.ikikasir.feature.antrean.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory

@Entity(tableName = "queue_history")
data class QueueHistoryEntity(
    @PrimaryKey
    val id: String,
    val transactionId: String,
    val status: String,
    val customerName: String,
    val queueSequence: Int,
    val completedAt: Timestamp,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
) {
    fun toDomain(): QueueHistory {
        return QueueHistory(
            id = id,
            transactionId = transactionId,
            status = status,
            customerName = customerName,
            queueSequence = queueSequence,
            completedAt = completedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced,
            isDeleted = isDeleted
        )
    }
}

fun QueueHistory.toEntity(isSynced: Boolean = this.isSynced): QueueHistoryEntity {
    return QueueHistoryEntity(
        id = id,
        transactionId = transactionId,
        status = status,
        customerName = customerName,
        queueSequence = queueSequence,
        completedAt = completedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}
