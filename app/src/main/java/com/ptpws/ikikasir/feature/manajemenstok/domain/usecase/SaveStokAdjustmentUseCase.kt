package com.ptpws.ikikasir.feature.manajemenstok.domain.usecase

import com.ptpws.ikikasir.feature.manajemenstok.domain.model.StockMovement
import com.ptpws.ikikasir.feature.manajemenstok.domain.repository.StockMovementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveStokAdjustmentUseCase @Inject constructor(
    private val repository: StockMovementRepository
) {
    suspend operator fun invoke(movement: StockMovement): Flow<Result<Unit>> =
        repository.saveMovement(movement)
}
