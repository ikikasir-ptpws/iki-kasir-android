package com.ptpws.ikikasir.feature.penjualan.domain.model

import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting

data class PenjualanTransaksi(
    val transactionId: String = "",       // Invoice number sebagai ID transaksi
    val transactionNumber: String = "",   // Invoice number
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,     // Harga keseluruhan sebelum PPN
    val ppnPercentage: Double = 0.0, // Persentase PPN saat transaksi dibuat (misal 10.0)
    val ppnAmount: Double = 0.0,    // Nominal PPN (0 jika tidak aktif)
    val ppnType: String = "",       // "EXCLUSIVE", "INCLUSIVE", atau ""
    val discount: Double = 0.0,
    val total: Double = 0.0,        // Grand total (termasuk PPN jika eksklusif)
    val paymentMethod: String = "Tunai",
    val paymentAmount: Double = 0.0,
    val change: Double = 0.0,
    val status: String = "COMPLETED",
    val notes: String = "",               // Catatan pesanan kasir/user
    val createdBy: String = "",           // Nama kasir/user
    val customerName: String = "",        // Nama Pelanggan
    val tableNumber: String = "",         // Nomor Meja
    val queueSequence: Int = 1,           // Nomor Antrean Real
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = false
) {
    // Helper properties for UI compatibility
    val kodeTransaksi: String get() = transactionNumber
    val totalBayar: Double get() = paymentAmount
    val kembalian: Double get() = change
    val metodePembayaran: String get() = paymentMethod

    /**
     * Menentukan apakah transaksi ini menggunakan PPN eksklusif (belum termasuk PPN).
     * Jika ppnType EXCLUSIVE, atau jika ppnType masih kosong namun transaksi lama memiliki ppnAmount > 0.
     */
    val isPpnEksklusif: Boolean
        get() = ppnType.equals(TaxSetting.TAX_TYPE_EXCLUSIVE, ignoreCase = true) ||
                (ppnType.isBlank() && ppnAmount > 0)

    /**
     * Persentase efektif PPN untuk transaksi ini.
     */
    val effectivePpnPercentage: Double
        get() {
            if (ppnPercentage > 0) return ppnPercentage
            if (subtotal > 0 && ppnAmount > 0) {
                return (ppnAmount / subtotal) * 100.0
            }
            return 0.0
        }

    /**
     * Label format persentase PPN (misal "10%" atau "11%").
     */
    val formattedPpnPercentage: String
        get() {
            val p = effectivePpnPercentage
            return if (p % 1.0 == 0.0) "${p.toInt()}%" else "$p%"
        }
}
