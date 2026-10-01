package com.ptpws.ikikasir.feature.bluetooth.presentation.state

import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterDevice
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting

data class BluetoothPrinterState(
    val setting: BluetoothPrinterSetting = BluetoothPrinterSetting(),
    val pairedDevices: List<BluetoothPrinterDevice> = emptyList(),
    val isBluetoothEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isConnecting: Boolean = false,
    val hasBluetoothPermission: Boolean = false,
    val selectedDevice: BluetoothPrinterDevice? = null,
    val connectedAddress: String? = null
)
