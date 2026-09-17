package com.ptpws.ikikasir.feature.penjualan.domain.model

import com.google.firebase.Timestamp

data class PenjualanTransaksi(
    val transactionId: String = "",       // Invoice number sebagai ID transaksi
    val transactionNumber: String = "",   // Invoice number
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: String = "Tunai",
    val paymentAmount: Double = 0.0,
    val change: Double = 0.0,
    val status: String = "COMPLETED",
    val notes: String = "",               // Catatan pesanan kasir/user
    val createdBy: String = "",           // Nama kasir/user
    val customerName: String = "",        // Nama Pelanggan
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = false
) {
    // Helper properties for UI compatibility
    val kodeTransaksi: String get() = transactionNumber
    val totalBayar: Double get() = paymentAmount
    val kembalian: Double get() = change
    val metodePembayaran: String get() = paymentMethod
}
