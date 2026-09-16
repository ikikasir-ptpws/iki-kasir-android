package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTransaksiUseCase @Inject constructor(
    private val repository: PenjualanRepository
) {
    operator fun invoke(): Flow<List<PenjualanTransaksi>> {
        return repository.getAllTransaksi()
    }
}
