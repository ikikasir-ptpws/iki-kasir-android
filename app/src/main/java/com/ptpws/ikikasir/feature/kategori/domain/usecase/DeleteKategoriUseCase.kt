package com.ptpws.ikikasir.feature.kategori.domain.usecase

import com.ptpws.ikikasir.feature.kategori.domain.repository.KategoriRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteKategoriUseCase @Inject constructor(
    private val repository: KategoriRepository
) {
    suspend operator fun invoke(id: String): Flow<Result<Unit>> {
        return repository.deleteKategori(id)
    }
}
