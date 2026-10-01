package com.ptpws.ikikasir.feature.bluetooth.domain.model

/**
 * Domain Model untuk perangkat printer Bluetooth.
 * - address: MAC address dari perangkat Bluetooth (unik identifier)
 * - name: Nama perangkat yang ditampilkan
 * - isPaired: Status paired dengan perangkat Android
 * - isConnected: Status koneksi aktif saat ini
 */
data class BluetoothPrinterDevice(
    val address: String,
    val name: String,
    val isPaired: Boolean = false,
    val isConnected: Boolean = false
)
