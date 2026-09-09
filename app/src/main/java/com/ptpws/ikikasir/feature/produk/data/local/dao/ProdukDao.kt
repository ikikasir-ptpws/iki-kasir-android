package com.ptpws.ikikasir.feature.produk.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ptpws.ikikasir.feature.produk.data.local.entity.ProdukEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdukDao {

    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllProdukFlow(): Flow<List<ProdukEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getProdukByCategoryIdFlow(categoryId: String): Flow<List<ProdukEntity>>

    @Query("SELECT * FROM products WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getProdukByIdFlow(id: String): Flow<ProdukEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProdukById(id: String): ProdukEntity?

    @Upsert
    suspend fun insertOrUpdate(produk: ProdukEntity)

    @Upsert
    suspend fun insertOrUpdateAll(produkList: List<ProdukEntity>)

    @Query("UPDATE products SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsDeleted(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProduk(): List<ProdukEntity>

    @Query("UPDATE products SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}