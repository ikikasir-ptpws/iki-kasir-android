package com.ptpws.ikikasir.feature.produk.domain.usecase

import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.repository.ProdukRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProdukUseCase @Inject constructor(
    private val repository: ProdukRepository
) {
    operator fun invoke(): Flow<List<Produk>> {
        return repository.getProdukList()
    }

    fun getByCategoryId(categoryId: String): Flow<List<Produk>> {
        return repository.getProdukByCategoryId(categoryId)
    }

    fun getById(id: String): Flow<Produk?> {
        return repository.getProdukById(id)
    }
}