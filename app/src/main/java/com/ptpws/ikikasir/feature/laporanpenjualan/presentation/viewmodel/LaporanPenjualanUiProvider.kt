package com.ptpws.ikikasir.feature.laporanpenjualan.presentation.viewmodel

import android.content.Context
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import com.ptpws.ikikasir.feature.laporanpenjualan.presentation.state.LaporanPenjualanState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk LaporanPenjualanUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface LaporanPenjualanUiProvider {
    val state: StateFlow<LaporanPenjualanState>
    fun setPeriode(periode: PeriodeLaporanPenjualan)
    fun setCustomDateRange(startMillis: Long, endMillis: Long)
    fun onSearchQueryChange(query: String)
    fun exportToExcel(context: Context)
    fun clearExportState()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke LaporanPenjualanViewModel asli.
 */
class HiltLaporanPenjualanUiProvider(private val viewModel: LaporanPenjualanViewModel) : LaporanPenjualanUiProvider {
    override val state: StateFlow<LaporanPenjualanState> get() = viewModel.state
    override fun setPeriode(periode: PeriodeLaporanPenjualan) = viewModel.setPeriode(periode)
    override fun setCustomDateRange(startMillis: Long, endMillis: Long) = viewModel.setCustomDateRange(startMillis, endMillis)
    override fun onSearchQueryChange(query: String) = viewModel.onSearchQueryChange(query)
    override fun exportToExcel(context: Context) = viewModel.exportToExcel(context)
    override fun clearExportState() = viewModel.clearExportState()
}
