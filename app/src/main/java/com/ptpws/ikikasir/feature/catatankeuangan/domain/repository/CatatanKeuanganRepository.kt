package com.ptpws.ikikasir.feature.catatankeuangan.domain.repository

import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan
import kotlinx.coroutines.flow.Flow

interface CatatanKeuanganRepository {
    fun getCatatanList(): Flow<List<CatatanKeuangan>>
    fun getCatatanById(id: String): Flow<CatatanKeuangan?>
    suspend fun insertCatatan(catatan: CatatanKeuangan): Flow<Result<Unit>>
    suspend fun updateCatatan(catatan: CatatanKeuangan): Flow<Result<Unit>>
    suspend fun deleteCatatan(id: String): Flow<Result<Unit>>
    suspend fun syncPendingCatatan(): Flow<Result<Unit>>
}
