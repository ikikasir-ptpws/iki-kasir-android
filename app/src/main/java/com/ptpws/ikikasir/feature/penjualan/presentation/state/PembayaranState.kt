package com.ptpws.ikikasir.feature.penjualan.presentation.state

import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi

enum class PrinterStatus(
    val label: String,
    val dotColor: Long,
    val bgColor: Long
) {
    SIAP("Printer Siap", 0xFF16A34A, 0xFFDCFCE7),
    CONNECTING("Menghubungkan...", 0xFFD97706, 0xFFFEF3C7),
    BELUM_TERHUBUNG("Belum Terhubung", 0xFF64748B, 0xFFF1F5F9),
    BELUM_DIATUR("Belum Disetel", 0xFF64748B, 0xFFF1F5F9),
    BLUETOOTH_MATI("Bluetooth Mati", 0xFFDC2626, 0xFFFEE2E2)
}

data class PembayaranState(
    val orderId: String = "#KP-2026-0891",
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val totalItemCount: Int = 0,
    val totalPcsCount: Int = 0,
    val taxSetting: TaxSetting = TaxSetting(),
    val paymentMethodSetting: PaymentMethodSetting = PaymentMethodSetting(),
    val tableSetting: com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting = com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting(),
    val ppnAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val ppnLabel: String = "",
    val metodePembayaran: String = "Tunai",
    val uangDiterimaText: String = "",
    val uangDiterima: Double = 0.0,
    val kembalian: Double = 0.0,
    val notes: String = "", // Catatan pesanan kasir/user
    val customerName: String = "", // Nama Pelanggan (Opsional)
    val tableNumber: String = "", // Nomor Meja (Opsional)
    val isCetakStrukOtomatis: Boolean = false,
    val printerStatus: PrinterStatus = PrinterStatus.BELUM_TERHUBUNG,
    val printerName: String = "",
    val isPrinterSiap: Boolean = false,
    val isRincianExpanded: Boolean = false,
    val activePromos: List<com.ptpws.ikikasir.feature.promo.domain.model.Promo> = emptyList(),
    val selectedPromo: com.ptpws.ikikasir.feature.promo.domain.model.Promo? = null,
    val promoDiscountAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showFailedDialog: Boolean = false,
    val errorMessage: String = "",
    val transaksiSukses: PenjualanTransaksi? = null
)
