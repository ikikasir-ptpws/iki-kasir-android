package com.ptpws.ikikasir.feature.kategori.presentation.viewmodel

import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.presentation.state.KategoriListState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk KategoriUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface KategoriUiProvider {
    val state: StateFlow<KategoriListState>
    fun onSearchQueryChange(query: String)
    fun requestDeleteKategori(kategori: Kategori)
    fun dismissDeleteDialog()
    fun deleteKategori(id: String)
    fun toggleCashierVisibility(kategori: Kategori)
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke KategoriViewModel asli.
 */
class HiltKategoriUiProvider(private val viewModel: KategoriViewModel) : KategoriUiProvider {
    override val state: StateFlow<KategoriListState> get() = viewModel.state
    override fun onSearchQueryChange(query: String) = viewModel.onSearchQueryChange(query)
    override fun requestDeleteKategori(kategori: Kategori) = viewModel.requestDeleteKategori(kategori)
    override fun dismissDeleteDialog() = viewModel.dismissDeleteDialog()
    override fun deleteKategori(id: String) = viewModel.deleteKategori(id)
    override fun toggleCashierVisibility(kategori: Kategori) = viewModel.toggleCashierVisibility(kategori)
}
