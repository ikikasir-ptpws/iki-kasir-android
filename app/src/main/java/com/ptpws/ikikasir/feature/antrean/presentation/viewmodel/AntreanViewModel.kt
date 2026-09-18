package com.ptpws.ikikasir.feature.antrean.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.antrean.domain.model.AntreanStatus
import com.ptpws.ikikasir.feature.antrean.domain.usecase.GetAntreanUseCase
import com.ptpws.ikikasir.feature.antrean.domain.usecase.UpdateAntreanStatusUseCase
import com.ptpws.ikikasir.feature.antrean.presentation.state.AntreanUiState
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
    private val updateAntreanStatusUseCase: UpdateAntreanStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AntreanUiState>(AntreanUiState.Loading)
    val uiState: StateFlow<AntreanUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadAntrean()
    }

    private fun loadAntrean() {
        viewModelScope.launch {
            getAntreanUseCase()
                .catch { e ->
                    Log.e(TAG, "Error loading antrean: ${e.message}", e)
                    _uiState.value = AntreanUiState.Error(e.message ?: "Terjadi kesalahan")
                }
                .collectLatest { list ->
                    _uiState.value = AntreanUiState.Success(list)
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selesaikan(id: String) {
        viewModelScope.launch {
            try {
                updateAntreanStatusUseCase(id, AntreanStatus.DONE).collect {}
                Log.d(TAG, "Antrean $id marked as DONE")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark antrean as DONE: ${e.message}", e)
            }
        }
    }

    fun batalkan(id: String) {
        viewModelScope.launch {
            try {
                updateAntreanStatusUseCase(id, AntreanStatus.CANCELLED).collect {}
                Log.d(TAG, "Antrean $id marked as CANCELLED")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark antrean as CANCELLED: ${e.message}", e)
            }
        }
    }
}
