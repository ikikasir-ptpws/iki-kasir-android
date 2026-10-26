package com.ptpws.ikikasir.feature.catatankeuangan.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase.DeleteCatatanKeuanganUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase.GetCatatanByIdUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.presentation.state.DetailCatatanState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailCatatanKeuanganViewModel @Inject constructor(
    private val getCatatanByIdUseCase: GetCatatanByIdUseCase,
    private val deleteCatatanKeuanganUseCase: DeleteCatatanKeuanganUseCase,
    private val logActivityUseCase: LogActivityUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(DetailCatatanState())
    val state: StateFlow<DetailCatatanState> = _state.asStateFlow()

    private val catatanId: String? = savedStateHandle.get<String>("catatanId")

    init {
        loadDetail()
    }

    fun loadDetail() {
        val id = catatanId
        if (id.isNullOrBlank()) {
            _state.update { it.copy(isLoading = false, errorMessage = "ID catatan tidak ditemukan.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getCatatanByIdUseCase(id).collect { item ->
                _state.update {
                    it.copy(
                        catatan = item,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteCatatan(onSuccess: () -> Unit) {
        val item = _state.value.catatan ?: return
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            deleteCatatanKeuanganUseCase(item.id).collect { result ->
                result.onSuccess {
                    GlobalCrudResultDialog.success("Catatan \"${item.title}\" berhasil dihapus.")
                    logActivityUseCase(
                        title = "Hapus Catatan Keuangan",
                        description = "Catatan ${item.title} (Rp ${item.amount}) berhasil dihapus.",
                        category = "KEUANGAN",
                        action = "DELETE",
                        isWarning = true
                    )
                    _state.update { it.copy(isDeleting = false, isDeleteSuccess = true) }
                    onSuccess()
                }.onFailure { err ->
                    val msg = err.message ?: "Gagal menghapus catatan keuangan."
                    GlobalCrudResultDialog.failure(msg)
                    _state.update { it.copy(isDeleting = false, errorMessage = msg) }
                }
            }
        }
    }
}
