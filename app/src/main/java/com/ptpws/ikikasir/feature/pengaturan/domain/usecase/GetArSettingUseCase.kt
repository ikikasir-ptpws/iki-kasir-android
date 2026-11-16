package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.model.ArSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.ArSettingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArSettingUseCase @Inject constructor(
    private val repository: ArSettingRepository
) {
    operator fun invoke(): Flow<ArSetting> {
        return repository.getSetting()
    }
}
