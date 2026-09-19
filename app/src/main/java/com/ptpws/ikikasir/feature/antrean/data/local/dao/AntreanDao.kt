package com.ptpws.ikikasir.feature.antrean.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ptpws.ikikasir.feature.antrean.data.local.entity.AntreanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AntreanDao {

    @Query("SELECT * FROM queues WHERE isDeleted = 0 ORDER BY queueSequence ASC")
    fun getAllAntreanFlow(): Flow<List<AntreanEntity>>

    @Query("SELECT * FROM queues WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getAntreanByIdFlow(id: String): Flow<AntreanEntity?>

    @Query("SELECT * FROM queues WHERE id = :id LIMIT 1")
    suspend fun getAntreanById(id: String): AntreanEntity?

    @Upsert
    suspend fun insertOrUpdate(antrean: AntreanEntity)

    @Upsert
    suspend fun insertOrUpdateAll(antreanList: List<AntreanEntity>)

    @Query("SELECT COALESCE(MAX(queueSequence), 0) FROM queues WHERE isDeleted = 0")
    suspend fun getMaxQueueSequence(): Int

    @Query("UPDATE queues SET status = :status, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE queues SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsDeleted(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM queues WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("SELECT * FROM queues WHERE isSynced = 0")
    suspend fun getUnsyncedAntrean(): List<AntreanEntity>

    @Query("UPDATE queues SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
