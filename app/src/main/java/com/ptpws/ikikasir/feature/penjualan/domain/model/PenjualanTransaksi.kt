package com.ptpws.ikikasir.feature.penjualan.domain.model

data class PenjualanTransaksi(
    val id: String = "",
    val kodeTransaksi: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val totalBayar: Double = 0.0,
    val kembalian: Double = 0.0,
    val metodePembayaran: String = "Tunai",
    val timestamp: Long = System.currentTimeMillis()
)
