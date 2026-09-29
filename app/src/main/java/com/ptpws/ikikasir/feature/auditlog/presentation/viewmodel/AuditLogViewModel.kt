package com.ptpws.ikikasir.feature.auditlog.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.commond.formatDateRangeLabel
import com.ptpws.ikikasir.commond.getEndOfDayLocalSeconds
import com.ptpws.ikikasir.commond.getStartOfDayLocalSeconds
import com.ptpws.ikikasir.feature.auditlog.domain.model.AuditLog
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.GetAuditLogsUseCase
import com.ptpws.ikikasir.feature.auditlog.presentation.state.AuditLogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AuditLogViewModel @Inject constructor(
    private val getAuditLogsUseCase: GetAuditLogsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuditLogState())
    val state: StateFlow<AuditLogState> = _state.asStateFlow()

    init {
        loadAuditLogs()
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getAuditLogsUseCase()
                .catch { e ->
                    _state.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { logs ->
                    _state.update { currentState ->
                        val filtered = applyFilters(
                            logs = logs,
                            query = currentState.searchQuery,
                            category = currentState.selectedCategory,
                            dateFilter = currentState.selectedDateFilter,
                            startDateMillis = currentState.startDateMillis,
                            endDateMillis = currentState.endDateMillis
                        )
                        currentState.copy(
                            auditLogs = logs,
                            filteredLogs = filtered,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { currentState ->
            val filtered = applyFilters(
                logs = currentState.auditLogs,
                query = query,
                category = currentState.selectedCategory,
                dateFilter = currentState.selectedDateFilter,
                startDateMillis = currentState.startDateMillis,
                endDateMillis = currentState.endDateMillis
            )
            currentState.copy(searchQuery = query, filteredLogs = filtered)
        }
    }

    fun onCategorySelected(category: String) {
        _state.update { currentState ->
            val filtered = applyFilters(
                logs = currentState.auditLogs,
                query = currentState.searchQuery,
                category = category,
                dateFilter = currentState.selectedDateFilter,
                startDateMillis = currentState.startDateMillis,
                endDateMillis = currentState.endDateMillis
            )
            currentState.copy(selectedCategory = category, filteredLogs = filtered)
        }
    }

    fun onDateFilterSelected(dateFilter: String) {
        _state.update { currentState ->
            val startDateMillis = if (dateFilter == "Filter Tanggal") currentState.startDateMillis else null
            val endDateMillis = if (dateFilter == "Filter Tanggal") currentState.endDateMillis else null
            val filtered = applyFilters(
                logs = currentState.auditLogs,
                query = currentState.searchQuery,
                category = currentState.selectedCategory,
                dateFilter = dateFilter,
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis
            )
            currentState.copy(
                selectedDateFilter = dateFilter,
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis,
                customDateLabel = if (dateFilter == "Filter Tanggal") currentState.customDateLabel else null,
                filteredLogs = filtered
            )
        }
    }

    fun onCustomDateRangeSelected(startDateMillis: Long, endDateMillis: Long) {
        _state.update { currentState ->
            val filtered = applyFilters(
                logs = currentState.auditLogs,
                query = currentState.searchQuery,
                category = currentState.selectedCategory,
                dateFilter = "Filter Tanggal",
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis
            )
            currentState.copy(
                selectedDateFilter = "Filter Tanggal",
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis,
                customDateLabel = formatDateRangeLabel(startDateMillis, endDateMillis),
                filteredLogs = filtered
            )
        }
    }

    private fun applyFilters(
        logs: List<AuditLog>,
        query: String,
        category: String,
        dateFilter: String,
        startDateMillis: Long?,
        endDateMillis: Long?
    ): List<AuditLog> {
        val now = Calendar.getInstance()

        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return logs.filter { log ->
            // Category filter
            val matchesCategory = when (category) {
                "Transaksi" -> log.category.equals("TRANSACTION", ignoreCase = true)
                "Stok" -> log.category.equals("STOCK", ignoreCase = true)
                "Harga" -> log.category.equals("PRICE", ignoreCase = true)
                "Autentikasi" -> log.category.equals("AUTHENTICATION", ignoreCase = true)
                "Promo" -> log.category.equals("PROMO", ignoreCase = true)
                "Sistem" -> log.category.equals("SYSTEM", ignoreCase = true)
                else -> true
            }

            // Date filter
            val matchesDate = when (dateFilter) {
                "Hari Ini" -> log.timestamp >= startOfToday
                "7 Hari Terakhir" -> log.timestamp >= sevenDaysAgo
                "Filter Tanggal" -> {
                    if (startDateMillis != null && endDateMillis != null) {
                        val startMillis = getStartOfDayLocalSeconds(startDateMillis) * 1000
                        val endMillis = getEndOfDayLocalSeconds(endDateMillis) * 1000 + 999
                        log.timestamp in startMillis..endMillis
                    } else {
                        true
                    }
                }
                else -> true
            }

            // Search query filter
            val matchesQuery = query.isBlank() ||
                    log.title.contains(query, ignoreCase = true) ||
                    log.description.contains(query, ignoreCase = true) ||
                    log.actorName.contains(query, ignoreCase = true)

            matchesCategory && matchesDate && matchesQuery
        }
    }
}
