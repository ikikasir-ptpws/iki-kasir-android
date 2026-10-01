package com.ptpws.ikikasir.feature.bluetooth.data.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.ptpws.ikikasir.feature.bluetooth.data.preferences.BluetoothPrinterPreferences
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterDevice
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import com.ptpws.ikikasir.feature.bluetooth.domain.repository.BluetoothPrinterRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BluetoothPrinterRepo"

@Singleton
class BluetoothPrinterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: BluetoothPrinterPreferences
) : BluetoothPrinterRepository {

    private val _settingFlow = MutableStateFlow(preferences.getSetting())

    override fun getBluetoothPrinterSetting(): Flow<BluetoothPrinterSetting> {
        // Reload from prefs to ensure freshness
        _settingFlow.value = preferences.getSetting()
        return _settingFlow.asStateFlow()
    }

    override suspend fun saveBluetoothPrinterSetting(setting: BluetoothPrinterSetting): Result<Unit> {
        return try {
            preferences.saveSetting(setting)
            _settingFlow.value = setting
            Log.d(TAG, "Bluetooth printer setting saved: ${setting.savedName} (${setting.savedAddress})")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save bluetooth printer setting: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun isBluetoothEnabled(): Boolean {
        return try {
            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bluetoothManager?.adapter?.isEnabled == true
        } catch (e: SecurityException) {
            Log.w(TAG, "Tidak dapat memeriksa status Bluetooth tanpa izin: ${e.message}")
            false
        }
    }

    override fun getPairedDevices(): List<BluetoothPrinterDevice> {
        return try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

            if (bluetoothAdapter == null || !isBluetoothEnabled()) {
                Log.w(TAG, "Bluetooth tidak aktif atau tidak tersedia")
                return emptyList()
            }

            // Check permission for Android 12+ (API 31+)
            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED
            }

            if (!hasPermission) {
                Log.w(TAG, "Izin Bluetooth tidak diberikan")
                return emptyList()
            }

            @Suppress("MissingPermission")
            bluetoothAdapter.bondedDevices?.map { device ->
                BluetoothPrinterDevice(
                    address = device.address,
                    name = device.name ?: "Unknown Device",
                    isPaired = true,
                    isConnected = false
                )
            } ?: emptyList()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException saat mengambil paired devices: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error saat mengambil paired devices: ${e.message}")
            emptyList()
        }
    }
}
