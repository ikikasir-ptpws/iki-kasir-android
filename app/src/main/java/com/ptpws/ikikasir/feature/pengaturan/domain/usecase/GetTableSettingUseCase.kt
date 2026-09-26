package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.TableSettingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTableSettingUseCase @Inject constructor(
    private val repository: TableSettingRepository
) {
    operator fun invoke(): Flow<TableSetting> = repository.getSetting()
}
