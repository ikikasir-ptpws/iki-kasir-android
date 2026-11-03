package com.ptpws.ikikasir.feature.keuangan.data.remote.dto

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.feature.keuangan.domain.model.CatatanKeuangan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CatatanKeuanganDto(
    @DocumentId
    val id: String = "",
    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",
    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = "PEMASUKAN",
    @get:PropertyName("amount")
    @set:PropertyName("amount")
    var amount: Double = 0.0,
    @get:PropertyName("date")
    @set:PropertyName("date")
    var date: String = "",
    @get:PropertyName("note")
    @set:PropertyName("note")
    var note: String = "",
    @get:PropertyName("paymentMethod")
    @set:PropertyName("paymentMethod")
    var paymentMethod: String = "Tunai",
    @get:PropertyName("proofImageUrl")
    @set:PropertyName("proofImageUrl")
    var proofImageUrl: String? = null,
    @get:PropertyName("tanggalMillis")
    @set:PropertyName("tanggalMillis")
    var tanggalMillis: Long = System.currentTimeMillis(),
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAt: Long = System.currentTimeMillis()
)

fun CatatanKeuanganDto.toDomain(): CatatanKeuangan = CatatanKeuangan(
    id = id,
    jenis = if (type.isNotBlank()) type else "PEMASUKAN",
    judul = title,
    keterangan = note,
    tanggalMillis = if (tanggalMillis > 0) tanggalMillis else createdAt,
    metodePembayaran = paymentMethod,
    nominal = amount,
    buktiUri = proofImageUrl,
    createdAtMillis = createdAt
)

fun CatatanKeuangan.toDto(): CatatanKeuanganDto {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(tanggalMillis))
    return CatatanKeuanganDto(
        id = id,
        title = judul,
        type = jenis,
        amount = nominal,
        date = dateStr,
        note = keterangan,
        paymentMethod = metodePembayaran,
        proofImageUrl = buktiUri,
        tanggalMillis = tanggalMillis,
        createdAt = createdAtMillis,
        updatedAt = System.currentTimeMillis()
    )
}
