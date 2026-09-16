package com.ptpws.ikikasir.feature.manajemenstok.domain.usecase

import com.ptpws.ikikasir.feature.manajemenstok.domain.model.StockMovement
import com.ptpws.ikikasir.feature.manajemenstok.domain.repository.StockMovementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStokAdjustmentUseCase @Inject constructor(
    private val repository: StockMovementRepository
) {
    operator fun invoke(): Flow<List<StockMovement>> = repository.getAllMovements()

    fun byProductId(productId: String): Flow<List<StockMovement>> =
        repository.getMovementsByProductId(productId)
}
