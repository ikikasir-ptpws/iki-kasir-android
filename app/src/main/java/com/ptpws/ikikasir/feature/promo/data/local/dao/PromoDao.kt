package com.ptpws.ikikasir.feature.promo.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ptpws.ikikasir.feature.promo.data.local.entity.PromoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromoDao {

    @Query("SELECT * FROM promos WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllPromosFlow(): Flow<List<PromoEntity>>

    @Query("SELECT * FROM promos WHERE isDeleted = 0 AND isActive = 1 ORDER BY createdAt DESC")
    fun getActivePromosFlow(): Flow<List<PromoEntity>>

    @Query("SELECT * FROM promos WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getPromoByIdFlow(id: String): Flow<PromoEntity?>

    @Query("SELECT * FROM promos WHERE id = :id LIMIT 1")
    suspend fun getPromoById(id: String): PromoEntity?

    @Upsert
    suspend fun insertOrUpdate(promo: PromoEntity)

    @Upsert
    suspend fun insertOrUpdateAll(promoList: List<PromoEntity>)

    @Query("UPDATE promos SET isActive = :isActive, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleStatus(id: String, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE promos SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsDeleted(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM promos WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("SELECT * FROM promos WHERE isSynced = 0")
    suspend fun getUnsyncedPromos(): List<PromoEntity>

    @Query("UPDATE promos SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
