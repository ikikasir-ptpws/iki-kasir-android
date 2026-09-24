package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.TaxSettingDto

interface TaxSettingRemoteDataSource {
    suspend fun getTaxSetting(): TaxSettingDto?
    suspend fun saveTaxSetting(dto: TaxSettingDto)
}
