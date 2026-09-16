package com.ptpws.ikikasir.feature.manajemenstok.domain.repository

import com.ptpws.ikikasir.feature.manajemenstok.domain.model.StockMovement
import kotlinx.coroutines.flow.Flow

interface StockMovementRepository {
    fun getAllMovements(): Flow<List<StockMovement>>
    fun getMovementsByProductId(productId: String): Flow<List<StockMovement>>
    suspend fun saveMovement(movement: StockMovement): Flow<Result<Unit>>
    suspend fun syncPendingMovements(): Flow<Result<Unit>>
}
