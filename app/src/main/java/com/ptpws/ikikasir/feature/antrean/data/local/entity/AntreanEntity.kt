package com.ptpws.ikikasir.feature.antrean.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import com.ptpws.ikikasir.feature.antrean.domain.model.AntreanStatus

@Entity(tableName = "queues")
data class AntreanEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "transactionId")
    val transactionId: String,

    @ColumnInfo(name = "queueSequence")
    val queueSequence: Int,

    @ColumnInfo(name = "status")
    val status: String = AntreanStatus.WAITING,

    @ColumnInfo(name = "customerName")
    val customerName: String = "",

    @ColumnInfo(name = "createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true,

    @ColumnInfo(name = "isDeleted")
    val isDeleted: Boolean = false
) {
    fun toDomain(): Antrean {
        return Antrean(
            id = id,
            transactionId = transactionId,
            queueSequence = queueSequence,
            status = status,
            customerName = customerName,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }
}

fun Antrean.toEntity(isSynced: Boolean = true, isDeleted: Boolean = false): AntreanEntity {
    return AntreanEntity(
        id = id,
        transactionId = transactionId,
        queueSequence = queueSequence,
        status = status,
        customerName = customerName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}
