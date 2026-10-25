package com.ptpws.ikikasir.feature.catatankeuangan.domain.model

import com.google.firebase.Timestamp

data class CatatanKeuangan(
    val id: String = "",
    val title: String = "",
    val type: String = "PEMASUKAN", // "PEMASUKAN" or "PENGELUARAN"
    val amount: Double = 0.0,
    val date: String = "",
    val note: String = "",
    val paymentMethod: String = "Tunai",
    val proofImageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
) {
    val isPemasukan: Boolean get() = type.equals("PEMASUKAN", ignoreCase = true)
    val isPengeluaran: Boolean get() = type.equals("PENGELUARAN", ignoreCase = true)
}
