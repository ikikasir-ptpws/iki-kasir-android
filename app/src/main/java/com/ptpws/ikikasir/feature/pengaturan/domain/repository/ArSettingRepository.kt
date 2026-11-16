package com.ptpws.ikikasir.feature.pengaturan.domain.repository

import com.ptpws.ikikasir.feature.pengaturan.domain.model.ArSetting
import kotlinx.coroutines.flow.Flow

interface ArSettingRepository {
    fun getSetting(): Flow<ArSetting>
    suspend fun saveSetting(setting: ArSetting): Result<Unit>
    suspend fun syncPendingSetting(): Result<Unit>
}
