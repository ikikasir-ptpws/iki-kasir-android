package com.ptpws.ikikasir.feature.produk.domain.usecase

import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.repository.ProdukRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InsertProdukUseCase @Inject constructor(
    private val repository: ProdukRepository
) {
    suspend operator fun invoke(produk: Produk): Flow<Result<Unit>> {
        return repository.insertProduk(produk)
    }
}