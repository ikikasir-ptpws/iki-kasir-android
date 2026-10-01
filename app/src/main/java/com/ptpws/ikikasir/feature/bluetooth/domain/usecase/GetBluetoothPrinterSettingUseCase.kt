package com.ptpws.ikikasir.feature.bluetooth.domain.usecase

import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import com.ptpws.ikikasir.feature.bluetooth.domain.repository.BluetoothPrinterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBluetoothPrinterSettingUseCase @Inject constructor(
    private val repository: BluetoothPrinterRepository
) {
    operator fun invoke(): Flow<BluetoothPrinterSetting> {
        return repository.getBluetoothPrinterSetting()
    }
}
