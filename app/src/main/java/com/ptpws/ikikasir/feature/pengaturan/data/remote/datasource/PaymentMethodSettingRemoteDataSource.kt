package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.PaymentMethodSettingDto

interface PaymentMethodSettingRemoteDataSource {
    suspend fun getSetting(id: String = "default"): PaymentMethodSettingDto?
    suspend fun saveSetting(dto: PaymentMethodSettingDto)
}
