package com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.commond.formatDateRangeLabel
import com.ptpws.ikikasir.commond.getEndOfDayLocalSeconds
import com.ptpws.ikikasir.commond.getStartOfDayLocalSeconds
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetAllTransaksiUseCase
import com.ptpws.ikikasir.feature.penjualan.presentation.state.GroupedTransaksi
import com.ptpws.ikikasir.feature.penjualan.presentation.state.RiwayatTransaksiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
                    val filtered = filterTransactions(
                        list = list,
                        query = current.searchQuery,
                        filter = current.selectedFilter,
                        startMillis = current.startDateMillis,
                        endMillis = current.endDateMillis,
                        customDateMillis = current.selectedCustomDateMillis
                    )
                    val grouped = groupTransactions(filtered)
                    current.copy(
                        transaksiList = list,
                        filteredList = filtered,
                        groupedTransactions = grouped,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { current ->
            val filtered = filterTransactions(
                list = current.transaksiList,
                query = query,
                filter = current.selectedFilter,
                startMillis = current.startDateMillis,
                endMillis = current.endDateMillis,
                customDateMillis = current.selectedCustomDateMillis
            )
            val grouped = groupTransactions(filtered)
            current.copy(
                searchQuery = query,
                filteredList = filtered,
                groupedTransactions = grouped
            )
        }
    }

    fun onFilterSelect(filter: String) {
        _state.update { current ->
            val filtered = filterTransactions(
                list = current.transaksiList,
                query = current.searchQuery,
                filter = filter,
                startMillis = current.startDateMillis,
                endMillis = current.endDateMillis,
                customDateMillis = current.selectedCustomDateMillis
            )
            val grouped = groupTransactions(filtered)
            current.copy(
                selectedFilter = filter,
                filteredList = filtered,
                groupedTransactions = grouped
            )
        }
    }

    fun onCustomDateRangeSelect(startMillis: Long, endMillis: Long) {
        val dateLabel = formatDateRangeLabel(startMillis, endMillis)
        _state.update { current ->
            val filtered = filterTransactions(
                list = current.transaksiList,
                query = current.searchQuery,
                filter = "Filter Tanggal",
                startMillis = startMillis,
                endMillis = endMillis,
                customDateMillis = null
            )
            val grouped = groupTransactions(filtered)
            current.copy(
                selectedFilter = "Filter Tanggal",
                startDateMillis = startMillis,
                endDateMillis = endMillis,
                selectedCustomDateMillis = null,
                customDateLabel = dateLabel,
                filteredList = filtered,
                groupedTransactions = grouped
            )
        }
    }

    private fun filterTransactions(
        list: List<PenjualanTransaksi>,
        query: String,
        filter: String,
        startMillis: Long?,
        endMillis: Long?,
        customDateMillis: Long?
    ): List<PenjualanTransaksi> {
        var filtered = list

        // Search query filter (by transaction number or ID)
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.transactionNumber.contains(query, ignoreCase = true) ||
                        it.transactionId.contains(query, ignoreCase = true)
            }
        }

        // Date filter
        when (filter) {
            "Semua" -> {
                // Show all transactions
            }
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
            "Filter Tanggal" -> {
                if (startMillis != null && endMillis != null) {
                    val startSec = getStartOfDayLocalSeconds(startMillis)
                    val endSec = getEndOfDayLocalSeconds(endMillis)
                    filtered = filtered.filter {
                        it.createdAt.seconds in startSec..endSec
                    }
                } else if (customDateMillis != null) {
                    val startOfSelDay = Calendar.getInstance().apply {
                        timeInMillis = customDateMillis
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis / 1000

                    val endOfSelDay = Calendar.getInstance().apply {
                        timeInMillis = customDateMillis
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis / 1000

                    filtered = filtered.filter {
                        it.createdAt.seconds in startOfSelDay..endOfSelDay
                    }
                }
            }
        }

        return filtered.sortedByDescending { it.createdAt.seconds }
    }

    private fun groupTransactions(filteredList: List<PenjualanTransaksi>): List<GroupedTransaksi> {
        if (filteredList.isEmpty()) return emptyList()

        val map = LinkedHashMap<String, MutableList<PenjualanTransaksi>>()

        for (tx in filteredList) {
            val dateObj = tx.createdAt.toDate()
            val headerText = formatGroupHeaderDate(dateObj)
            map.getOrPut(headerText) { mutableListOf() }.add(tx)
        }

        return map.map { (header, list) ->
            GroupedTransaksi(
                dateHeader = header,
                countText = "${list.size} Transaksi",
                transactions = list
            )
        }
    }

    private fun formatGroupHeaderDate(date: Date): String {
        val txCal = Calendar.getInstance().apply { time = date }
        val todayCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
        val dayNameFormat = SimpleDateFormat("EEEE, d MMM yyyy", Locale("id", "ID"))

        return when {
            isSameDay(txCal, todayCal) -> "Hari Ini, ${dateFormat.format(date)}"
            isSameDay(txCal, yesterdayCal) -> "Kemarin, ${dateFormat.format(date)}"
            else -> dayNameFormat.format(date)
        }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }
}
