package com.ptpws.ikikasir.feature.pengaturan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.TableSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TableSettingDao {
    @Query("SELECT * FROM table_settings WHERE id = :id")
    fun getSettingFlow(id: String = "default"): Flow<TableSettingEntity?>

    @Query("SELECT * FROM table_settings WHERE id = :id")
    suspend fun getSetting(id: String = "default"): TableSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: TableSettingEntity)

    @Query("SELECT * FROM table_settings WHERE isSynced = 0")
    suspend fun getUnsyncedSettings(): List<TableSettingEntity>

    @Query("UPDATE table_settings SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String = "default")
}
