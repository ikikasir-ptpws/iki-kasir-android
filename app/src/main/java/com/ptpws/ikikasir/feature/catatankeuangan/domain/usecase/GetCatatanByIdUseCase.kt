package com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase

import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.catatankeuangan.domain.repository.CatatanKeuanganRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCatatanByIdUseCase @Inject constructor(
    private val repository: CatatanKeuanganRepository
) {
    operator fun invoke(id: String): Flow<CatatanKeuangan?> {
        return repository.getCatatanById(id)
    }
}
