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
    val parsedDateMillis = try {
        if (date.isNotBlank()) {
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).parse(date)?.time
        } else null
    } catch (_: Exception) {
        null
    } ?: createdAt?.toDate()?.time ?: System.currentTimeMillis()

    return CatatanKeuangan(
        id = id,
        jenis = if (type.isNotBlank()) type else "PEMASUKAN",
        judul = title,
        keterangan = note,
        tanggalMillis = parsedDateMillis,
        metodePembayaran = paymentMethod,
        nominal = amount,
        buktiUri = proofImageUrl.ifBlank { null },
        createdAtMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis()
    )
}

fun CatatanKeuangan.toDto(): CatatanKeuanganDto {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(tanggalMillis))
    return CatatanKeuanganDto(
        id = id,
        amount = nominal,
        createdAt = null,
        date = dateStr,
        note = keterangan,
        paymentMethod = metodePembayaran,
        proofImageUrl = buktiUri.orEmpty(),
        title = judul,
        type = jenis,
        updatedAt = null
    )
}
