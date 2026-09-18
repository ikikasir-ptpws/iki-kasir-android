package com.ptpws.ikikasir.feature.antrean.domain.repository

import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import kotlinx.coroutines.flow.Flow

interface AntreanRepository {
    fun getAntreanList(): Flow<List<Antrean>>
    fun getAntreanById(id: String): Flow<Antrean?>
    suspend fun insertAntrean(antrean: Antrean): Flow<Result<Unit>>
    suspend fun updateAntreanStatus(id: String, status: String): Flow<Result<Unit>>
    suspend fun deleteAntrean(id: String): Flow<Result<Unit>>
    suspend fun syncPendingAntrean(): Flow<Result<Unit>>
}
