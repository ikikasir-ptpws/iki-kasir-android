package com.ptpws.ikikasir.feature.catatankeuangan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan

@Entity(tableName = "catatan_keuangan")
data class CatatanKeuanganEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val amount: Double,
    val date: String,
    val note: String,
    val paymentMethod: String = "Tunai",
    val proofImageUrl: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = true,
    val isDeleted: Boolean = false
) {
    fun toDomain(): CatatanKeuangan = CatatanKeuangan(
        id = id,
        title = title,
        type = type,
        amount = amount,
        date = date,
        note = note,
        paymentMethod = paymentMethod,
        proofImageUrl = proofImageUrl,
        createdAt = Timestamp(createdAt / 1000, ((createdAt % 1000) * 1000000).toInt()),
        updatedAt = Timestamp(updatedAt / 1000, ((updatedAt % 1000) * 1000000).toInt()),
        isSynced = isSynced
    )
}

fun CatatanKeuangan.toEntity(isSynced: Boolean = true, isDeleted: Boolean = false): CatatanKeuanganEntity =
    CatatanKeuanganEntity(
        id = id,
        title = title,
        type = type,
        amount = amount,
        date = date,
        note = note,
        paymentMethod = paymentMethod,
        proofImageUrl = proofImageUrl,
        createdAt = createdAt.seconds * 1000 + createdAt.nanoseconds / 1000000,
        updatedAt = updatedAt.seconds * 1000 + updatedAt.nanoseconds / 1000000,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
