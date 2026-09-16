package com.ptpws.ikikasir.feature.penjualan.presentation.state

import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class KasirState(
    val searchQuery: String = "",
    val catalogSearchQuery: String = "",
    val selectedCategoryId: String? = null,
    val kategoriList: List<Kategori> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val produkKatalog: List<Produk> = emptyList(),
    val showProductCatalogDialog: Boolean = false,
    val orderNote: String = "",
    val showOrderNoteDialog: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
) {
    val totalItemCount: Int
        get() = cartItems.sumOf { it.quantity }

    val subtotal: Double
        get() = cartItems.sumOf { it.totalPrice }

    fun getItemQuantity(produkId: String): Int {
        return cartItems.find { it.produk.id == produkId }?.quantity ?: 0
    }
}
