package com.ptpws.ikikasir.feature.auditlog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ptpws.ikikasir.feature.auditlog.domain.model.AuditLog

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val action: String,
    val actorId: String,
    val actorName: String,
    val actorRole: String,
    val isWarning: Boolean,
    val timestamp: Long,
    val isSynced: Boolean = false
)

fun AuditLogEntity.toAuditLog(): AuditLog {
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
        timestamp = timestamp,
        isSynced = isSynced
    )
}

fun AuditLog.toAuditLogEntity(): AuditLogEntity {
    return AuditLogEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        action = action,
        actorId = actorId,
        actorName = actorName,
        actorRole = actorRole,
        isWarning = isWarning,
        timestamp = timestamp,
        isSynced = isSynced
    )
}
