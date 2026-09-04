package com.ptpws.ikikasir.feature.kategori.presentation.state

data class KategoriFormState(
    val id: String = "",
    val nama: String = "",
    val deskripsi: String = "",
    val iconName: String = "LocalCafe",
    val colorHex: String = "#4F46E5",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false
)
