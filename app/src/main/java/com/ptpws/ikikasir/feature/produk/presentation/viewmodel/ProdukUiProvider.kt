package com.ptpws.ikikasir.feature.produk.presentation.viewmodel

import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.presentation.state.ProdukListState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk ProdukUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface ProdukUiProvider {
    val state: StateFlow<ProdukListState>
    fun onSearchQueryChange(query: String)
    fun onCategoryFilterChange(categoryId: String?)
    fun requestDeleteProduk(produk: Produk)
    fun dismissDeleteDialog()
    fun deleteProduk(id: String)
    fun updateProdukCategory(produk: Produk, newCategoryId: String)
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke ProdukViewModel asli.
 */
class HiltProdukUiProvider(private val viewModel: ProdukViewModel) : ProdukUiProvider {
    override val state: StateFlow<ProdukListState> get() = viewModel.state
    override fun onSearchQueryChange(query: String) = viewModel.onSearchQueryChange(query)
    override fun onCategoryFilterChange(categoryId: String?) = viewModel.onCategoryFilterChange(categoryId)
    override fun requestDeleteProduk(produk: Produk) = viewModel.requestDeleteProduk(produk)
    override fun dismissDeleteDialog() = viewModel.dismissDeleteDialog()
    override fun deleteProduk(id: String) = viewModel.deleteProduk(id)
    override fun updateProdukCategory(produk: Produk, newCategoryId: String) = viewModel.updateProdukCategory(produk, newCategoryId)
}
