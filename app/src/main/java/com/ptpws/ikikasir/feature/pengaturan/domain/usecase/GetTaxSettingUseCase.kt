package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.TaxSettingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTaxSettingUseCase @Inject constructor(
    private val repository: TaxSettingRepository
) {
    operator fun invoke(): Flow<TaxSetting> {
        return repository.getTaxSetting()
    }
}
