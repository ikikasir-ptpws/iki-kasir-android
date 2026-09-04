package com.ptpws.ikikasir.feature.kategori.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.ptpws.ikikasir.feature.kategori.data.local.entity.KategoriEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KategoriDao {

    @Query("SELECT * FROM kategori WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllKategoriFlow(): Flow<List<KategoriEntity>>

    @Query("SELECT * FROM kategori WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getKategoriByIdFlow(id: String): Flow<KategoriEntity?>

    @Query("SELECT * FROM kategori WHERE id = :id LIMIT 1")
    suspend fun getKategoriById(id: String): KategoriEntity?

    @Upsert
    suspend fun insertOrUpdate(kategori: KategoriEntity)

    @Upsert
    suspend fun insertOrUpdateAll(kategoriList: List<KategoriEntity>)

    @Query("UPDATE kategori SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsDeleted(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM kategori WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("SELECT * FROM kategori WHERE isSynced = 0")
    suspend fun getUnsyncedKategori(): List<KategoriEntity>

    @Query("UPDATE kategori SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
