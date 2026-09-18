package com.ptpws.ikikasir.feature.antrean.data.remote.datasource

import com.ptpws.ikikasir.feature.antrean.data.remote.dto.AntreanDto
import kotlinx.coroutines.flow.Flow

interface AntreanRemoteDataSource {
    fun getAntreanFlow(): Flow<List<AntreanDto>>
    suspend fun getAllAntrean(): List<AntreanDto>
    suspend fun getAntreanById(id: String): AntreanDto?
    suspend fun saveAntrean(antreanDto: AntreanDto)
    suspend fun updateAntreanStatus(id: String, status: String, updatedAt: com.google.firebase.Timestamp)
    suspend fun deleteAntrean(id: String)
}
