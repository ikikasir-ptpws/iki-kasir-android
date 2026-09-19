package com.ptpws.ikikasir.feature.antrean.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ptpws.ikikasir.feature.antrean.data.local.entity.QueueHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueHistoryDao {

    @Query("SELECT * FROM queue_history WHERE isDeleted = 0 ORDER BY completedAt DESC")
    fun getAllHistoryFlow(): Flow<List<QueueHistoryEntity>>

    @Query("SELECT * FROM queue_history WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getHistoryByIdFlow(id: String): Flow<QueueHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: QueueHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(entities: List<QueueHistoryEntity>)

    @Query("SELECT * FROM queue_history WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedHistory(): List<QueueHistoryEntity>

    @Query("UPDATE queue_history SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT COALESCE(MAX(queueSequence), 0) FROM queue_history WHERE isDeleted = 0")
    suspend fun getMaxQueueSequence(): Int
}
