package com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk DetailCatatanKeuanganUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface DetailCatatanKeuanganUiProvider {
    val state: StateFlow<DetailCatatanUiState>
    fun deleteCatatan(onSuccess: () -> Unit)
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke DetailCatatanKeuanganViewModel asli.
 */
class HiltDetailCatatanKeuanganUiProvider(private val viewModel: DetailCatatanKeuanganViewModel) : DetailCatatanKeuanganUiProvider {
    override val state: StateFlow<DetailCatatanUiState> get() = viewModel.state
    override fun deleteCatatan(onSuccess: () -> Unit) = viewModel.deleteCatatan(onSuccess)
}
