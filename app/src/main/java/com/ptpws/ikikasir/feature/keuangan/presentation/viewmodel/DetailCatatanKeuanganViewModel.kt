package com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.keuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.keuangan.domain.repository.CatatanKeuanganRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailCatatanUiState(
    val catatan: CatatanKeuangan? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DetailCatatanKeuanganViewModel @Inject constructor(
    private val repository: CatatanKeuanganRepository,
    private val logActivityUseCase: LogActivityUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val catatanId: String = savedStateHandle.get<String>("id").orEmpty()

    private val _state = MutableStateFlow(DetailCatatanUiState())
    val state: StateFlow<DetailCatatanUiState> = _state.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        if (catatanId.isBlank()) {
            _state.update { it.copy(isLoading = false, errorMessage = "ID catatan tidak valid") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getCatatanByIdFlow(catatanId).collect { item ->
                if (item != null) {
                    _state.update { it.copy(catatan = item, isLoading = false, errorMessage = null) }
                } else if (_state.value.catatan == null) {
                    _state.update { it.copy(isLoading = false, errorMessage = "Catatan tidak ditemukan") }
                }
            }
        }
    }

    fun deleteCatatan(onSuccess: () -> Unit) {
        val item = _state.value.catatan ?: return
        viewModelScope.launch {
            repository.deleteCatatanById(item.id)
            logActivityUseCase(
                title = "Hapus Catatan Keuangan",
                description = "Menghapus catatan ${item.jenis}: ${item.judul}",
                category = "KEUANGAN",
                action = "DELETE",
                isWarning = true
            )
            _state.update { it.copy(isDeleted = true) }
            onSuccess()
        }
    }
}
