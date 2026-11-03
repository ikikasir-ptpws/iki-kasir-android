package com.ptpws.ikikasir.feature.keuangan.data.remote.datasource

import com.ptpws.ikikasir.feature.keuangan.data.remote.dto.CatatanKeuanganDto
import kotlinx.coroutines.flow.Flow

interface CatatanKeuanganRemoteDataSource {
    fun getCatatanFlow(): Flow<List<CatatanKeuanganDto>>
    suspend fun saveCatatan(dto: CatatanKeuanganDto)
    suspend fun deleteCatatan(id: String)
}
