package com.ptpws.ikikasir.feature.pengaturan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.PaymentMethodSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodSettingDao {
    @Query("SELECT * FROM payment_method_settings WHERE id = :id")
    fun getSettingFlow(id: String = "default"): Flow<PaymentMethodSettingEntity?>

    @Query("SELECT * FROM payment_method_settings WHERE id = :id")
    suspend fun getSetting(id: String = "default"): PaymentMethodSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: PaymentMethodSettingEntity)

    @Query("SELECT * FROM payment_method_settings WHERE isSynced = 0")
    suspend fun getUnsyncedSettings(): List<PaymentMethodSettingEntity>

    @Query("UPDATE payment_method_settings SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String = "default")
}
