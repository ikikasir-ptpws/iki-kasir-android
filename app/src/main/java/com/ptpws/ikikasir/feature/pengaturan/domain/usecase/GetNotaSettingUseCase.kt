package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.NotaSettingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotaSettingUseCase @Inject constructor(
    private val repository: NotaSettingRepository
) {
    operator fun invoke(): Flow<NotaSetting> = repository.getNotaSetting()
}
