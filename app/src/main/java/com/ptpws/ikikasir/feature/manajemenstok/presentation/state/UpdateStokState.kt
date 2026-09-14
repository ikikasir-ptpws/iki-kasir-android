package com.ptpws.ikikasir.feature.manajemenstok.presentation.state

import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class UpdateStokState(
    val produkList: List<Produk> = emptyList(),
    val selectedProduk: Produk? = null,
    val newStock: Int = 0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val showProductDropdown: Boolean = false
)
