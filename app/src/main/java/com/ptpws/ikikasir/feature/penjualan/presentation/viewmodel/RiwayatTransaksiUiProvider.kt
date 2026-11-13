package com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel

import android.content.Context
import com.ptpws.ikikasir.feature.penjualan.presentation.state.RiwayatTransaksiState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk RiwayatTransaksiUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface RiwayatTransaksiUiProvider {
    val state: StateFlow<RiwayatTransaksiState>
    fun onSearchQueryChange(query: String)
    fun onFilterSelect(filter: String)
    fun onCustomDateRangeSelect(startMillis: Long, endMillis: Long)
    fun applyInitialFilter(filter: String?, startMillis: Long? = null, endMillis: Long? = null)
    fun resetToDefault()
    fun exportToExcel(context: Context)
    fun clearExportState()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke RiwayatTransaksiViewModel asli.
 */
class HiltRiwayatTransaksiUiProvider(private val viewModel: RiwayatTransaksiViewModel) : RiwayatTransaksiUiProvider {
    override val state: StateFlow<RiwayatTransaksiState> get() = viewModel.state
    override fun onSearchQueryChange(query: String) = viewModel.onSearchQueryChange(query)
    override fun onFilterSelect(filter: String) = viewModel.onFilterSelect(filter)
    override fun onCustomDateRangeSelect(startMillis: Long, endMillis: Long) = viewModel.onCustomDateRangeSelect(startMillis, endMillis)
    override fun applyInitialFilter(filter: String?, startMillis: Long?, endMillis: Long?) = viewModel.applyInitialFilter(filter, startMillis, endMillis)
    override fun resetToDefault() = viewModel.resetToDefault()
    override fun exportToExcel(context: Context) = viewModel.exportToExcel(context)
    override fun clearExportState() = viewModel.clearExportState()
}
