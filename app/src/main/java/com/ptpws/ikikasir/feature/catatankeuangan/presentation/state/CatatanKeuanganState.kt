package com.ptpws.ikikasir.feature.catatankeuangan.presentation.state

import android.net.Uri
import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan

data class CatatanKeuanganState(
    val catatanList: List<CatatanKeuangan> = emptyList(),
    val filteredList: List<CatatanKeuangan> = emptyList(),
    val totalPemasukan: Double = 0.0,
    val totalPengeluaran: Double = 0.0,
    val saldoBersih: Double = 0.0,
    val searchQuery: String = "",
    val selectedFilterTab: String = "Semua", // "Semua", "Pemasukan", "Pengeluaran", "Filter Tanggal"
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val customDateLabel: String? = null,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val exportedUri: Uri? = null,
    val exportSuccessMessage: String? = null,
    val exportErrorMessage: String? = null,
    val userMessage: String? = null,
    val errorMessage: String? = null
)
