package com.ptpws.ikikasir.feature.produk.domain.usecase

import com.ptpws.ikikasir.feature.produk.domain.repository.ProdukRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteProdukUseCase @Inject constructor(
    private val repository: ProdukRepository
) {
    suspend operator fun invoke(id: String): Flow<Result<Unit>> {
        return repository.deleteProduk(id)
    }
}