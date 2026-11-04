package com.ptpws.ikikasir.feature.keuangan.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.ptpws.ikikasir.feature.keuangan.domain.model.CatatanKeuangan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CatatanKeuanganDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("amount") @set:PropertyName("amount")
    var amount: Double = 0.0,

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    @get:ServerTimestamp
    var createdAt: Timestamp? = null,

    @get:PropertyName("date") @set:PropertyName("date")
    var date: String = "",

    @get:PropertyName("note") @set:PropertyName("note")
    var note: String = "",

    @get:PropertyName("paymentMethod") @set:PropertyName("paymentMethod")
    var paymentMethod: String = "Tunai",

    @get:PropertyName("proofImageUrl") @set:PropertyName("proofImageUrl")
    var proofImageUrl: String = "",

    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("type") @set:PropertyName("type")
    var type: String = "PEMASUKAN",

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @get:ServerTimestamp
    var updatedAt: Timestamp? = null
)

fun CatatanKeuanganDto.toDomain(): CatatanKeuangan {
    val createdTime = createdAt?.toDate()?.time ?: updatedAt?.toDate()?.time ?: 0L

    val timeInMillis = if (createdTime > 0L) {
        createdTime
    } else {
        try {
            if (date.isNotBlank()) {
                SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).parse(date)?.time ?: System.currentTimeMillis()
            } else System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    return CatatanKeuangan(
        id = id,
        jenis = if (type.isNotBlank()) type else "PEMASUKAN",
        judul = title,
        keterangan = note,
        tanggalMillis = timeInMillis,
        metodePembayaran = paymentMethod,
        nominal = amount,
        buktiUri = proofImageUrl.ifBlank { null },
        createdAtMillis = if (createdTime > 0L) createdTime else timeInMillis
    )
}

fun CatatanKeuangan.toDto(): CatatanKeuanganDto {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(tanggalMillis))
    val createdTimestamp = if (createdAtMillis > 0L) Timestamp(Date(createdAtMillis)) else Timestamp(Date(tanggalMillis))

    return CatatanKeuanganDto(
        id = id,
        amount = nominal,
        createdAt = createdTimestamp,
        date = dateStr,
        note = keterangan,
        paymentMethod = metodePembayaran,
        proofImageUrl = buktiUri.orEmpty(),
        title = judul,
        type = jenis,
        updatedAt = Timestamp.now()
    )
}
