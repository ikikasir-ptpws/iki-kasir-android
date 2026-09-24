package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.NotaSettingDto

interface NotaSettingRemoteDataSource {
    suspend fun getNotaSetting(id: String = "default_nota_setting"): NotaSettingDto?
    suspend fun saveNotaSetting(setting: NotaSettingDto)
}
