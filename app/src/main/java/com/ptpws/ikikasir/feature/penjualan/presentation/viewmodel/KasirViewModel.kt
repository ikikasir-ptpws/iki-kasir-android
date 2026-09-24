package com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.kategori.domain.usecase.GetKategoriUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetTaxSettingUseCase
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetCartUseCase
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.ManageCartUseCase
import com.ptpws.ikikasir.feature.penjualan.presentation.state.KasirState
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.usecase.GetProdukUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KasirViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val getProdukUseCase: GetProdukUseCase,
    private val getKategoriUseCase: GetKategoriUseCase,
    private val getTaxSettingUseCase: GetTaxSettingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(KasirState())
    val state: StateFlow<KasirState> = _state.asStateFlow()

    init {
        observeCart()
        loadProdukKatalog()
        loadKategoriList()
        observeTaxSetting()
    }

    private fun observeTaxSetting() {
        viewModelScope.launch {
            getTaxSettingUseCase().collect { tax ->
                _state.update { it.copy(taxSetting = tax) }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            getCartUseCase().collect { cartList ->
                _state.update { it.copy(cartItems = cartList) }
            }
        }
    }

    private fun loadProdukKatalog() {
        viewModelScope.launch {
            getProdukUseCase().collect { list ->
                _state.update { it.copy(produkKatalog = list) }
            }
        }
    }

    private fun loadKategoriList() {
        viewModelScope.launch {
            getKategoriUseCase().collect { list ->
                _state.update { it.copy(kategoriList = list) }
            }
        }
    }

    fun onBarcodeScanned(scannedCode: String) {
        val matchingProduct = _state.value.produkKatalog.find {
            it.barcode.equals(scannedCode, ignoreCase = true) || it.id.equals(scannedCode, ignoreCase = true)
        }
        if (matchingProduct != null) {
            viewModelScope.launch {
                manageCartUseCase.addToCart(matchingProduct, 1)
                _state.update { it.copy(userMessage = "Produk \"${matchingProduct.name}\" berhasil ditambahkan ke keranjang") }
            }
        } else {
            _state.update { it.copy(errorMessage = "Produk dengan barcode \"$scannedCode\" tidak ditemukan") }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onCatalogSearchQueryChange(query: String) {
        _state.update { it.copy(catalogSearchQuery = query) }
    }

    fun onCategoryFilterChange(categoryId: String?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun incrementCartItem(produk: Produk) {
        viewModelScope.launch {
            manageCartUseCase.addToCart(produk, 1)
        }
    }

    fun decrementCartItem(produk: Produk) {
        viewModelScope.launch {
            val currentQty = _state.value.getItemQuantity(produk.id)
            if (currentQty <= 1) {
                manageCartUseCase.removeFromCart(produk.id)
            } else {
                manageCartUseCase.updateQuantity(produk.id, currentQty - 1)
            }
        }
    }

    fun addToCart(produk: Produk) {
        viewModelScope.launch {
            manageCartUseCase.addToCart(produk)
        }
    }

    fun updateQuantity(produkId: String, quantity: Int) {
        viewModelScope.launch {
            manageCartUseCase.updateQuantity(produkId, quantity)
        }
    }

    fun removeFromCart(produkId: String) {
        viewModelScope.launch {
            manageCartUseCase.removeFromCart(produkId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            manageCartUseCase.clearCart()
        }
    }

    fun openProductCatalogDialog(show: Boolean = true) {
        _state.update { it.copy(showProductCatalogDialog = show) }
    }

    fun openOrderNoteDialog(show: Boolean = true) {
        _state.update { it.copy(showOrderNoteDialog = show) }
    }

    fun onOrderNoteChange(note: String) {
        _state.update { it.copy(orderNote = note) }
    }

    fun clearUserMessage() {
        _state.update { it.copy(userMessage = null) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
