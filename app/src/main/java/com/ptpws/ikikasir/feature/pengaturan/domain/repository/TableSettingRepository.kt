package com.ptpws.ikikasir.feature.pengaturan.domain.repository

import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting
import kotlinx.coroutines.flow.Flow

interface TableSettingRepository {
    fun getSetting(): Flow<TableSetting>
    suspend fun saveSetting(setting: TableSetting): Result<Unit>
    suspend fun syncPendingSetting(): Result<Unit>
}
