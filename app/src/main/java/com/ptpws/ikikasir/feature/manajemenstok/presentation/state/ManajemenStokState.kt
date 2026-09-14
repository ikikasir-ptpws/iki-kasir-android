package com.ptpws.ikikasir.feature.manajemenstok.presentation.state

import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class ManajemenStokState(
    val produkList: List<Produk> = emptyList(),
    val kategoriList: List<Kategori> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val isFilterKritisOnly: Boolean = false,
    val totalUnitKeseluruhan: Int = 0,
    val jumlahRestockDibutuhkan: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedRestockProduk: Produk? = null,
    val restockTambahJumlahText: String = "",
    val isSuccessRestock: Boolean = false
)
