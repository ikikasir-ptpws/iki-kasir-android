package com.ptpws.ikikasir.feature.produk.presentation.viewmodel

import com.ptpws.ikikasir.feature.produk.presentation.state.DetailProdukState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk DetailProdukUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface DetailProdukUiProvider {
    val state: StateFlow<DetailProdukState>
    fun loadProduk(produkId: String)
    fun onVisibilityToggle(isVisible: Boolean)
    fun requestDeleteDialog(show: Boolean)
    fun deleteProduk()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke DetailProdukViewModel asli.
 */
class HiltDetailProdukUiProvider(private val viewModel: DetailProdukViewModel) : DetailProdukUiProvider {
    override val state: StateFlow<DetailProdukState> get() = viewModel.state
    override fun loadProduk(produkId: String) = viewModel.loadProduk(produkId)
    override fun onVisibilityToggle(isVisible: Boolean) = viewModel.onVisibilityToggle(isVisible)
    override fun requestDeleteDialog(show: Boolean) = viewModel.requestDeleteDialog(show)
    override fun deleteProduk() = viewModel.deleteProduk()
}
