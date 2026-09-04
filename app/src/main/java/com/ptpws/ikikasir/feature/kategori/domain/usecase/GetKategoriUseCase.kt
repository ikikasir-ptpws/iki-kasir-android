package com.ptpws.ikikasir.feature.kategori.domain.usecase

import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.domain.repository.KategoriRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetKategoriUseCase @Inject constructor(
    private val repository: KategoriRepository
) {
    operator fun invoke(): Flow<List<Kategori>> {
        return repository.getKategoriList()
    }
}
