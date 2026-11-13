package com.ptpws.ikikasir.feature.bluetooth.presentation.viewmodel

import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterDevice
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import com.ptpws.ikikasir.feature.bluetooth.presentation.state.BluetoothPrinterState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk BluetoothPrinterUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface BluetoothPrinterUiProvider {
    val state: StateFlow<BluetoothPrinterState>
    fun refreshPairedDevices()
    fun selectDevice(device: BluetoothPrinterDevice)
    fun saveSetting(setting: BluetoothPrinterSetting, onComplete: (Boolean) -> Unit = {})
    fun updatePermissionStatus(hasPermission: Boolean)
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke BluetoothPrinterViewModel asli.
 */
class HiltBluetoothPrinterUiProvider(private val viewModel: BluetoothPrinterViewModel) : BluetoothPrinterUiProvider {
    override val state: StateFlow<BluetoothPrinterState> get() = viewModel.state
    override fun refreshPairedDevices() = viewModel.refreshPairedDevices()
    override fun selectDevice(device: BluetoothPrinterDevice) = viewModel.selectDevice(device)
    override fun saveSetting(setting: BluetoothPrinterSetting, onComplete: (Boolean) -> Unit) = viewModel.saveSetting(setting, onComplete)
    override fun updatePermissionStatus(hasPermission: Boolean) = viewModel.updatePermissionStatus(hasPermission)
}
