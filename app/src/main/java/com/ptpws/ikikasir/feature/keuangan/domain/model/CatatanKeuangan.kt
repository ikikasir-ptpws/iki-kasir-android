package com.ptpws.ikikasir.feature.keuangan.domain.model

import com.ptpws.ikikasir.feature.keuangan.data.local.entity.CatatanKeuanganEntity
import java.util.UUID

data class CatatanKeuangan(
    val id: String = UUID.randomUUID().toString(),
    val jenis: String = "PEMASUKAN", // "PEMASUKAN" or "PENGELUARAN"
    val judul: String = "",
    val keterangan: String = "",
    val tanggalMillis: Long = System.currentTimeMillis(),
    val metodePembayaran: String = "Tunai",
    val nominal: Double = 0.0,
    val buktiUri: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)

fun CatatanKeuanganEntity.toDomain(): CatatanKeuangan = CatatanKeuangan(
    id = id,
    jenis = jenis,
    judul = judul,
    keterangan = keterangan,
    tanggalMillis = tanggalMillis,
    metodePembayaran = metodePembayaran,
    nominal = nominal,
    buktiUri = buktiUri,
    createdAtMillis = createdAtMillis,
    isSynced = isSynced
)

fun CatatanKeuangan.toEntity(isSynced: Boolean = this.isSynced): CatatanKeuanganEntity = CatatanKeuanganEntity(
    id = id,
    jenis = jenis,
    judul = judul,
    keterangan = keterangan,
    tanggalMillis = tanggalMillis,
    metodePembayaran = metodePembayaran,
    nominal = nominal,
    buktiUri = buktiUri,
    createdAtMillis = createdAtMillis,
    isSynced = isSynced
)
