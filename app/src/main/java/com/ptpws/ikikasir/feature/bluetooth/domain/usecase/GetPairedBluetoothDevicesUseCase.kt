package com.ptpws.ikikasir.feature.bluetooth.domain.usecase

import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterDevice
import com.ptpws.ikikasir.feature.bluetooth.domain.repository.BluetoothPrinterRepository
import javax.inject.Inject

class GetPairedBluetoothDevicesUseCase @Inject constructor(
    private val repository: BluetoothPrinterRepository
) {
    operator fun invoke(): List<BluetoothPrinterDevice> {
        return repository.getPairedDevices()
    }
}
