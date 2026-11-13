package com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk CatatanKeuanganUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface CatatanKeuanganUiProvider {
    val state: StateFlow<CatatanKeuanganUiState>
    fun onSearchQueryChanged(query: String)
    fun onFilterSelected(filter: String)
    fun onCustomDateRangeSelected(startMillis: Long, endMillis: Long)
    fun deleteCatatan(id: String)
    fun exportToExcel(context: Context)
    fun clearExportState()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke CatatanKeuanganViewModel asli.
 */
class HiltCatatanKeuanganUiProvider(private val viewModel: CatatanKeuanganViewModel) : CatatanKeuanganUiProvider {
    override val state: StateFlow<CatatanKeuanganUiState> get() = viewModel.state
    override fun onSearchQueryChanged(query: String) = viewModel.onSearchQueryChanged(query)
    override fun onFilterSelected(filter: String) = viewModel.onFilterSelected(filter)
    override fun onCustomDateRangeSelected(startMillis: Long, endMillis: Long) = viewModel.onCustomDateRangeSelected(startMillis, endMillis)
    override fun deleteCatatan(id: String) = viewModel.deleteCatatan(id)
    override fun exportToExcel(context: Context) = viewModel.exportToExcel(context)
    override fun clearExportState() = viewModel.clearExportState()
}
