package com.ptpws.ikikasir.feature.antrean.domain.repository

import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import kotlinx.coroutines.flow.Flow

interface QueueHistoryRepository {
    fun getHistoryList(): Flow<List<QueueHistory>>
    fun getHistoryById(id: String): Flow<QueueHistory?>
    suspend fun insertHistory(history: QueueHistory): Flow<Result<Unit>>
    suspend fun syncHistory(): Flow<Result<Unit>>
}
