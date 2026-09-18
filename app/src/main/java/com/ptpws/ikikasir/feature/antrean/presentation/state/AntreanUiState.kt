package com.ptpws.ikikasir.feature.antrean.presentation.state

import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean

sealed class AntreanUiState {
    object Loading : AntreanUiState()
    data class Success(val data: List<Antrean>) : AntreanUiState()
    data class Error(val message: String) : AntreanUiState()
}
