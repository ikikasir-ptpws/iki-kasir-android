package com.ptpws.ikikasir.feature.produk.domain.repository

import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import kotlinx.coroutines.flow.Flow

interface ProdukRepository {
    fun getProdukList(): Flow<List<Produk>>
    fun getProdukByCategoryId(categoryId: String): Flow<List<Produk>>
    fun getProdukById(id: String): Flow<Produk?>
    suspend fun insertProduk(produk: Produk): Flow<Result<Unit>>
    suspend fun updateProduk(produk: Produk): Flow<Result<Unit>>
    suspend fun deleteProduk(id: String): Flow<Result<Unit>>
    suspend fun syncPendingProduk(): Flow<Result<Unit>>
}