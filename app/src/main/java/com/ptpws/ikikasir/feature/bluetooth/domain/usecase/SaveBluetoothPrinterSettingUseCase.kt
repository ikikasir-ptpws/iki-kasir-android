package com.ptpws.ikikasir.feature.bluetooth.domain.usecase

import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import com.ptpws.ikikasir.feature.bluetooth.domain.repository.BluetoothPrinterRepository
import javax.inject.Inject

class SaveBluetoothPrinterSettingUseCase @Inject constructor(
    private val repository: BluetoothPrinterRepository
) {
    suspend operator fun invoke(setting: BluetoothPrinterSetting): Result<Unit> {
        return repository.saveBluetoothPrinterSetting(setting)
    }
}
