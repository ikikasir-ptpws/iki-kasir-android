package com.ptpws.ikikasir.feature.auditlog.data.remote.dto

import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.auditlog.domain.model.AuditLog

data class AuditLogDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "TRANSACTION",
    val action: String = "",
    val actorId: String = "",
    val actorName: String = "",
    val actorRole: String = "",
    val isWarning: Boolean = false,
    val createdAt: Timestamp? = null
)

fun AuditLogDto.toAuditLog(): AuditLog {
    return AuditLog(
        id = id,
        title = title,
        description = description,
        category = category,
        action = action,
        actorId = actorId,
        actorName = actorName,
        actorRole = actorRole,
        isWarning = isWarning,
        timestamp = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
        isSynced = true
    )
}

fun AuditLog.toAuditLogDto(): AuditLogDto {
    return AuditLogDto(
        id = id,
        title = title,
        description = description,
        category = category,
        action = action,
        actorId = actorId,
        actorName = actorName,
        actorRole = actorRole,
        isWarning = isWarning,
        createdAt = Timestamp(java.util.Date(timestamp))
    )
}
