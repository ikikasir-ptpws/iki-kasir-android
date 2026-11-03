package com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.commond.formatDateRangeLabel
import com.ptpws.ikikasir.commond.getEndOfDayLocalSeconds
import com.ptpws.ikikasir.commond.getStartOfDayLocalSeconds
import com.ptpws.ikikasir.core.util.ExcelExportService
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.keuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.keuangan.domain.repository.CatatanKeuanganRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class GroupedCatatanKeuangan(
    val dateHeader: String,
    val items: List<CatatanKeuangan>
)

data class CatatanKeuanganUiState(
    val rawList: List<CatatanKeuangan> = emptyList(),
    val filteredList: List<CatatanKeuangan> = emptyList(),
    val groupedList: List<GroupedCatatanKeuangan> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Semua", // "Semua", "Pemasukan", "Pengeluaran", "Filter Tanggal"
    val customStartDateMillis: Long? = null,
    val customEndDateMillis: Long? = null,
    val customDateLabel: String? = null,
    val totalPemasukan: Double = 0.0,
    val totalPengeluaran: Double = 0.0,
    val saldoBersih: Double = 0.0,
    val totalTransaksiCount: Int = 0,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val exportedFileUri: Uri? = null,
    val exportError: String? = null
)

@HiltViewModel
class CatatanKeuanganViewModel @Inject constructor(
    private val repository: CatatanKeuanganRepository,
    private val excelExportService: ExcelExportService,
    private val logActivityUseCase: LogActivityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CatatanKeuanganUiState())
    val state: StateFlow<CatatanKeuanganUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getAllCatatanFlow().collect { list ->
                _state.update { current ->
                    val filtered = applyFilterAndSearch(
                        list = list,
                        query = current.searchQuery,
                        filter = current.selectedFilter,
                        startMillis = current.customStartDateMillis,
                        endMillis = current.customEndDateMillis
                    )
                    val grouped = groupListByDate(filtered)

                    val pemasukan = filtered.filter { it.jenis.equals("PEMASUKAN", ignoreCase = true) }.sumOf { it.nominal }
                    val pengeluaran = filtered.filter { it.jenis.equals("PENGELUARAN", ignoreCase = true) }.sumOf { it.nominal }

                    current.copy(
                        rawList = list,
                        filteredList = filtered,
                        groupedList = grouped,
                        totalPemasukan = pemasukan,
                        totalPengeluaran = pengeluaran,
                        saldoBersih = pemasukan - pengeluaran,
                        totalTransaksiCount = filtered.size,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { current ->
            val filtered = applyFilterAndSearch(
                list = current.rawList,
                query = query,
                filter = current.selectedFilter,
                startMillis = current.customStartDateMillis,
                endMillis = current.customEndDateMillis
            )
            val grouped = groupListByDate(filtered)
            val pemasukan = filtered.filter { it.jenis.equals("PEMASUKAN", ignoreCase = true) }.sumOf { it.nominal }
            val pengeluaran = filtered.filter { it.jenis.equals("PENGELUARAN", ignoreCase = true) }.sumOf { it.nominal }

            current.copy(
                searchQuery = query,
                filteredList = filtered,
                groupedList = grouped,
                totalPemasukan = pemasukan,
                totalPengeluaran = pengeluaran,
                saldoBersih = pemasukan - pengeluaran,
                totalTransaksiCount = filtered.size
            )
        }
    }

    fun onFilterSelected(filter: String) {
        _state.update { current ->
            val filtered = applyFilterAndSearch(
                list = current.rawList,
                query = current.searchQuery,
                filter = filter,
                startMillis = if (filter == "Filter Tanggal") current.customStartDateMillis else null,
                endMillis = if (filter == "Filter Tanggal") current.customEndDateMillis else null
            )
            val grouped = groupListByDate(filtered)
            val pemasukan = filtered.filter { it.jenis.equals("PEMASUKAN", ignoreCase = true) }.sumOf { it.nominal }
            val pengeluaran = filtered.filter { it.jenis.equals("PENGELUARAN", ignoreCase = true) }.sumOf { it.nominal }

            current.copy(
                selectedFilter = filter,
                customStartDateMillis = if (filter == "Filter Tanggal") current.customStartDateMillis else null,
                customEndDateMillis = if (filter == "Filter Tanggal") current.customEndDateMillis else null,
                customDateLabel = if (filter == "Filter Tanggal") current.customDateLabel else null,
                filteredList = filtered,
                groupedList = grouped,
                totalPemasukan = pemasukan,
                totalPengeluaran = pengeluaran,
                saldoBersih = pemasukan - pengeluaran,
                totalTransaksiCount = filtered.size
            )
        }
    }

    fun onCustomDateRangeSelected(startMillis: Long, endMillis: Long) {
        val label = formatDateRangeLabel(startMillis, endMillis)
        _state.update { current ->
            val filtered = applyFilterAndSearch(
                list = current.rawList,
                query = current.searchQuery,
                filter = "Filter Tanggal",
                startMillis = startMillis,
                endMillis = endMillis
            )
            val grouped = groupListByDate(filtered)
            val pemasukan = filtered.filter { it.jenis.equals("PEMASUKAN", ignoreCase = true) }.sumOf { it.nominal }
            val pengeluaran = filtered.filter { it.jenis.equals("PENGELUARAN", ignoreCase = true) }.sumOf { it.nominal }

            current.copy(
                selectedFilter = "Filter Tanggal",
                customStartDateMillis = startMillis,
                customEndDateMillis = endMillis,
                customDateLabel = label,
                filteredList = filtered,
                groupedList = grouped,
                totalPemasukan = pemasukan,
                totalPengeluaran = pengeluaran,
                saldoBersih = pemasukan - pengeluaran,
                totalTransaksiCount = filtered.size
            )
        }
    }

    fun deleteCatatan(id: String) {
        viewModelScope.launch {
            val item = repository.getCatatanById(id)
            repository.deleteCatatanById(id)
            if (item != null) {
                logActivityUseCase(
                    title = "Hapus Catatan Keuangan",
                    description = "Menghapus catatan ${item.jenis}: ${item.judul}",
                    category = "KEUANGAN",
                    action = "DELETE",
                    isWarning = true
                )
            }
        }
    }

    fun exportToExcel(context: Context) {
        val current = _state.value
        val dataToExport = current.filteredList
        val label = if (current.selectedFilter == "Filter Tanggal") current.customDateLabel ?: "Filter Tanggal" else current.selectedFilter

        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportError = null) }
            val result = excelExportService.exportCatatanKeuangan(context, dataToExport, label)
            result.fold(
                onSuccess = { uri ->
                    _state.update { it.copy(isExporting = false, exportedFileUri = uri) }
                    logActivityUseCase(
                        title = "Ekspor Catatan Keuangan",
                        description = "Data catatan keuangan (${dataToExport.size} item) berhasil diekspor ke Excel.",
                        category = "KEUANGAN",
                        action = "EXPORT",
                        isWarning = false
                    )
                },
                onFailure = { error ->
                    _state.update { it.copy(isExporting = false, exportError = error.message ?: "Gagal ekspor excel.") }
                }
            )
        }
    }

    fun clearExportState() {
        _state.update { it.copy(exportedFileUri = null, exportError = null) }
    }

    private fun applyFilterAndSearch(
        list: List<CatatanKeuangan>,
        query: String,
        filter: String,
        startMillis: Long?,
        endMillis: Long?
    ): List<CatatanKeuangan> {
        var res = list

        if (query.isNotBlank()) {
            res = res.filter {
                it.judul.contains(query, ignoreCase = true) ||
                        it.keterangan.contains(query, ignoreCase = true) ||
                        it.metodePembayaran.contains(query, ignoreCase = true)
            }
        }

        when (filter) {
            "Pemasukan" -> {
                res = res.filter { it.jenis.equals("PEMASUKAN", ignoreCase = true) }
            }
            "Pengeluaran" -> {
                res = res.filter { it.jenis.equals("PENGELUARAN", ignoreCase = true) }
            }
            "Filter Tanggal" -> {
                if (startMillis != null && endMillis != null) {
                    val startSec = getStartOfDayLocalSeconds(startMillis)
                    val endSec = getEndOfDayLocalSeconds(endMillis)
                    res = res.filter { (it.tanggalMillis / 1000) in startSec..endSec }
                }
            }
        }

        return res.sortedByDescending { it.tanggalMillis }
    }

    private fun groupListByDate(list: List<CatatanKeuangan>): List<GroupedCatatanKeuangan> {
        if (list.isEmpty()) return emptyList()
        val map = LinkedHashMap<String, MutableList<CatatanKeuangan>>()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

        for (item in list) {
            val header = dateFormat.format(Date(item.tanggalMillis)).uppercase(Locale("id", "ID"))
            map.getOrPut(header) { mutableListOf() }.add(item)
        }

        return map.map { (header, items) ->
            GroupedCatatanKeuangan(
                dateHeader = header,
                items = items.sortedByDescending { it.tanggalMillis }
            )
        }
    }
}
