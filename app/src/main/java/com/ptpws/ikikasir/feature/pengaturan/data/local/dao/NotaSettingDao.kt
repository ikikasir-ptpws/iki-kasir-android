package com.ptpws.ikikasir.feature.pengaturan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.NotaSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotaSettingDao {

    @Query("SELECT * FROM nota_settings WHERE id = :id LIMIT 1")
    fun getNotaSettingFlow(id: String = "default_nota_setting"): Flow<NotaSettingEntity?>

    @Query("SELECT * FROM nota_settings WHERE id = :id LIMIT 1")
    suspend fun getNotaSetting(id: String = "default_nota_setting"): NotaSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(setting: NotaSettingEntity)

    @Query("SELECT * FROM nota_settings WHERE isSynced = 0")
    suspend fun getUnsyncedNotaSettings(): List<NotaSettingEntity>

    @Query("UPDATE nota_settings SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String = "default_nota_setting")
}
