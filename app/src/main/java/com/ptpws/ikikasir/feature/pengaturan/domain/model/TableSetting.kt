package com.ptpws.ikikasir.feature.pengaturan.domain.model

import com.google.firebase.Timestamp

/**
 * Domain model untuk Pengaturan Nomor Meja.
 * Clean Architecture — Domain Layer.
 * Mengontrol apakah fitur input nomor meja aktif saat transaksi.
 */
data class TableSetting(
    val id: String = "default",
    val isTableEnabled: Boolean = true,
    val updatedAt: Timestamp = Timestamp.now()
)

