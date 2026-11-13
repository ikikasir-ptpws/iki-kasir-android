package com.ptpws.ikikasir.feature.auditlog.presentation.viewmodel

import com.ptpws.ikikasir.feature.auditlog.presentation.state.AuditLogState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk AuditLogUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface AuditLogUiProvider {
    val state: StateFlow<AuditLogState>
    fun onSearchQueryChanged(query: String)
    fun onCategorySelected(category: String)
    fun onDateFilterSelected(dateFilter: String)
    fun onCustomDateRangeSelected(startDateMillis: Long, endDateMillis: Long)
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke AuditLogViewModel asli.
 */
class HiltAuditLogUiProvider(private val viewModel: AuditLogViewModel) : AuditLogUiProvider {
    override val state: StateFlow<AuditLogState> get() = viewModel.state
    override fun onSearchQueryChanged(query: String) = viewModel.onSearchQueryChanged(query)
    override fun onCategorySelected(category: String) = viewModel.onCategorySelected(category)
    override fun onDateFilterSelected(dateFilter: String) = viewModel.onDateFilterSelected(dateFilter)
    override fun onCustomDateRangeSelected(startDateMillis: Long, endDateMillis: Long) = viewModel.onCustomDateRangeSelected(startDateMillis, endDateMillis)
}
