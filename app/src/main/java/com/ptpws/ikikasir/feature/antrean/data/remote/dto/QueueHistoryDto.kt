package com.ptpws.ikikasir.feature.antrean.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.ptpws.ikikasir.feature.antrean.data.local.entity.QueueHistoryEntity
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory

data class QueueHistoryDto(
    @DocumentId
    var id: String = "",
    var transactionId: String = "",
    var status: String = "",
    var customerName: String = "",
    var tableNumber: String = "",
    var queueSequence: Int = 1,
    @ServerTimestamp
    var completedAt: Timestamp? = null,
    @ServerTimestamp
    var createdAt: Timestamp? = null,
    @ServerTimestamp
    var updatedAt: Timestamp? = null
) {
    fun toEntity(isSynced: Boolean = true): QueueHistoryEntity {
        val now = Timestamp.now()
        val effectiveId = id.ifBlank { transactionId.ifBlank { java.util.UUID.randomUUID().toString() } }
        val effectiveTxId = transactionId.ifBlank { effectiveId }
        return QueueHistoryEntity(
            id = effectiveId,
            transactionId = effectiveTxId,
            status = status,
            customerName = customerName,
            tableNumber = tableNumber,
            queueSequence = queueSequence,
            completedAt = completedAt ?: now,
            createdAt = createdAt ?: now,
            updatedAt = updatedAt ?: now,
            isSynced = isSynced,
            isDeleted = false
        )
    }

    fun toDomain(): QueueHistory {
        val now = Timestamp.now()
        val effectiveId = id.ifBlank { transactionId }
        val effectiveTxId = transactionId.ifBlank { effectiveId }
        return QueueHistory(
            id = effectiveId,
            transactionId = effectiveTxId,
            status = status,
            customerName = customerName,
            tableNumber = tableNumber,
            queueSequence = queueSequence,
            completedAt = completedAt ?: now,
            createdAt = createdAt ?: now,
            updatedAt = updatedAt ?: now,
            isSynced = true,
            isDeleted = false
        )
    }
}

fun QueueHistory.toDto(): QueueHistoryDto {
    return QueueHistoryDto(
        id = id,
        transactionId = transactionId,
        status = status,
        customerName = customerName,
        tableNumber = tableNumber,
        queueSequence = queueSequence,
        completedAt = completedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
