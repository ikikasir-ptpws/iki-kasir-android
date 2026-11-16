package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.ArSettingDto

interface ArSettingRemoteDataSource {
    suspend fun getSetting(id: String = "default"): ArSettingDto?
    suspend fun saveSetting(dto: ArSettingDto)
}
