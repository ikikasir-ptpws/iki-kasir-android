package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.TableSettingRepository
import javax.inject.Inject

class SaveTableSettingUseCase @Inject constructor(
    private val repository: TableSettingRepository
) {
    suspend operator fun invoke(setting: TableSetting): Result<Unit> = repository.saveSetting(setting)
}
