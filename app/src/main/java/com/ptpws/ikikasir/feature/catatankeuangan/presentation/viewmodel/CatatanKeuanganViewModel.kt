package com.ptpws.ikikasir.feature.catatankeuangan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.commond.formatDateRangeLabel
import com.ptpws.ikikasir.commond.getEndOfDayLocalSeconds
import com.ptpws.ikikasir.commond.getStartOfDayLocalSeconds
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase.DeleteCatatanKeuanganUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase.GetCatatanKeuanganListUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase.InsertCatatanKeuanganUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase.UpdateCatatanKeuanganUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.presentation.state.CatatanKeuanganState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CatatanKeuanganViewModel @Inject constructor(
    private val getCatatanKeuanganListUseCase: GetCatatanKeuanganListUseCase,
    private val insertCatatanKeuanganUseCase: InsertCatatanKeuanganUseCase,
    private val updateCatatanKeuanganUseCase: UpdateCatatanKeuanganUseCase,
    private val deleteCatatanKeuanganUseCase: DeleteCatatanKeuanganUseCase,
    private val logActivityUseCase: LogActivityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CatatanKeuanganState(isLoading = true))
    val state: StateFlow<CatatanKeuanganState> = _state.asStateFlow()

    init {
        refreshCatatanList()
    }

    fun refreshCatatanList() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getCatatanKeuanganListUseCase().collect { list ->
                _state.update { currentState ->
                    val totalPemasukan = list.filter { it.isPemasukan }.sumOf { it.amount }
                    val totalPengeluaran = list.filter { it.isPengeluaran }.sumOf { it.amount }
                    val saldoBersih = totalPemasukan - totalPengeluaran

                    val filtered = applyFilterAndSearch(
                        list,
                        currentState.searchQuery,
                        currentState.selectedFilterTab,
                        currentState.startDateMillis,
                        currentState.endDateMillis
                    )
                    currentState.copy(
                        catatanList = list,
                        filteredList = filtered,
                        totalPemasukan = totalPemasukan,
                        totalPengeluaran = totalPengeluaran,
                        saldoBersih = saldoBersih,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { currentState ->
            val filtered = applyFilterAndSearch(
                currentState.catatanList,
                query,
                currentState.selectedFilterTab,
                currentState.startDateMillis,
                currentState.endDateMillis
            )
            currentState.copy(searchQuery = query, filteredList = filtered)
        }
    }

    fun onFilterTabSelected(tab: String) {
        _state.update { currentState ->
            val filtered = applyFilterAndSearch(
                currentState.catatanList,
                currentState.searchQuery,
                tab,
                if (tab == "Filter Tanggal") currentState.startDateMillis else null,
                if (tab == "Filter Tanggal") currentState.endDateMillis else null
            )
            currentState.copy(
                selectedFilterTab = tab,
                startDateMillis = if (tab == "Filter Tanggal") currentState.startDateMillis else null,
                endDateMillis = if (tab == "Filter Tanggal") currentState.endDateMillis else null,
                customDateLabel = if (tab == "Filter Tanggal") currentState.customDateLabel else null,
                filteredList = filtered
            )
        }
    }

    fun onDateRangeSelected(startMillis: Long, endMillis: Long) {
        _state.update { currentState ->
            val filtered = applyFilterAndSearch(
                currentState.catatanList,
                currentState.searchQuery,
                "Filter Tanggal",
                startMillis,
                endMillis
            )
            currentState.copy(
                selectedFilterTab = "Filter Tanggal",
                startDateMillis = startMillis,
                endDateMillis = endMillis,
                customDateLabel = formatDateRangeLabel(startMillis, endMillis),
                filteredList = filtered
            )
        }
    }

    fun clearDateRange() {
        _state.update { currentState ->
            val filtered = applyFilterAndSearch(
                currentState.catatanList,
                currentState.searchQuery,
                "Semua",
                null,
                null
            )
            currentState.copy(
                selectedFilterTab = "Semua",
                startDateMillis = null,
                endDateMillis = null,
                customDateLabel = null,
                filteredList = filtered
            )
        }
    }

    fun simpanCatatan(
        id: String?,
        title: String,
        type: String,
        amount: Double,
        date: String,
        note: String
    ) {
        if (title.isBlank()) {
            GlobalCrudResultDialog.failure("Judul catatan tidak boleh kosong.")
            return
        }
        if (amount <= 0) {
            GlobalCrudResultDialog.failure("Jumlah nominal harus lebih dari 0.")
            return
        }

        val isEdit = !id.isNullOrBlank()
        val targetId = id ?: UUID.randomUUID().toString()

        val item = CatatanKeuangan(
            id = targetId,
            title = title,
            type = type,
            amount = amount,
            date = date,
            note = note,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            isSynced = false
        )

        viewModelScope.launch {
            val flow = if (isEdit) updateCatatanKeuanganUseCase(item) else insertCatatanKeuanganUseCase(item)
            flow.collect { result ->
                result.onSuccess {
                    val labelType = if (type == "PEMASUKAN") "Pemasukan" else "Pengeluaran"
                    val actionName = if (isEdit) "UPDATE" else "CREATE"
                    GlobalCrudResultDialog.success("Catatan $labelType \"$title\" berhasil disimpan.")
                    logActivityUseCase(
                        title = "Catatan Keuangan: $title",
                        description = "Catatan $labelType sebesar Rp ${amount.toLong()} tersimpan.",
                        category = "KEUANGAN",
                        action = actionName,
                        isWarning = false
                    )
                }.onFailure { err ->
                    GlobalCrudResultDialog.failure(err.message ?: "Gagal menyimpan catatan keuangan.")
                }
            }
        }
    }

    fun deleteCatatan(id: String, title: String) {
        viewModelScope.launch {
            deleteCatatanKeuanganUseCase(id).collect { result ->
                result.onSuccess {
                    GlobalCrudResultDialog.success("Catatan \"$title\" berhasil dihapus.")
                    logActivityUseCase(
                        title = "Hapus Catatan Keuangan: $title",
                        description = "Catatan keuangan $title telah dihapus.",
                        category = "KEUANGAN",
                        action = "DELETE",
                        isWarning = true
                    )
                }.onFailure { err ->
                    GlobalCrudResultDialog.failure(err.message ?: "Gagal menghapus catatan keuangan.")
                }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(userMessage = null, errorMessage = null) }
    }

    private fun applyFilterAndSearch(
        list: List<CatatanKeuangan>,
        query: String,
        filterTab: String,
        startDateMillis: Long?,
        endDateMillis: Long?
    ): List<CatatanKeuangan> {
        val startSeconds = startDateMillis?.let(::getStartOfDayLocalSeconds)
        val endSeconds = endDateMillis?.let(::getEndOfDayLocalSeconds)
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.note.contains(query, ignoreCase = true)

            val matchesTab = when (filterTab) {
                "Pemasukan" -> item.isPemasukan
                "Pengeluaran" -> item.isPengeluaran
                else -> true
            }

            val itemDateSeconds = runCatching {
                dateFormatter.parse(item.date)?.time?.div(1000)
            }.getOrNull()
            val matchesDate = if (startSeconds == null && endSeconds == null) {
                true
            } else {
                itemDateSeconds != null &&
                    (startSeconds == null || itemDateSeconds >= startSeconds) &&
                    (endSeconds == null || itemDateSeconds <= endSeconds)
            }
            matchesQuery && matchesTab && matchesDate
        }
    }
}
