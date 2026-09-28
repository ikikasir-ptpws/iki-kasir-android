package com.ptpws.ikikasir.feature.promo.domain.usecase

import com.ptpws.ikikasir.feature.promo.domain.repository.PromoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeletePromoUseCase @Inject constructor(
    private val repository: PromoRepository
) {
    suspend operator fun invoke(id: String): Flow<Result<Unit>> {
        return repository.deletePromo(id)
    }
}
