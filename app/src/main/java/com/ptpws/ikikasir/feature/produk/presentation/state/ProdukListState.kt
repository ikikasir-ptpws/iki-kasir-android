package com.ptpws.ikikasir.feature.produk.presentation.state

import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class ProdukListState(
    val produkList: List<Produk> = emptyList(),
    val filteredList: List<Produk> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val produkToDelete: Produk? = null,
    val produkToEdit: Produk? = null
)