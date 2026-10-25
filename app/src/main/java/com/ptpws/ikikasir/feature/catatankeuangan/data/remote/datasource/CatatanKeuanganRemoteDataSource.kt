package com.ptpws.ikikasir.feature.catatankeuangan.data.remote.datasource

import com.ptpws.ikikasir.feature.catatankeuangan.data.remote.dto.CatatanKeuanganDto

interface CatatanKeuanganRemoteDataSource {
    suspend fun getAllCatatan(): List<CatatanKeuanganDto>
    suspend fun saveCatatan(dto: CatatanKeuanganDto)
    suspend fun updateCatatan(dto: CatatanKeuanganDto)
    suspend fun deleteCatatan(id: String)
}
