package com.ptpws.ikikasir.feature.kategori.presentation.state

import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori

data class KategoriListState(
    val kategoriList: List<Kategori> = emptyList(),
    val filteredList: List<Kategori> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val kategoriToDelete: Kategori? = null,
    val kategoriToEdit: Kategori? = null
)
