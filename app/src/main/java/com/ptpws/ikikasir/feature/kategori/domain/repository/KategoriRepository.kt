package com.ptpws.ikikasir.feature.kategori.domain.repository

import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import kotlinx.coroutines.flow.Flow

interface KategoriRepository {
    fun getKategoriList(): Flow<List<Kategori>>
    fun getKategoriById(id: String): Flow<Kategori?>
    suspend fun insertKategori(kategori: Kategori): Flow<Result<Unit>>
    suspend fun updateKategori(kategori: Kategori): Flow<Result<Unit>>
    suspend fun deleteKategori(id: String): Flow<Result<Unit>>
    suspend fun syncPendingKategori(): Flow<Result<Unit>>
}
