package com.ptpws.ikikasir.feature.kategori.domain.usecase

import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.domain.repository.KategoriRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateKategoriUseCase @Inject constructor(
    private val repository: KategoriRepository
) {
    suspend operator fun invoke(kategori: Kategori): Flow<Result<Unit>> {
        val updatedKategori = kategori.copy(updatedAt = System.currentTimeMillis())
        return repository.updateKategori(updatedKategori)
    }
}
