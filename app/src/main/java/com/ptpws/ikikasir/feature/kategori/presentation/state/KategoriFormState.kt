package com.ptpws.ikikasir.feature.kategori.presentation.state

data class KategoriFormState(
    val id: String = "",
    val nama: String = "",
    val deskripsi: String = "",
    val iconName: String = "BakeryDining",
    val colorHex: String = "#4F46E5",
    val isVisibleInCashier: Boolean = true,
    val productCount: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false
)