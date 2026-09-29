package com.ptpws.ikikasir.feature.auditlog.domain.repository

import com.ptpws.ikikasir.feature.auditlog.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun getAuditLogs(): Flow<List<AuditLog>>
    suspend fun logActivity(
        title: String,
        description: String,
        category: String,
        action: String,
        isWarning: Boolean = false,
        actorId: String? = null,
        actorName: String? = null,
        actorRole: String? = null
    )
    suspend fun syncUnsyncedLogs()
    suspend fun refreshFromRemote()
}
