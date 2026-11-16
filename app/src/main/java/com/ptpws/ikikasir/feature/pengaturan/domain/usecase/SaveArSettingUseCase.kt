package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.model.ArSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.ArSettingRepository
import javax.inject.Inject

class SaveArSettingUseCase @Inject constructor(
    private val repository: ArSettingRepository
) {
    suspend operator fun invoke(setting: ArSetting): Result<Unit> {
        return repository.saveSetting(setting)
    }
}
