package com.ptpws.ikikasir.feature.antrean.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.antrean.domain.usecase.GetQueueHistoryUseCase
import com.ptpws.ikikasir.feature.antrean.domain.usecase.InsertQueueHistoryUseCase
import com.ptpws.ikikasir.feature.antrean.presentation.state.QueueHistoryUiState
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetAllTransaksiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

import com.ptpws.ikikasir.commond.formatDateRangeLabel

private const val TAG = "QueueHistoryVM"

@HiltViewModel
class QueueHistoryViewModel @Inject constructor(
    private val getQueueHistoryUseCase: GetQueueHistoryUseCase,
    private val insertQueueHistoryUseCase: InsertQueueHistoryUseCase,
    private val getAllTransaksiUseCase: GetAllTransaksiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<QueueHistoryUiState>(QueueHistoryUiState.Loading)
    val uiState: StateFlow<QueueHistoryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("SEMUA") // "SEMUA", "HARI_INI", "7_HARI_TERAKHIR", "FILTER_TANGGAL"
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _startDateMillis = MutableStateFlow<Long?>(null)
    val startDateMillis: StateFlow<Long?> = _startDateMillis.asStateFlow()

    private val _endDateMillis = MutableStateFlow<Long?>(null)
    val endDateMillis: StateFlow<Long?> = _endDateMillis.asStateFlow()

    private val _customDateLabel = MutableStateFlow<String?>(null)
    val customDateLabel: StateFlow<String?> = _customDateLabel.asStateFlow()

    private val _transaksiMap = MutableStateFlow<Map<String, PenjualanTransaksi>>(emptyMap())
    val transaksiMap: StateFlow<Map<String, PenjualanTransaksi>> = _transaksiMap.asStateFlow()

    init {
        loadHistory()
        loadTransaksiMap()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getQueueHistoryUseCase()
                .catch { e ->
                    Log.e(TAG, "Error loading queueHistory: ${e.message}", e)
                    _uiState.value = QueueHistoryUiState.Error(e.message ?: "Terjadi kesalahan")
                }
                .collectLatest { list ->
                    _uiState.value = QueueHistoryUiState.Success(list)
                }
        }
    }

    private fun loadTransaksiMap() {
        viewModelScope.launch {
            getAllTransaksiUseCase()
                .catch { e -> Log.e(TAG, "Error loading transactions: ${e.message}") }
                .collectLatest { list ->
                    val map = list.associateBy { it.transactionId }
                    _transaksiMap.value = map
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        if (filter != "FILTER_TANGGAL") {
            _startDateMillis.value = null
            _endDateMillis.value = null
            _customDateLabel.value = null
        }
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _startDateMillis.value = startMillis
        _endDateMillis.value = endMillis
        _customDateLabel.value = formatDateRangeLabel(startMillis, endMillis)
        _selectedFilter.value = "FILTER_TANGGAL"
    }

    fun addHistory(history: QueueHistory) {
        viewModelScope.launch {
            try {
                insertQueueHistoryUseCase(history).collect {}
                Log.d(TAG, "QueueHistory added successfully: ${history.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed adding QueueHistory: ${e.message}", e)
            }
        }
    }

    fun groupHistoryByDate(items: List<QueueHistory>): Map<String, List<QueueHistory>> {
        val todayCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        val sdfFull = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

        return items.groupBy { history ->
            val date = history.completedAt.toDate()
            val itemCal = Calendar.getInstance().apply { time = date }

            when {
                isSameDay(itemCal, todayCal) -> "Hari Ini, ${sdfFull.format(date)}"
                isSameDay(itemCal, yesterdayCal) -> "Kemarin, ${sdfFull.format(date)}"
                else -> sdfFull.format(date)
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
