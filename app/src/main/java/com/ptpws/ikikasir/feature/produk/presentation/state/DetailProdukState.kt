package com.ptpws.ikikasir.feature.produk.presentation.state

import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class DetailProdukState(
    val produk: Produk? = null,
    val categoryName: String = "Umum",
    val terjualHariIni: Int = 0,
    val total30HariTerakhir: Int = 0,
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false
)
