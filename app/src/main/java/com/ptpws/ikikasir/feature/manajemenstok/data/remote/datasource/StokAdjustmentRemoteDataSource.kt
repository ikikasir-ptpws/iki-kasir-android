package com.ptpws.ikikasir.feature.manajemenstok.data.remote.datasource

import com.ptpws.ikikasir.feature.manajemenstok.data.remote.dto.StockMovementDto

interface StokAdjustmentRemoteDataSource {
    suspend fun getAllMovements(): List<StockMovementDto>
    suspend fun saveMovement(dto: StockMovementDto)
}
