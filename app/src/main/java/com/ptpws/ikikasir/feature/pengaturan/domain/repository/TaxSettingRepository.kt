package com.ptpws.ikikasir.feature.pengaturan.domain.repository

import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import kotlinx.coroutines.flow.Flow

interface TaxSettingRepository {
    fun getTaxSetting(): Flow<TaxSetting>
    suspend fun saveTaxSetting(setting: TaxSetting): Flow<Result<Unit>>
    suspend fun syncPendingTaxSetting(): Flow<Result<Unit>>
}
