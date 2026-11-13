package com.ptpws.ikikasir.feature.manajemenstok.presentation.viewmodel

import com.ptpws.ikikasir.feature.manajemenstok.presentation.state.ManajemenStokState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk ManajemenStokUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface ManajemenStokUiProvider {
    val state: StateFlow<ManajemenStokState>
    fun onSearchQueryChange(query: String)
    fun toggleFilterKritisOnly()
    fun closeRestockDialog()
    fun onRestockJumlahChange(jumlah: String)
    fun simpanRestock()
    fun clearError()
    fun clearSuccessRestock()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke ManajemenStokViewModel asli.
 */
class HiltManajemenStokUiProvider(private val viewModel: ManajemenStokViewModel) : ManajemenStokUiProvider {
    override val state: StateFlow<ManajemenStokState> get() = viewModel.state
    override fun onSearchQueryChange(query: String) = viewModel.onSearchQueryChange(query)
    override fun toggleFilterKritisOnly() = viewModel.toggleFilterKritisOnly()
    override fun closeRestockDialog() = viewModel.closeRestockDialog()
    override fun onRestockJumlahChange(jumlah: String) = viewModel.onRestockJumlahChange(jumlah)
    override fun simpanRestock() = viewModel.simpanRestock()
    override fun clearError() = viewModel.clearError()
    override fun clearSuccessRestock() = viewModel.clearSuccessRestock()
}
