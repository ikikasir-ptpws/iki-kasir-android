package com.ptpws.ikikasir.feature.catatankeuangan.presentation.state

import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan

data class DetailCatatanState(
    val catatan: CatatanKeuangan? = null,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val isDeleteSuccess: Boolean = false,
    val errorMessage: String? = null
)
