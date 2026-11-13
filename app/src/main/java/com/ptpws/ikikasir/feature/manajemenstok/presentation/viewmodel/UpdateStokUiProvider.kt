package com.ptpws.ikikasir.feature.manajemenstok.presentation.viewmodel

import com.ptpws.ikikasir.feature.manajemenstok.presentation.state.UpdateStokState
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk UpdateStokUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface UpdateStokUiProvider {
    val state: StateFlow<UpdateStokState>
    fun selectProduk(produk: Produk)
    fun toggleProductDropdown(show: Boolean? = null)
    fun incrementStok()
    fun decrementStok()
    fun setNewStock(stock: Int)
    fun simpanPerubahan()
    fun clearError()
    fun clearSuccess()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke UpdateStokViewModel asli.
 */
class HiltUpdateStokUiProvider(private val viewModel: UpdateStokViewModel) : UpdateStokUiProvider {
    override val state: StateFlow<UpdateStokState> get() = viewModel.state
    override fun selectProduk(produk: Produk) = viewModel.selectProduk(produk)
    override fun toggleProductDropdown(show: Boolean?) = viewModel.toggleProductDropdown(show)
    override fun incrementStok() = viewModel.incrementStok()
    override fun decrementStok() = viewModel.decrementStok()
    override fun setNewStock(stock: Int) = viewModel.setNewStock(stock)
    override fun simpanPerubahan() = viewModel.simpanPerubahan()
    override fun clearError() = viewModel.clearError()
    override fun clearSuccess() = viewModel.clearSuccess()
}
