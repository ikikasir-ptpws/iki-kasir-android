package com.ptpws.ikikasir.feature.produk.data.remote.datasource

import com.ptpws.ikikasir.feature.produk.data.remote.dto.ProdukDto
import kotlinx.coroutines.flow.Flow

interface ProdukRemoteDataSource {
    fun getProdukFlow(): Flow<List<ProdukDto>>
    suspend fun getAllProduk(): List<ProdukDto>
    suspend fun getProdukById(id: String): ProdukDto?
    suspend fun saveProduk(produkDto: ProdukDto)
    suspend fun updateProduk(produkDto: ProdukDto)
    suspend fun deleteProduk(id: String)
}