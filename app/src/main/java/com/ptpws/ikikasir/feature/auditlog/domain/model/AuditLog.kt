package com.ptpws.ikikasir.feature.auditlog.domain.model

data class AuditLog(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "TRANSACTION", // TRANSACTION, STOCK, PRICE, AUTHENTICATION, PROMO, SYSTEM
    val action: String = "",              // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, CANCEL
    val actorId: String = "",
    val actorName: String = "",
    val actorRole: String = "",
    val isWarning: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
