package com.ptpws.ikikasir.feature.antrean.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.antrean.domain.repository.AntreanRepository
import com.ptpws.ikikasir.feature.antrean.domain.usecase.GetAntreanUseCase
import com.ptpws.ikikasir.feature.antrean.domain.usecase.InsertQueueHistoryUseCase
import com.ptpws.ikikasir.feature.antrean.presentation.state.AntreanUiState
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetAllTransaksiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase

private const val TAG = "AntreanViewModel"

@HiltViewModel
class AntreanViewModel @Inject constructor(
    private val getAntreanUseCase: GetAntreanUseCase,
    private val antreanRepository: AntreanRepository,
    private val insertQueueHistoryUseCase: InsertQueueHistoryUseCase,
    private val getAllTransaksiUseCase: GetAllTransaksiUseCase,
    private val logActivityUseCase: LogActivityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AntreanUiState>(AntreanUiState.Loading)
    val uiState: StateFlow<AntreanUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _transaksiMap = MutableStateFlow<Map<String, PenjualanTransaksi>>(emptyMap())
    val transaksiMap: StateFlow<Map<String, PenjualanTransaksi>> = _transaksiMap.asStateFlow()

    private val _isBulkProcessing = MutableStateFlow(false)
    val isBulkProcessing: StateFlow<Boolean> = _isBulkProcessing.asStateFlow()

    init {
        loadAntrean()
        loadTransaksiMap()
    }

    private fun loadAntrean() {
        viewModelScope.launch {
            getAntreanUseCase()
                .catch { e ->
                    Log.e(TAG, "Error loading antrean: ${e.message}", e)
                    _uiState.value = AntreanUiState.Error(e.message ?: "Terjadi kesalahan")
                }
                .collectLatest { list ->
                    // Order by sequence ASC, filter out any antrean with status REFUND / REFUNDED / BATAL
                    val validList = list.filterNot { antrean ->
                        antrean.status.equals("REFUND", ignoreCase = true) ||
                        antrean.status.equals("REFUNDED", ignoreCase = true) ||
                        antrean.status.equals("BATAL", ignoreCase = true)
                    }
                    val sortedList = validList.sortedBy { it.queueSequence }
                    _uiState.value = AntreanUiState.Success(sortedList)
                }
        }
    }

    private fun loadTransaksiMap() {
        viewModelScope.launch {
            getAllTransaksiUseCase()
                .catch { e -> Log.e(TAG, "Error loading transactions: ${e.message}") }
                .collectLatest { list ->
                    val map = mutableMapOf<String, PenjualanTransaksi>()
                    list.forEach { tx ->
                        if (tx.transactionId.isNotBlank()) {
                            map[tx.transactionId] = tx
                            map[tx.transactionId.removePrefix("#")] = tx
                        }
                        if (tx.transactionNumber.isNotBlank()) {
                            map[tx.transactionNumber] = tx
                            map[tx.transactionNumber.removePrefix("#")] = tx
                        }
                    }
                    _uiState.value = _uiState.value // trigger update if needed
                    _transaksiMap.value = map
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selesaikan(id: String) {
        processAntreanRemoval(listOf(id), status = "DONE")
    }

    fun batalkan(id: String) {
        processAntreanRemoval(listOf(id), status = "CANCELLED")
    }

    fun selesaikanBanyak(ids: List<String>) {
        processAntreanRemoval(ids, status = "DONE", isBulk = true)
    }

    fun batalkanBanyak(ids: List<String>) {
        processAntreanRemoval(ids, status = "CANCELLED", isBulk = true)
    }

    private fun processAntreanRemoval(ids: List<String>, status: String, isBulk: Boolean = false) {
        viewModelScope.launch {
            if (isBulk) _isBulkProcessing.value = true
            try {
                val currentList = (uiState.value as? AntreanUiState.Success)?.data ?: emptyList()
                if (ids.isEmpty()) {
                    GlobalCrudResultDialog.failure("Pilih antrean yang akan diproses.")
                    return@launch
                }

                var succeeded = 0
                var failed = 0
                val succeededItems = mutableListOf<Antrean>()
                ids.distinct().forEach { id ->
                    val targetItem = currentList.find { it.id == id || it.transactionId == id }
                    if (targetItem == null) {
                        failed++
                        Log.w(TAG, "Antrean $id not found while processing $status")
                    } else {
                        try {
                            processAntreanItem(targetItem, status)
                            succeeded++
                            succeededItems += targetItem
                        } catch (e: Exception) {
                            failed++
                            Log.e(TAG, "Failed processing antrean ${targetItem.id} ($status): ${e.message}", e)
                        }
                    }
                }

                val actionName = if (status == "DONE") "diselesaikan" else "dibatalkan"
                if (failed == 0) {
                    val message = if (isBulk) {
                        "$succeeded antrean berhasil $actionName."
                    } else {
                        "Antrean #${succeededItems.first().queueSequence} berhasil $actionName."
                    }
                    GlobalCrudResultDialog.success(message)
                } else if (succeeded == 0) {
                    GlobalCrudResultDialog.failure(
                        if (isBulk) "Gagal memproses $failed antrean." else "Antrean tidak ditemukan atau gagal diproses."
                    )
                } else {
                    GlobalCrudResultDialog.failure(
                        "$succeeded antrean berhasil $actionName, $failed antrean gagal diproses."
                    )
                }
            } finally {
                if (isBulk) _isBulkProcessing.value = false
            }
        }
    }

    private suspend fun processAntreanItem(targetItem: Antrean, status: String) {
        val targetTxId = targetItem.transactionId.ifBlank { targetItem.id }
        val timestamp = Timestamp.now()
        val history = QueueHistory(
            id = targetTxId,
            transactionId = targetTxId,
            status = status,
            customerName = targetItem.customerName,
            tableNumber = targetItem.tableNumber,
            queueSequence = targetItem.queueSequence,
            completedAt = timestamp,
            createdAt = targetItem.createdAt,
            updatedAt = timestamp
        )
        insertQueueHistoryUseCase(history).collect { result -> result.getOrThrow() }
        Log.d(TAG, "QueueHistory inserted for $targetTxId (sequence #${targetItem.queueSequence}) with status $status")

        antreanRepository.deleteAntrean(targetItem.id).collect { result -> result.getOrThrow() }
        if (targetTxId != targetItem.id) {
            antreanRepository.deleteAntrean(targetTxId).collect { result -> result.getOrThrow() }
        }
        Log.d(TAG, "Active antrean ${targetItem.id} deleted from queues")

        val isDone = status == "DONE"
        logActivityUseCase(
            title = if (isDone) "Antrean Selesai: #${targetItem.queueSequence}" else "Antrean Dibatalkan: #${targetItem.queueSequence}",
            description = if (isDone) {
                "Pesanan atas nama ${targetItem.customerName} (Meja ${targetItem.tableNumber}) telah diselesaikan."
            } else {
                "Pesanan atas nama ${targetItem.customerName} (Meja ${targetItem.tableNumber}) telah dibatalkan."
            },
            category = "TRANSACTION",
            action = if (isDone) "UPDATE" else "CANCEL",
            isWarning = !isDone
        )
    }
}
