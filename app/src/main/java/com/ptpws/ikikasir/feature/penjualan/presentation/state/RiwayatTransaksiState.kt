package com.ptpws.ikikasir.feature.penjualan.presentation.state

import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi

data class RiwayatTransaksiState(
    val searchQuery: String = "",
    val selectedFilter: String = "Hari Ini",
    val transaksiList: List<PenjualanTransaksi> = emptyList(),
    val filteredList: List<PenjualanTransaksi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
