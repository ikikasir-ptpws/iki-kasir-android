package com.ptpws.ikikasir.feature.antrean.presentation.viewmodel

import com.ptpws.ikikasir.feature.antrean.presentation.state.AntreanUiState
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk AntreanUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface AntreanUiProvider {
    val uiState: StateFlow<AntreanUiState>
    val searchQuery: StateFlow<String>
    val transaksiMap: StateFlow<Map<String, PenjualanTransaksi>>
    val isBulkProcessing: StateFlow<Boolean>
    fun onSearchQueryChanged(query: String)
    fun selesaikan(id: String)
    fun batalkan(id: String)
    fun selesaikanBanyak(ids: List<String>)
    fun batalkanBanyak(ids: List<String>)
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke AntreanViewModel asli.
 */
class HiltAntreanUiProvider(private val viewModel: AntreanViewModel) : AntreanUiProvider {
    override val uiState: StateFlow<AntreanUiState> get() = viewModel.uiState
    override val searchQuery: StateFlow<String> get() = viewModel.searchQuery
    override val transaksiMap: StateFlow<Map<String, PenjualanTransaksi>> get() = viewModel.transaksiMap
    override val isBulkProcessing: StateFlow<Boolean> get() = viewModel.isBulkProcessing
    override fun onSearchQueryChanged(query: String) = viewModel.onSearchQueryChanged(query)
    override fun selesaikan(id: String) = viewModel.selesaikan(id)
    override fun batalkan(id: String) = viewModel.batalkan(id)
    override fun selesaikanBanyak(ids: List<String>) = viewModel.selesaikanBanyak(ids)
    override fun batalkanBanyak(ids: List<String>) = viewModel.batalkanBanyak(ids)
}
