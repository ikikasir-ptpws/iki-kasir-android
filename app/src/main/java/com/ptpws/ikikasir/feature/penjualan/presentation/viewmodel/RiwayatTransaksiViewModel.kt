package com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetAllTransaksiUseCase
import com.ptpws.ikikasir.feature.penjualan.presentation.state.RiwayatTransaksiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class RiwayatTransaksiViewModel @Inject constructor(
    private val getAllTransaksiUseCase: GetAllTransaksiUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RiwayatTransaksiState())
    val state: StateFlow<RiwayatTransaksiState> = _state.asStateFlow()

    init {
        loadTransaksi()
    }

    private fun loadTransaksi() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllTransaksiUseCase().collect { list ->
                _state.update { current ->
                    val filtered = filterTransactions(list, current.searchQuery, current.selectedFilter)
                    current.copy(
                        transaksiList = list,
                        filteredList = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { current ->
            val filtered = filterTransactions(current.transaksiList, query, current.selectedFilter)
            current.copy(searchQuery = query, filteredList = filtered)
        }
    }

    fun onFilterSelect(filter: String) {
        _state.update { current ->
            val filtered = filterTransactions(current.transaksiList, current.searchQuery, filter)
            current.copy(selectedFilter = filter, filteredList = filtered)
        }
    }

    private fun filterTransactions(
        list: List<PenjualanTransaksi>,
        query: String,
        filter: String
    ): List<PenjualanTransaksi> {
        var filtered = list

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.transactionNumber.contains(query, ignoreCase = true) ||
                        it.transactionId.contains(query, ignoreCase = true)
            }
        }

        val now = Calendar.getInstance()
        when (filter) {
            "Hari Ini" -> {
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis / 1000

                filtered = filtered.filter {
                    it.createdAt.seconds >= startOfDay
                }
            }
            "7 Hari Terakhir" -> {
                val startOf7Days = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis / 1000

                filtered = filtered.filter {
                    it.createdAt.seconds >= startOf7Days
                }
            }
        }

        return filtered.sortedByDescending { it.createdAt.seconds }
    }
}
