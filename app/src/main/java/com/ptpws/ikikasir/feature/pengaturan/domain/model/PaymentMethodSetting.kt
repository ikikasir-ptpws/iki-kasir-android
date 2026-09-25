package com.ptpws.ikikasir.feature.pengaturan.domain.model

import com.google.firebase.Timestamp

/**
 * Domain model untuk Pengaturan Metode Pembayaran.
 * Clean Architecture — Domain Layer.
 * Mengontrol metode pembayaran mana saja yang aktif saat transaksi:
 * - Tunai
 * - QRIS
 * - Transfer Bank
 * - Kartu Kredit / Debit
 */
data class PaymentMethodSetting(
    val id: String = "default",
    val isTunaiEnabled: Boolean = true,
    val isQrisEnabled: Boolean = true,
    val isTransferEnabled: Boolean = true,
    val isKartuKreditEnabled: Boolean = true,
    val updatedAt: Timestamp = Timestamp.now()
)
