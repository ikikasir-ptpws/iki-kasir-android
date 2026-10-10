package com.ptpws.ikikasir.feature.laporanpenjualan.domain.repository

import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.LaporanPenjualanSummary
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import kotlinx.coroutines.flow.Flow

interface LaporanPenjualanRepository {
    fun getLaporanPenjualan(
        periode: PeriodeLaporanPenjualan,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null
    ): Flow<LaporanPenjualanSummary>
}
