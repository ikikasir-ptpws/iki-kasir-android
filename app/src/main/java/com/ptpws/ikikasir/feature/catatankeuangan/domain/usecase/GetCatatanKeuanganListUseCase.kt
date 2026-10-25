package com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase

import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.catatankeuangan.domain.repository.CatatanKeuanganRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCatatanKeuanganListUseCase @Inject constructor(
    private val repository: CatatanKeuanganRepository
) {
    operator fun invoke(): Flow<List<CatatanKeuangan>> {
        return repository.getCatatanList()
    }
}
