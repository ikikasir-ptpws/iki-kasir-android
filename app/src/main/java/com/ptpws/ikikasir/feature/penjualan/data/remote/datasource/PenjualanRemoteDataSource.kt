package com.ptpws.ikikasir.feature.penjualan.data.remote.datasource

import com.ptpws.ikikasir.feature.penjualan.data.remote.dto.TransactionDto

interface PenjualanRemoteDataSource {
    suspend fun getAllTransactions(): List<TransactionDto>
    suspend fun saveTransaction(dto: TransactionDto)
}
