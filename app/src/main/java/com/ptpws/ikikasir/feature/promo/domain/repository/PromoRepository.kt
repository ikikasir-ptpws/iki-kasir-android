package com.ptpws.ikikasir.feature.promo.domain.repository

import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import kotlinx.coroutines.flow.Flow

interface PromoRepository {
    fun getPromoList(): Flow<List<Promo>>
    fun getActivePromos(): Flow<List<Promo>>
    fun getPromoById(id: String): Flow<Promo?>
    suspend fun insertPromo(promo: Promo): Flow<Result<Unit>>
    suspend fun updatePromo(promo: Promo): Flow<Result<Unit>>
    suspend fun deletePromo(id: String): Flow<Result<Unit>>
    suspend fun togglePromoStatus(id: String, isActive: Boolean): Flow<Result<Unit>>
    suspend fun syncPendingPromos(): Flow<Result<Unit>>
}
