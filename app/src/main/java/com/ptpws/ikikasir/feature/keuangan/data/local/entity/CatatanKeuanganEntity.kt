package com.ptpws.ikikasir.feature.keuangan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "catatan_keuangan")
data class CatatanKeuanganEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val jenis: String = "PEMASUKAN", // "PEMASUKAN" or "PENGELUARAN"
    val judul: String = "",
    val keterangan: String = "",
    val tanggalMillis: Long = System.currentTimeMillis(),
    val metodePembayaran: String = "Tunai", // "Tunai", "QRIS", "Kartu Debit", "Transfer"
    val nominal: Double = 0.0,
    val buktiUri: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)
