package com.ptpws.ikikasir.feature.promo.data.remote.datasource

import com.ptpws.ikikasir.feature.promo.data.remote.dto.PromoDto

interface PromoRemoteDataSource {
    suspend fun getAllPromos(): List<PromoDto>
    suspend fun getPromoById(id: String): PromoDto?
    suspend fun savePromo(promoDto: PromoDto)
    suspend fun updatePromo(promoDto: PromoDto)
    suspend fun toggleStatus(id: String, isActive: Boolean)
    suspend fun deletePromo(id: String)
}
