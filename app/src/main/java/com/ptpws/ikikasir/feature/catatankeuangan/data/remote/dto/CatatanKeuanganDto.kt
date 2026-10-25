package com.ptpws.ikikasir.feature.catatankeuangan.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.core.storage.domain.model.isLocalImageUri
import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan

data class CatatanKeuanganDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "PEMASUKAN",
    @get:PropertyName("amount") @set:PropertyName("amount") var amount: Double = 0.0,
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("note") @set:PropertyName("note") var note: String = "",
    @get:PropertyName("paymentMethod") @set:PropertyName("paymentMethod") var paymentMethod: String = "Tunai",
    @get:PropertyName("proofImageUrl") @set:PropertyName("proofImageUrl") var proofImageUrl: String = "",
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Timestamp? = null,
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Timestamp? = null
) {
    fun toDomain(): CatatanKeuangan = CatatanKeuangan(
        id = id,
        title = title,
        type = type,
        amount = amount,
        date = date,
        note = note,
        paymentMethod = paymentMethod,
        proofImageUrl = proofImageUrl.takeUnless(::isLocalImageUri).orEmpty(),
        createdAt = createdAt ?: Timestamp.now(),
        updatedAt = updatedAt ?: Timestamp.now(),
        isSynced = true
    )
}

fun CatatanKeuangan.toDto(): CatatanKeuanganDto = CatatanKeuanganDto(
    id = id,
    title = title,
    type = type,
    amount = amount,
    date = date,
    note = note,
    paymentMethod = paymentMethod,
    proofImageUrl = proofImageUrl.takeUnless(::isLocalImageUri).orEmpty(),
    createdAt = createdAt,
    updatedAt = updatedAt
)
