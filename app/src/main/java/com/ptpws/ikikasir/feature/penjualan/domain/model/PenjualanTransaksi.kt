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

    val isPpnInklusif: Boolean
        get() = ppnType.equals(TaxSetting.TAX_TYPE_INCLUSIVE, ignoreCase = true)

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

    /**
     * Mengecek apakah PPN aktif untuk transaksi ini murni berdasarkan data transaksi saat dibuat.
     * Tidak terpengaruh oleh perubahan setting toko di kemudian hari.
     */
    val isPpnAktif: Boolean
        get() = ppnAmount > 0 || ppnPercentage > 0 || (ppnType.isNotBlank() && !ppnType.equals("NONE", ignoreCase = true))

    fun isTaxActive(taxSetting: TaxSetting? = null): Boolean {
        return isPpnAktif
    }

    /**
     * Menentukan apakah PPN eksklusif untuk transaksi ini.
     */
    fun checkIsPpnEksklusif(taxSetting: TaxSetting? = null): Boolean {
        if (ppnType.isNotBlank()) {
            return ppnType.equals(TaxSetting.TAX_TYPE_EXCLUSIVE, ignoreCase = true)
        }
        return ppnAmount > 0
    }

    /**
     * Mendapatkan persentase PPN (dari data transaksi yang tersimpan).
     */
    fun getEffectivePercentage(taxSetting: TaxSetting? = null): Double {
        if (!isPpnAktif) return 0.0
        if (ppnPercentage > 0) return ppnPercentage
        if (effectivePpnPercentage > 0) return effectivePpnPercentage
        return 0.0
    }

    /**
     * Format label persentase (misal "11%" atau "10%").
     */
    fun getFormattedPercentage(taxSetting: TaxSetting? = null): String {
        val p = getEffectivePercentage(taxSetting)
        return if (p % 1.0 == 0.0) "${p.toInt()}%" else "$p%"
    }

    /**
     * Menghitung nominal Rupiah PPN yang efektif dari data transaksi.
     */
    fun getEffectivePpnAmount(taxSetting: TaxSetting? = null): Double {
        if (!isPpnAktif) return 0.0
        if (ppnAmount > 0) return ppnAmount
        val percent = getEffectivePercentage(taxSetting)
        if (percent <= 0) return 0.0
        val baseSubtotal = if (subtotal > 0) subtotal else items.sumOf { it.totalPrice }
        val discounted = (baseSubtotal - discount).coerceAtLeast(0.0)
        val isEksklusif = checkIsPpnEksklusif(taxSetting)
        return if (isEksklusif) {
            discounted * (percent / 100.0)
        } else {
            discounted - (discounted / (1.0 + percent / 100.0))
        }
    }
}
