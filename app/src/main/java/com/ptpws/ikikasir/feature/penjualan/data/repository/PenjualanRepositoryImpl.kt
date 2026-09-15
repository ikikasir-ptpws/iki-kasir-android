package com.ptpws.ikikasir.feature.penjualan.data.repository

import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PenjualanRepositoryImpl @Inject constructor() : PenjualanRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    override fun getCartItems(): Flow<List<CartItem>> {
        return _cartItems.asStateFlow()
    }

    override suspend fun addToCart(produk: Produk, quantity: Int) {
        _cartItems.update { current ->
            val existing = current.find { it.produk.id == produk.id }
            if (existing != null) {
                current.map {
                    if (it.produk.id == produk.id) {
                        it.copy(quantity = it.quantity + quantity)
                    } else it
                }
            } else {
                current + CartItem(produk = produk, quantity = quantity)
            }
        }
    }

    override suspend fun removeFromCart(produkId: String) {
        _cartItems.update { current ->
            current.filterNot { it.produk.id == produkId }
        }
    }

    override suspend fun updateQuantity(produkId: String, quantity: Int) {
        _cartItems.update { current ->
            if (quantity <= 0) {
                current.filterNot { it.produk.id == produkId }
            } else {
                current.map {
                    if (it.produk.id == produkId) {
                        it.copy(quantity = quantity)
                    } else it
                }
            }
        }
    }

    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }

    override suspend fun simpanTransaksi(transaksi: PenjualanTransaksi): Flow<Result<Unit>> = flow {
        try {
            clearCart()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
