package com.ptpws.ikikasir.feature.pengaturan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.ArSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArSettingDao {
    @Query("SELECT * FROM ar_settings WHERE id = :id")
    fun getSettingFlow(id: String = "default"): Flow<ArSettingEntity?>

    @Query("SELECT * FROM ar_settings WHERE id = :id")
    suspend fun getSetting(id: String = "default"): ArSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: ArSettingEntity)

    @Query("SELECT * FROM ar_settings WHERE isSynced = 0")
    suspend fun getUnsyncedSettings(): List<ArSettingEntity>

    @Query("UPDATE ar_settings SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String = "default")
}
