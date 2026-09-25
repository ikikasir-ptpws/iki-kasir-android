package com.ptpws.ikikasir.feature.pengaturan.domain.repository

import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import kotlinx.coroutines.flow.Flow

interface PaymentMethodSettingRepository {
    fun getSetting(): Flow<PaymentMethodSetting>
    suspend fun saveSetting(setting: PaymentMethodSetting): Result<Unit>
    suspend fun syncPendingSetting(): Result<Unit>
}
