package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.domain.repository.PenjualanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransaksiByIdUseCase @Inject constructor(
    private val repository: PenjualanRepository
) {
    operator fun invoke(transactionId: String): Flow<PenjualanTransaksi?> {
        return repository.getTransaksiById(transactionId)
    }
}
