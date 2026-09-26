package com.ptpws.ikikasir.feature.antrean.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.ptpws.ikikasir.feature.antrean.data.local.entity.AntreanEntity
import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import com.ptpws.ikikasir.feature.antrean.domain.model.AntreanStatus

data class AntreanDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    @get:PropertyName("transactionId") @set:PropertyName("transactionId")
    var transactionId: String = "",

    @get:PropertyName("queueSequence") @set:PropertyName("queueSequence")
    var queueSequence: Int = 0,

    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = AntreanStatus.WAITING,

    @get:PropertyName("customerName") @set:PropertyName("customerName")
    var customerName: String = "",

    @get:PropertyName("tableNumber") @set:PropertyName("tableNumber")
    var tableNumber: String = "",

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    @get:ServerTimestamp
    var createdAt: Timestamp? = null,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @get:ServerTimestamp
    var updatedAt: Timestamp? = null
) {
    fun toDomain(): Antrean {
        return Antrean(
            id = id,
            transactionId = transactionId,
            queueSequence = queueSequence,
            status = status,
            customerName = customerName,
            tableNumber = tableNumber,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true
        )
    }

    fun toEntity(): AntreanEntity {
        return AntreanEntity(
            id = id,
            transactionId = transactionId,
            queueSequence = queueSequence,
            status = status,
            customerName = customerName,
            tableNumber = tableNumber,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true,
            isDeleted = false
        )
    }
}

fun Antrean.toDto(): AntreanDto {
    return AntreanDto(
        id = id,
        transactionId = transactionId,
        queueSequence = queueSequence,
        status = status,
        customerName = customerName,
        tableNumber = tableNumber,
        createdAt = null,  // Let @ServerTimestamp handle it
        updatedAt = null   // Let @ServerTimestamp handle it
    )
}
