package com.ptpws.ikikasir.feature.antrean.domain.usecase

import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.antrean.domain.repository.QueueHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InsertQueueHistoryUseCase @Inject constructor(
    private val repository: QueueHistoryRepository
) {
    suspend operator fun invoke(history: QueueHistory): Flow<Result<Unit>> {
        return repository.insertHistory(history)
    }
}
