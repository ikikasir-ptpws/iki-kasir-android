package com.ptpws.ikikasir.feature.antrean.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
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

private const val TAG = "AntreanViewModel"

@HiltViewModel
class AntreanViewModel @Inject constructor(
    private val getAntreanUseCase: GetAntreanUseCase,
    private val antreanRepository: AntreanRepository,
    private val insertQueueHistoryUseCase: InsertQueueHistoryUseCase,
    private val getAllTransaksiUseCase: GetAllTransaksiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AntreanUiState>(AntreanUiState.Loading)
    val uiState: StateFlow<AntreanUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _transaksiMap = MutableStateFlow<Map<String, PenjualanTransaksi>>(emptyMap())
    val transaksiMap: StateFlow<Map<String, PenjualanTransaksi>> = _transaksiMap.asStateFlow()

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
                    // Order by sequence ASC
                    val sortedList = list.sortedBy { it.queueSequence }
                    _uiState.value = AntreanUiState.Success(sortedList)
                }
        }
    }

    private fun loadTransaksiMap() {
        viewModelScope.launch {
            getAllTransaksiUseCase()
                .catch { e -> Log.e(TAG, "Error loading transactions: ${e.message}") }
                .collectLatest { list ->
                    val map = list.associateBy { it.transactionId }
                    _uiState.value = _uiState.value // trigger update if needed
                    _transaksiMap.value = map
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selesaikan(id: String) {
        processAntreanRemoval(id, status = "DONE")
    }

    fun batalkan(id: String) {
        processAntreanRemoval(id, status = "CANCELLED")
    }

    private fun processAntreanRemoval(id: String, status: String) {
        viewModelScope.launch {
            try {
                val currentList = (uiState.value as? AntreanUiState.Success)?.data ?: emptyList()
                val targetItem = currentList.find { it.id == id }

                if (targetItem != null) {
                    // 1. Save to QueueHistory with original queue sequence number
                    val history = QueueHistory(
                        id = targetItem.id,
                        transactionId = targetItem.transactionId,
                        status = status,
                        customerName = targetItem.customerName,
                        queueSequence = targetItem.queueSequence,
                        completedAt = Timestamp.now(),
                        createdAt = targetItem.createdAt,
                        updatedAt = Timestamp.now()
                    )
                    insertQueueHistoryUseCase(history).collect {}
                    Log.d(TAG, "QueueHistory inserted for $id (sequence #${targetItem.queueSequence}) with status $status")

                    // 2. Delete item from active queues (Room DB + Firestore)
                    antreanRepository.deleteAntrean(id).collect {}
                    Log.d(TAG, "Active antrean $id deleted from queues")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed processing antrean removal ($status): ${e.message}", e)
            }
        }
    }
}
