package com.ptpws.ikikasir.feature.antrean.presentation.viewmodel

import android.content.Context
import android.net.Uri
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.antrean.presentation.state.QueueHistoryUiState
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk QueueHistoryUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface QueueHistoryUiProvider {
    val uiState: StateFlow<QueueHistoryUiState>
    val searchQuery: StateFlow<String>
    val selectedFilter: StateFlow<String>
    val startDateMillis: StateFlow<Long?>
    val endDateMillis: StateFlow<Long?>
    val customDateLabel: StateFlow<String?>
    val transaksiMap: StateFlow<Map<String, PenjualanTransaksi>>
    val isExporting: StateFlow<Boolean>
    val exportedFileUri: StateFlow<Uri?>
    val exportError: StateFlow<String?>
    fun onSearchQueryChanged(query: String)
    fun setFilter(filter: String)
    fun setCustomDateRange(startMillis: Long, endMillis: Long)
    fun resetToDefault()
    fun exportToExcel(context: Context, filteredItems: List<QueueHistory>)
    fun clearExportState()
    fun groupHistoryByDate(items: List<QueueHistory>): Map<String, List<QueueHistory>>
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke QueueHistoryViewModel asli.
 */
class HiltQueueHistoryUiProvider(private val viewModel: QueueHistoryViewModel) : QueueHistoryUiProvider {
    override val uiState: StateFlow<QueueHistoryUiState> get() = viewModel.uiState
    override val searchQuery: StateFlow<String> get() = viewModel.searchQuery
    override val selectedFilter: StateFlow<String> get() = viewModel.selectedFilter
    override val startDateMillis: StateFlow<Long?> get() = viewModel.startDateMillis
    override val endDateMillis: StateFlow<Long?> get() = viewModel.endDateMillis
    override val customDateLabel: StateFlow<String?> get() = viewModel.customDateLabel
    override val transaksiMap: StateFlow<Map<String, PenjualanTransaksi>> get() = viewModel.transaksiMap
    override val isExporting: StateFlow<Boolean> get() = viewModel.isExporting
    override val exportedFileUri: StateFlow<Uri?> get() = viewModel.exportedFileUri
    override val exportError: StateFlow<String?> get() = viewModel.exportError
    override fun onSearchQueryChanged(query: String) = viewModel.onSearchQueryChanged(query)
    override fun setFilter(filter: String) = viewModel.setFilter(filter)
    override fun setCustomDateRange(startMillis: Long, endMillis: Long) = viewModel.setCustomDateRange(startMillis, endMillis)
    override fun resetToDefault() = viewModel.resetToDefault()
    override fun exportToExcel(context: Context, filteredItems: List<QueueHistory>) = viewModel.exportToExcel(context, filteredItems)
    override fun clearExportState() = viewModel.clearExportState()
    override fun groupHistoryByDate(items: List<QueueHistory>) = viewModel.groupHistoryByDate(items)
}
