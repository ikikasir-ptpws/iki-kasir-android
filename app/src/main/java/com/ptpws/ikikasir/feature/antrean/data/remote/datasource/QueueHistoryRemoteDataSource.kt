package com.ptpws.ikikasir.feature.antrean.data.remote.datasource

import com.ptpws.ikikasir.feature.antrean.data.remote.dto.QueueHistoryDto
import kotlinx.coroutines.flow.Flow

interface QueueHistoryRemoteDataSource {
    suspend fun getAllHistory(): List<QueueHistoryDto>
    suspend fun saveHistory(dto: QueueHistoryDto)
    fun getHistoryFlow(): Flow<List<QueueHistoryDto>>
}
