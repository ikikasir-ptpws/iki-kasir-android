package com.ptpws.ikikasir.feature.kategori.domain.usecase

import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.domain.repository.KategoriRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class InsertKategoriUseCase @Inject constructor(
    private val repository: KategoriRepository
) {
    suspend operator fun invoke(kategori: Kategori): Flow<Result<Unit>> {
        val kategoriToInsert = if (kategori.id.isBlank()) {
            kategori.copy(id = UUID.randomUUID().toString())
        } else {
            kategori
        }
        return repository.insertKategori(kategoriToInsert)
    }
}
