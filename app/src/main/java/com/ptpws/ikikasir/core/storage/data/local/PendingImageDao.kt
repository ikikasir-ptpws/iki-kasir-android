package com.ptpws.ikikasir.core.storage.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PendingImageDao {
    @Upsert
    suspend fun enqueue(item: PendingImageEntity)

    @Query("SELECT * FROM pending_images ORDER BY createdAt")
    suspend fun getPending(): List<PendingImageEntity>

    @Query("DELETE FROM pending_images WHERE id = :id")
    suspend fun remove(id: String)
}
