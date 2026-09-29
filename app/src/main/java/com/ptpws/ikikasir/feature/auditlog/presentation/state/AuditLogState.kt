package com.ptpws.ikikasir.feature.auditlog.presentation.state

import com.ptpws.ikikasir.feature.auditlog.domain.model.AuditLog

data class AuditLogState(
    val auditLogs: List<AuditLog> = emptyList(),
    val filteredLogs: List<AuditLog> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Semua", // Semua, Transaksi, Stok, Harga, Autentikasi, Promo, Sistem
    val selectedDateFilter: String = "Hari Ini", // Hari Ini, 7 Hari Terakhir, Semua Tanggal
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
