package com.ptpws.ikikasir.feature.penjualan.presentation.state

import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi

data class GroupedTransaksi(
    val dateHeader: String,
    val countText: String,
    val transactions: List<PenjualanTransaksi>
)

data class RiwayatTransaksiState(
    val searchQuery: String = "",
    val selectedFilter: String = "Hari Ini",
    val selectedCustomDateMillis: Long? = null,
    val customDateLabel: String? = null,
    val transaksiList: List<PenjualanTransaksi> = emptyList(),
    val filteredList: List<PenjualanTransaksi> = emptyList(),
    val groupedTransactions: List<GroupedTransaksi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
