package com.ptpws.ikikasir.feature.bluetooth.domain.usecase

import com.ptpws.ikikasir.feature.bluetooth.domain.repository.BluetoothPrinterRepository
import javax.inject.Inject

class IsBluetoothEnabledUseCase @Inject constructor(
    private val repository: BluetoothPrinterRepository
) {
    operator fun invoke(): Boolean = repository.isBluetoothEnabled()
}
