package com.ptpws.ikikasir.feature.pengaturan.domain.usecase

import com.ptpws.ikikasir.feature.pengaturan.domain.repository.NotaSettingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncNotaSettingUseCase @Inject constructor(
    private val repository: NotaSettingRepository
) {
    suspend operator fun invoke(): Flow<Result<Unit>> =
        repository.syncPendingNotaSetting()
}
