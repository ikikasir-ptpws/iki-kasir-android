package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.NotaSettingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveNotaSettingUseCase @Inject constructor(
    private val repository: NotaSettingRepository
) {
    suspend operator fun invoke(setting: NotaSetting): Flow<Result<Unit>> =
        repository.saveNotaSetting(setting)
}
