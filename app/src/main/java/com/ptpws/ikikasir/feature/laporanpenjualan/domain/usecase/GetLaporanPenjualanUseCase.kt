package com.ptpws.ikikasir.feature.laporanpenjualan.domain.usecase

import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.LaporanPenjualanSummary
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.repository.LaporanPenjualanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLaporanPenjualanUseCase @Inject constructor(
    private val repository: LaporanPenjualanRepository
) {
    operator fun invoke(
        periode: PeriodeLaporanPenjualan,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null
    ): Flow<LaporanPenjualanSummary> {
        return repository.getLaporanPenjualan(periode, customStartMillis, customEndMillis)
    }
}
