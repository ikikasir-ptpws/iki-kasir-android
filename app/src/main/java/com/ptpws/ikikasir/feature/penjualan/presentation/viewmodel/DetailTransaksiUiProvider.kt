package com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel

import com.ptpws.ikikasir.feature.penjualan.presentation.state.DetailTransaksiState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk DetailTransaksiUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface DetailTransaksiUiProvider {
    val state: StateFlow<DetailTransaksiState>
    fun loadTransaksi(transactionId: String)
    fun refundTransaksi(reason: String = "", onSuccess: () -> Unit = {}, onError: (String) -> Unit = {})
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke DetailTransaksiViewModel asli.
 */
class HiltDetailTransaksiUiProvider(private val viewModel: DetailTransaksiViewModel) : DetailTransaksiUiProvider {
    override val state: StateFlow<DetailTransaksiState> get() = viewModel.state
    override fun loadTransaksi(transactionId: String) = viewModel.loadTransaksi(transactionId)
    override fun refundTransaksi(reason: String, onSuccess: () -> Unit) = viewModel.refundTransaksi(reason, onSuccess)
}
