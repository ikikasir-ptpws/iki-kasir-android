package com.ptpws.ikikasir.feature.bluetooth.domain.repository

import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterDevice
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import kotlinx.coroutines.flow.Flow

interface BluetoothPrinterRepository {
    fun getBluetoothPrinterSetting(): Flow<BluetoothPrinterSetting>
    suspend fun saveBluetoothPrinterSetting(setting: BluetoothPrinterSetting): Result<Unit>
    fun isBluetoothEnabled(): Boolean
    fun getPairedDevices(): List<BluetoothPrinterDevice>
}
