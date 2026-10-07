package com.ptpws.ikikasir.feature.produk.presentation.state

import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.promo.domain.model.Promo

data class DetailProdukState(
    val produk: Produk? = null,
    val categoryName: String = "Umum",
    val terjualHariIni: Int = 0,
    val total30HariTerakhir: Int = 0,
    val activePromos: List<Promo> = emptyList(),
    val activePromo: Promo? = null,
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false
)
