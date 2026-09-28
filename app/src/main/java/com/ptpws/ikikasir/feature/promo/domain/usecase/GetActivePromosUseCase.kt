package com.ptpws.ikikasir.feature.promo.domain.usecase

import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.repository.PromoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActivePromosUseCase @Inject constructor(
    private val repository: PromoRepository
) {
    operator fun invoke(): Flow<List<Promo>> {
        return repository.getActivePromos()
    }
}
