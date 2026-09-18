package com.ptpws.ikikasir.feature.antrean.domain.usecase

import com.ptpws.ikikasir.feature.antrean.domain.repository.AntreanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateAntreanStatusUseCase @Inject constructor(
    private val repository: AntreanRepository
) {
    suspend operator fun invoke(id: String, status: String): Flow<Result<Unit>> {
        return repository.updateAntreanStatus(id, status)
    }
}
