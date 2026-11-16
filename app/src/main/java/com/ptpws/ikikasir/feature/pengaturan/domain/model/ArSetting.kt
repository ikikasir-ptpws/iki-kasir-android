package com.ptpws.ikikasir.feature.pengaturan.domain.model

import com.google.firebase.Timestamp

/**
 * Domain model untuk Pengaturan AR Mode.
 * Clean Architecture — Domain Layer.
 * Mengontrol apakah fitur AR aktif saat scan produk.
 */
data class ArSetting(
    val id: String = "default",
    val isArEnabled: Boolean = false,
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
)
