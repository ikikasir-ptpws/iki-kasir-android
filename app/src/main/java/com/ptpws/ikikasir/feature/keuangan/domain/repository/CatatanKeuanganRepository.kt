package com.ptpws.ikikasir.feature.keuangan.domain.repository

import com.ptpws.ikikasir.feature.keuangan.domain.model.CatatanKeuangan
import kotlinx.coroutines.flow.Flow

interface CatatanKeuanganRepository {
    fun getAllCatatanFlow(): Flow<List<CatatanKeuangan>>
    suspend fun getCatatanById(id: String): CatatanKeuangan?
    suspend fun insertCatatan(catatan: CatatanKeuangan)
    suspend fun updateCatatan(catatan: CatatanKeuangan)
    suspend fun deleteCatatanById(id: String)
}
