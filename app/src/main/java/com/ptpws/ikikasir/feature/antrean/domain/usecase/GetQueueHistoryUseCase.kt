package com.ptpws.ikikasir.feature.antrean.domain.usecase

import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.antrean.domain.repository.QueueHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQueueHistoryUseCase @Inject constructor(
    private val repository: QueueHistoryRepository
) {
    operator fun invoke(): Flow<List<QueueHistory>> {
        return repository.getHistoryList()
    }
}
