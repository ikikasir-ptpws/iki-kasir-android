package com.ptpws.ikikasir.feature.manajemenstok.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ptpws.ikikasir.feature.manajemenstok.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StokAdjustmentDao {

    @Query("SELECT * FROM stock_movements ORDER BY createdAt DESC")
    fun getAllMovementsFlow(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY createdAt DESC")
    fun getMovementsByProductIdFlow(productId: String): Flow<List<StockMovementEntity>>

    @Upsert
    suspend fun insertOrUpdate(movement: StockMovementEntity)

    @Upsert
    suspend fun insertOrUpdateAll(movements: List<StockMovementEntity>)

    @Query("SELECT * FROM stock_movements WHERE isSynced = 0")
    suspend fun getUnsyncedMovements(): List<StockMovementEntity>

    @Query("SELECT * FROM stock_movements WHERE productId = :productId AND isSynced = 0 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getUnsyncedMovementForProduct(productId: String): StockMovementEntity?

    @Query("UPDATE stock_movements SET isSynced = 1 WHERE movementId = :movementId")
    suspend fun markAsSynced(movementId: String)
}
