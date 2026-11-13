package com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel

import com.ptpws.ikikasir.feature.penjualan.presentation.state.KasirState
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk KasirUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface KasirUiProvider {
    val state: StateFlow<KasirState>
    fun onBarcodeScanned(scannedCode: String)
    fun onSearchQueryChange(query: String)
    fun onCatalogSearchQueryChange(query: String)
    fun onCategoryFilterChange(categoryId: String?)
    fun incrementCartItem(produk: Produk)
    fun decrementCartItem(produk: Produk)
    fun updateQuantity(produkId: String, quantity: Int)
    fun setCartItemQuantity(produk: Produk, quantity: Int)
    fun removeFromCart(produkId: String)
    fun clearCart()
    fun openProductCatalogDialog(show: Boolean = true)
    fun refreshKatalog()
    fun clearUserMessage()
    fun clearError()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke KasirViewModel asli.
 */
class HiltKasirUiProvider(private val viewModel: KasirViewModel) : KasirUiProvider {
    override val state: StateFlow<KasirState> get() = viewModel.state
    override fun onBarcodeScanned(scannedCode: String) = viewModel.onBarcodeScanned(scannedCode)
    override fun onSearchQueryChange(query: String) = viewModel.onSearchQueryChange(query)
    override fun onCatalogSearchQueryChange(query: String) = viewModel.onCatalogSearchQueryChange(query)
    override fun onCategoryFilterChange(categoryId: String?) = viewModel.onCategoryFilterChange(categoryId)
    override fun incrementCartItem(produk: Produk) = viewModel.incrementCartItem(produk)
    override fun decrementCartItem(produk: Produk) = viewModel.decrementCartItem(produk)
    override fun updateQuantity(produkId: String, quantity: Int) = viewModel.updateQuantity(produkId, quantity)
    override fun setCartItemQuantity(produk: Produk, quantity: Int) = viewModel.setCartItemQuantity(produk, quantity)
    override fun removeFromCart(produkId: String) = viewModel.removeFromCart(produkId)
    override fun clearCart() = viewModel.clearCart()
    override fun openProductCatalogDialog(show: Boolean) = viewModel.openProductCatalogDialog(show)
    override fun refreshKatalog() = viewModel.refreshKatalog()
    override fun clearUserMessage() = viewModel.clearUserMessage()
    override fun clearError() = viewModel.clearError()
}
