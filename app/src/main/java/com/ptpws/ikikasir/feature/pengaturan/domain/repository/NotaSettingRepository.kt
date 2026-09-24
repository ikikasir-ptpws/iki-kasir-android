package com.ptpws.ikikasir.feature.pengaturan.domain.repository

import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import kotlinx.coroutines.flow.Flow

interface NotaSettingRepository {
    fun getNotaSetting(): Flow<NotaSetting>
    suspend fun saveNotaSetting(setting: NotaSetting): Flow<Result<Unit>>
    suspend fun syncPendingNotaSetting(): Flow<Result<Unit>>
}
