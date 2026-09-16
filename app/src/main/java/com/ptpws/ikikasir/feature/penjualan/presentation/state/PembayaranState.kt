package com.ptpws.ikikasir.feature.penjualan.presentation.state

import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi

data class PembayaranState(
    val orderId: String = "#KP-2026-0891",
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val totalItemCount: Int = 0,
    val totalPcsCount: Int = 0,
    val metodePembayaran: String = "Tunai",
    val uangDiterimaText: String = "",
    val uangDiterima: Double = 0.0,
    val kembalian: Double = 0.0,
    val notes: String = "", // Catatan pesanan kasir/user
    val isCetakStrukOtomatis: Boolean = true,
    val isPrinterSiap: Boolean = true,
    val isRincianExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showFailedDialog: Boolean = false,
    val errorMessage: String = "",
    val transaksiSukses: PenjualanTransaksi? = null
)
