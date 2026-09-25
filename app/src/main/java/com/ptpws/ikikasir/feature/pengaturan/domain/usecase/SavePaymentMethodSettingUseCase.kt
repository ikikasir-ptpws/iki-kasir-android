package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.PaymentMethodSettingRepository
import javax.inject.Inject

class SavePaymentMethodSettingUseCase @Inject constructor(
    private val repository: PaymentMethodSettingRepository
) {
    suspend operator fun invoke(setting: PaymentMethodSetting): Result<Unit> = repository.saveSetting(setting)
}
