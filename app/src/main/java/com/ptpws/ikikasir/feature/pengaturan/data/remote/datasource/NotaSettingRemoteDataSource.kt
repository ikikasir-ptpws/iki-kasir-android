package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.NotaSettingDto
import kotlinx.coroutines.flow.Flow

interface NotaSettingRemoteDataSource {
    fun getNotaSettingFlow(id: String = "default_nota_setting"): Flow<NotaSettingDto?>
    suspend fun getNotaSetting(id: String = "default_nota_setting"): NotaSettingDto?
    suspend fun saveNotaSetting(setting: NotaSettingDto)
}
