package com.ptpws.ikikasir.feature.promo.domain.usecase

import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.repository.PromoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPromoByIdUseCase @Inject constructor(
    private val repository: PromoRepository
) {
    operator fun invoke(id: String): Flow<Promo?> {
        return repository.getPromoById(id)
    }
}
