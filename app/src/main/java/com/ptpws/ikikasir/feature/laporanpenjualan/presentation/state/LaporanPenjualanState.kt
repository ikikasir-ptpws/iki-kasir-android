package com.ptpws.ikikasir.feature.laporanpenjualan.presentation.state

import android.net.Uri
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.LaporanPenjualanSummary
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.ProdukTerjualItem
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.SortByLaporanPenjualan

data class LaporanPenjualanState(
    val summary: LaporanPenjualanSummary = LaporanPenjualanSummary(),
    val displayedItems: List<ProdukTerjualItem> = emptyList(),
    val selectedPeriode: PeriodeLaporanPenjualan = PeriodeLaporanPenjualan.HARI_INI,
    val selectedSortBy: SortByLaporanPenjualan = SortByLaporanPenjualan.TERBANYAK_QTY,
    val searchQuery: String = "",
    val customStartDateMillis: Long? = null,
    val customEndDateMillis: Long? = null,
    val customDateLabel: String? = null,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val exportedFileUri: Uri? = null,
    val exportError: String? = null,
    val exportMessage: String? = null
)
