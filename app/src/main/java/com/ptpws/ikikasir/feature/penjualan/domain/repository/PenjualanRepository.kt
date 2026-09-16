package com.ptpws.ikikasir.feature.penjualan.domain.repository

import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import kotlinx.coroutines.flow.Flow

interface PenjualanRepository {
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun addToCart(produk: Produk, quantity: Int = 1)
    suspend fun removeFromCart(produkId: String)
    suspend fun updateQuantity(produkId: String, quantity: Int)
    suspend fun clearCart()

    fun getAllTransaksi(): Flow<List<PenjualanTransaksi>>
    fun getTransaksiById(transactionId: String): Flow<PenjualanTransaksi?>
    suspend fun simpanTransaksi(transaksi: PenjualanTransaksi): Flow<Result<Unit>>
    suspend fun syncPendingTransaksi(): Flow<Result<Unit>>
}
