package com.ptpws.ikikasir.feature.laporanpenjualan.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.commond.formatDateRangeLabel
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.LaporanPenjualanSummary
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.ProdukTerjualItem
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.SortByLaporanPenjualan
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.usecase.ExportLaporanPenjualanUseCase
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.usecase.GetLaporanPenjualanUseCase
import com.ptpws.ikikasir.feature.laporanpenjualan.presentation.state.LaporanPenjualanState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaporanPenjualanViewModel @Inject constructor(
    private val getLaporanPenjualanUseCase: GetLaporanPenjualanUseCase,
    private val exportLaporanPenjualanUseCase: ExportLaporanPenjualanUseCase,
    private val logActivityUseCase: LogActivityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LaporanPenjualanState())
    val state: StateFlow<LaporanPenjualanState> = _state.asStateFlow()

    private var rawSummary = LaporanPenjualanSummary()
    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    fun setPeriode(periode: PeriodeLaporanPenjualan) {
        if (_state.value.selectedPeriode == periode && _state.value.customStartDateMillis == null) {
            return
        }
        _state.update {
            it.copy(
                selectedPeriode = periode,
                customStartDateMillis = null,
                customEndDateMillis = null,
                customDateLabel = null
            )
        }
        loadData()
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        val label = formatDateRangeLabel(startMillis, endMillis)
        _state.update {
            it.copy(
                selectedPeriode = PeriodeLaporanPenjualan.KUSTOM,
                customStartDateMillis = startMillis,
                customEndDateMillis = endMillis,
                customDateLabel = label
            )
        }
        loadData()
    }

    fun setSortBy(sortBy: SortByLaporanPenjualan) {
        _state.update { it.copy(selectedSortBy = sortBy) }
        applySearchAndSort()
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applySearchAndSort()
    }

    private fun loadData() {
        val st = _state.value
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getLaporanPenjualanUseCase(
                periode = st.selectedPeriode,
                customStartMillis = st.customStartDateMillis,
                customEndMillis = st.customEndDateMillis
            ).collect { summary ->
                rawSummary = summary
                _state.update { current ->
                    current.copy(
                        summary = summary,
                        isLoading = false
                    )
                }
                applySearchAndSort()
            }
        }
    }

    private fun applySearchAndSort() {
        val st = _state.value
        val query = st.searchQuery.trim().lowercase()

        var items = rawSummary.items

        if (query.isNotBlank()) {
            items = items.filter {
                it.namaProduk.lowercase().contains(query) ||
                        it.kategori.lowercase().contains(query)
            }
        }

        items = when (st.selectedSortBy) {
            SortByLaporanPenjualan.TERBANYAK_QTY -> items.sortedByDescending { it.unitTerjual }
            SortByLaporanPenjualan.TERBESAR_OMZET -> items.sortedByDescending { it.totalOmzet }
            SortByLaporanPenjualan.NAMA_AZ -> items.sortedBy { it.namaProduk.lowercase() }
        }

        _state.update { it.copy(displayedItems = items) }
    }

    fun exportToExcel(context: Context) {
        val currentSummary = _state.value.summary
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportError = null) }
            val result = exportLaporanPenjualanUseCase(context, currentSummary)
            result.fold(
                onSuccess = { uri ->
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportedFileUri = uri,
                            exportError = null,
                            exportMessage = "Laporan Penjualan berhasil diekspor ke Excel!"
                        )
                    }
                    logActivityUseCase(
                        title = "Ekspor Laporan Penjualan",
                        description = "Laporan penjualan produk (${currentSummary.items.size} item) berhasil diekspor ke Excel.",
                        category = "LAPORAN",
                        action = "EXPORT",
                        isWarning = false
                    )
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportError = error.message ?: "Gagal mengekspor laporan penjualan.",
                            exportMessage = null
                        )
                    }
                }
            )
        }
    }

    fun clearExportState() {
        _state.update { it.copy(exportedFileUri = null, exportError = null, exportMessage = null) }
    }
}
