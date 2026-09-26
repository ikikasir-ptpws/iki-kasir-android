package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.TableSettingDto

interface TableSettingRemoteDataSource {
    suspend fun getSetting(id: String = "default"): TableSettingDto?
    suspend fun saveSetting(dto: TableSettingDto)
}
