package com.ptpws.ikikasir.feature.antrean.domain.usecase

import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import com.ptpws.ikikasir.feature.antrean.domain.repository.AntreanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAntreanUseCase @Inject constructor(
    private val repository: AntreanRepository
) {
    operator fun invoke(): Flow<List<Antrean>> {
        return repository.getAntreanList()
    }
}
