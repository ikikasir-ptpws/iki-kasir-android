package com.ptpws.ikikasir.feature.antrean.presentation.state

import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory

sealed interface QueueHistoryUiState {
    object Loading : QueueHistoryUiState
    data class Success(val data: List<QueueHistory>) : QueueHistoryUiState
    data class Error(val message: String) : QueueHistoryUiState
}
