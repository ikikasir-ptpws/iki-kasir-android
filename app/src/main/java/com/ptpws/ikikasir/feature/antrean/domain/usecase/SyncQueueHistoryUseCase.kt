package com.ptpws.ikikasir.feature.antrean.domain.usecase

import com.ptpws.ikikasir.feature.antrean.domain.repository.QueueHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncQueueHistoryUseCase @Inject constructor(
    private val repository: QueueHistoryRepository
) {
    suspend operator fun invoke(): Flow<Result<Unit>> {
        return repository.syncHistory()
    }
}
