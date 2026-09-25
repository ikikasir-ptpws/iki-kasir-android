package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.PaymentMethodSettingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPaymentMethodSettingUseCase @Inject constructor(
    private val repository: PaymentMethodSettingRepository
) {
    operator fun invoke(): Flow<PaymentMethodSetting> = repository.getSetting()
}
