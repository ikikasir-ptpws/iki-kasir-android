package com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel

import android.content.Context
import com.ptpws.ikikasir.feature.keuangan.presentation.state.LaporanKeuanganState
import com.ptpws.ikikasir.feature.keuangan.presentation.state.PeriodeFilter
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk LaporanKeuanganUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface LaporanKeuanganUiProvider {
    val state: StateFlow<LaporanKeuanganState>
    fun setPeriode(periode: PeriodeFilter)
    fun setCustomDateRange(startMillis: Long, endMillis: Long)
    fun exportToExcel(context: Context)
    fun clearExportState()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke LaporanKeuanganViewModel asli.
 */
class HiltLaporanKeuanganUiProvider(private val viewModel: LaporanKeuanganViewModel) : LaporanKeuanganUiProvider {
    override val state: StateFlow<LaporanKeuanganState> get() = viewModel.state
    override fun setPeriode(periode: PeriodeFilter) = viewModel.setPeriode(periode)
    override fun setCustomDateRange(startMillis: Long, endMillis: Long) = viewModel.setCustomDateRange(startMillis, endMillis)
    override fun exportToExcel(context: Context) = viewModel.exportToExcel(context)
    override fun clearExportState() = viewModel.clearExportState()
}
