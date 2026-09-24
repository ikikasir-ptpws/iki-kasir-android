package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.TaxSettingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveTaxSettingUseCase @Inject constructor(
    private val repository: TaxSettingRepository
) {
    suspend operator fun invoke(setting: TaxSetting): Flow<Result<Unit>> {
        return repository.saveTaxSetting(setting)
    }
}
