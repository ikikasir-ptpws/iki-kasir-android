package com.ptpws.ikikasir.feature.promo.domain.usecase

import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.repository.PromoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InsertPromoUseCase @Inject constructor(
    private val repository: PromoRepository
) {
    suspend operator fun invoke(promo: Promo): Flow<Result<Unit>> {
        return repository.insertPromo(promo)
    }
}
