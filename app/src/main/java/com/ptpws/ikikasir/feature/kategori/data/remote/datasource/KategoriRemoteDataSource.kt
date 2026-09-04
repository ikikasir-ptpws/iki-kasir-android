package com.ptpws.ikikasir.feature.kategori.data.remote.datasource

import com.ptpws.ikikasir.feature.kategori.data.remote.dto.KategoriDto
import kotlinx.coroutines.flow.Flow

interface KategoriRemoteDataSource {
    fun getKategoriFlow(): Flow<List<KategoriDto>>
    suspend fun getAllKategori(): List<KategoriDto>
    suspend fun getKategoriById(id: String): KategoriDto?
    suspend fun saveKategori(kategoriDto: KategoriDto)
    suspend fun updateKategori(kategoriDto: KategoriDto)
    suspend fun deleteKategori(id: String)
}
