package com.ptpws.ikikasir.feature.promo.domain.usecase

import com.ptpws.ikikasir.feature.promo.domain.repository.PromoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TogglePromoStatusUseCase @Inject constructor(
    private val repository: PromoRepository
) {
    suspend operator fun invoke(id: String, isActive: Boolean): Flow<Result<Unit>> {
        return repository.togglePromoStatus(id, isActive)
    }
}
