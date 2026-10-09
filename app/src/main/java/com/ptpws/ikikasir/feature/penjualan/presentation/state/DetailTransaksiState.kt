package com.ptpws.ikikasir.feature.penjualan.presentation.state

import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi

data class DetailTransaksiState(
    val transaksi: PenjualanTransaksi? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefunding: Boolean = false,
    val refundSuccessMessage: String? = null,
    val refundErrorMessage: String? = null
)
