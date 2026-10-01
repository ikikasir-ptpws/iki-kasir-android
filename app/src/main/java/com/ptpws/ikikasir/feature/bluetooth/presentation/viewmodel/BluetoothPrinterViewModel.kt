package com.ptpws.ikikasir.feature.bluetooth.presentation.viewmodel

import android.util.Log
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.bluetooth.data.BluetoothPrinterConnection
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterDevice
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import com.ptpws.ikikasir.feature.bluetooth.domain.usecase.GetBluetoothPrinterSettingUseCase
import com.ptpws.ikikasir.feature.bluetooth.domain.usecase.GetPairedBluetoothDevicesUseCase
import com.ptpws.ikikasir.feature.bluetooth.domain.usecase.IsBluetoothEnabledUseCase
import com.ptpws.ikikasir.feature.bluetooth.domain.usecase.SaveBluetoothPrinterSettingUseCase
import com.ptpws.ikikasir.feature.bluetooth.presentation.state.BluetoothPrinterState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "BluetoothPrinterVM"

@HiltViewModel
class BluetoothPrinterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getBluetoothPrinterSettingUseCase: GetBluetoothPrinterSettingUseCase,
    private val saveBluetoothPrinterSettingUseCase: SaveBluetoothPrinterSettingUseCase,
    private val getPairedBluetoothDevicesUseCase: GetPairedBluetoothDevicesUseCase,
    private val isBluetoothEnabledUseCase: IsBluetoothEnabledUseCase,
    private val logActivityUseCase: LogActivityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothPrinterState())
    val state: StateFlow<BluetoothPrinterState> = _state.asStateFlow()

    init {
        loadSetting()
    }

    fun loadSetting() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getBluetoothPrinterSettingUseCase().collect { setting ->
                val isEnabled = withContext(Dispatchers.IO) {
                    isBluetoothEnabledUseCase()
                }
                val savedAddr = setting.savedAddress
                val savedName = setting.savedName
                val selectedDevice = if (savedAddr.isNotBlank()) {
                    BluetoothPrinterDevice(
                        address = savedAddr,
                        name = savedName.ifBlank { savedAddr },
                        isPaired = true
                    )
                } else null

                _state.update {
                    it.copy(
                        setting = setting,
                        isBluetoothEnabled = isEnabled,
                        isLoading = false,
                        selectedDevice = selectedDevice
                    )
                }
                if (setting.isAutoConnect && _state.value.hasBluetoothPermission && selectedDevice != null) {
                    connectDevice(selectedDevice, showError = false)
                }
            }
        }
    }

    fun refreshPairedDevices() {
        if (!_state.value.hasBluetoothPermission) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val (devices, isEnabled) = withContext(Dispatchers.IO) {
                getPairedBluetoothDevicesUseCase() to isBluetoothEnabledUseCase()
            }
            _state.update {
                it.copy(
                    pairedDevices = devices,
                    isBluetoothEnabled = isEnabled,
                    connectedAddress = it.setting.savedAddress.takeIf { address ->
                        BluetoothPrinterConnection.isConnected(address)
                    },
                    isLoading = false
                )
            }
            Log.d(TAG, "Refreshed paired devices: ${devices.size} devices found")
        }
    }

    fun selectDevice(device: BluetoothPrinterDevice) {
        _state.update { it.copy(selectedDevice = device) }
        connectDevice(device)
    }

    private fun connectDevice(device: BluetoothPrinterDevice, showError: Boolean = true) {
        if (!_state.value.hasBluetoothPermission) return
        viewModelScope.launch {
            _state.update { it.copy(isConnecting = true, connectedAddress = null) }
            try {
                BluetoothPrinterConnection.connect(context, device.address)
                _state.update {
                    it.copy(
                        selectedDevice = device,
                        connectedAddress = device.address,
                        isConnecting = false
                    )
                }
            } catch (error: Exception) {
                _state.update { it.copy(connectedAddress = null, isConnecting = false) }
                Log.w(TAG, "Gagal menghubungkan printer ${device.name}: ${error.message}")
                if (showError) {
                    GlobalCrudResultDialog.failure(
                        error.message ?: "Gagal menghubungkan printer Bluetooth."
                    )
                }
            }
        }
    }

    fun saveSetting(setting: BluetoothPrinterSetting, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            if (setting.isAutoConnect && setting.savedAddress.isNotBlank() &&
                !BluetoothPrinterConnection.isConnected(setting.savedAddress)
            ) {
                try {
                    BluetoothPrinterConnection.connect(context, setting.savedAddress)
                } catch (error: Exception) {
                    _state.update { it.copy(isSaving = false, connectedAddress = null) }
                    GlobalCrudResultDialog.failure(
                        error.message ?: "Koneksi otomatis ke printer gagal."
                    )
                    onComplete(false)
                    return@launch
                }
            }
            val result = saveBluetoothPrinterSettingUseCase(setting)
            result.fold(
                onSuccess = {
                    if (setting.savedAddress.isBlank()) {
                        BluetoothPrinterConnection.disconnect()
                    }
                    _state.update { s ->
                        s.copy(
                            isSaving = false,
                            setting = setting,
                            connectedAddress = if (BluetoothPrinterConnection.isConnected(setting.savedAddress)) {
                                setting.savedAddress
                            } else null
                        )
                    }
                    GlobalCrudResultDialog.success("Pengaturan printer Bluetooth berhasil disimpan.")
                    logActivityUseCase(
                        title = "Pengaturan Printer Bluetooth",
                        description = "Printer ${setting.savedName} (${setting.savedAddress}) dipilih. " +
                            "Koneksi otomatis: ${setting.isAutoConnect}.",
                        category = "SYSTEM",
                        action = "UPDATE"
                    )
                    onComplete(true)
                },
                onFailure = { error ->
                    _state.update { it.copy(isSaving = false) }
                    GlobalCrudResultDialog.failure(
                        error.message ?: "Gagal menyimpan pengaturan printer Bluetooth."
                    )
                    onComplete(false)
                }
            )
        }
    }

    fun updatePermissionStatus(hasPermission: Boolean) {
        _state.update {
            it.copy(
                hasBluetoothPermission = hasPermission,
                pairedDevices = if (hasPermission) it.pairedDevices else emptyList()
            )
        }
        if (hasPermission) {
            refreshPairedDevices()
            val current = _state.value
            if (current.setting.isAutoConnect && current.selectedDevice != null) {
                connectDevice(current.selectedDevice, showError = false)
            }
        }
    }

}
