package com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase

import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.catatankeuangan.domain.repository.CatatanKeuanganRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateCatatanKeuanganUseCase @Inject constructor(
    private val repository: CatatanKeuanganRepository
) {
    suspend operator fun invoke(catatan: CatatanKeuangan): Flow<Result<Unit>> {
        return repository.updateCatatan(catatan)
    }
}
