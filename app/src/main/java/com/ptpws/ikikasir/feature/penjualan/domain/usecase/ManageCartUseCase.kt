package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import javax.inject.Inject

class ManageCartUseCase @Inject constructor(
    private val repository: PenjualanRepository
) {
    suspend fun addToCart(produk: Produk, quantity: Int = 1) {
        repository.addToCart(produk, quantity)
    }

    suspend fun removeFromCart(produkId: String) {
        repository.removeFromCart(produkId)
    }

    suspend fun updateQuantity(produkId: String, quantity: Int) {
        repository.updateQuantity(produkId, quantity)
    }

    suspend fun clearCart() {
        repository.clearCart()
    }
}
