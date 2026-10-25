package com.ptpws.ikikasir.feature.catatankeuangan.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ptpws.ikikasir.feature.catatankeuangan.data.local.entity.CatatanKeuanganEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatatanKeuanganDao {

    @Query("SELECT * FROM catatan_keuangan WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllCatatanFlow(): Flow<List<CatatanKeuanganEntity>>

    @Query("SELECT * FROM catatan_keuangan WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getCatatanByIdFlow(id: String): Flow<CatatanKeuanganEntity?>

    @Query("SELECT * FROM catatan_keuangan WHERE id = :id LIMIT 1")
    suspend fun getCatatanById(id: String): CatatanKeuanganEntity?

    @Upsert
    suspend fun insertOrUpdate(catatan: CatatanKeuanganEntity)

    @Upsert
    suspend fun insertOrUpdateAll(catatanList: List<CatatanKeuanganEntity>)

    @Query("UPDATE catatan_keuangan SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsDeleted(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM catatan_keuangan WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("SELECT * FROM catatan_keuangan WHERE isSynced = 0")
    suspend fun getUnsyncedCatatan(): List<CatatanKeuanganEntity>

    @Query("UPDATE catatan_keuangan SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("UPDATE catatan_keuangan SET proofImageUrl = :proofImageUrl WHERE id = :id")
    suspend fun updateProofImageUrl(id: String, proofImageUrl: String)
}
