package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartUseCase @Inject constructor(
    private val repository: PenjualanRepository
) {
    operator fun invoke(): Flow<List<CartItem>> {
        return repository.getCartItems()
    }
}
