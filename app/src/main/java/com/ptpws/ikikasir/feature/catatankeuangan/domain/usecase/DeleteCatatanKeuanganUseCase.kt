package com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase

import com.ptpws.ikikasir.feature.catatankeuangan.domain.repository.CatatanKeuanganRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteCatatanKeuanganUseCase @Inject constructor(
    private val repository: CatatanKeuanganRepository
) {
    suspend operator fun invoke(id: String): Flow<Result<Unit>> {
        return repository.deleteCatatan(id)
    }
}
