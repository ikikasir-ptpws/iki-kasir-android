package com.ptpws.ikikasir.feature.antrean.domain.usecase

import com.ptpws.ikikasir.feature.antrean.domain.repository.AntreanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncAntreanUseCase @Inject constructor(
    private val repository: AntreanRepository
) {
    suspend operator fun invoke(): Flow<Result<Unit>> {
        return repository.syncPendingAntrean()
    }
}
