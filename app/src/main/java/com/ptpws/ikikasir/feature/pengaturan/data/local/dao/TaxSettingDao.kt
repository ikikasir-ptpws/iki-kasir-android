package com.ptpws.ikikasir.feature.pengaturan.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.TaxSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxSettingDao {

    @Query("SELECT * FROM tax_settings WHERE id = 'default' LIMIT 1")
    fun getTaxSettingFlow(): Flow<TaxSettingEntity?>

    @Query("SELECT * FROM tax_settings WHERE id = 'default' LIMIT 1")
    suspend fun getTaxSetting(): TaxSettingEntity?

    @Upsert
    suspend fun insertOrUpdate(setting: TaxSettingEntity)

    @Query("SELECT * FROM tax_settings WHERE isSynced = 0")
    suspend fun getUnsyncedTaxSettings(): List<TaxSettingEntity>

    @Query("UPDATE tax_settings SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
