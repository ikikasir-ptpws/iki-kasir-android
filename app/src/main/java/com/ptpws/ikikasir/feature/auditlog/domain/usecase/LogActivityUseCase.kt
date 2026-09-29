package com.ptpws.ikikasir.feature.auditlog.domain.usecase

import com.ptpws.ikikasir.feature.auditlog.domain.repository.AuditLogRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogActivityUseCase @Inject constructor(
    private val repository: AuditLogRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        category: String,
        action: String,
        isWarning: Boolean = false,
        actorId: String? = null,
        actorName: String? = null,
        actorRole: String? = null
    ) {
        repository.logActivity(
            title = title,
            description = description,
            category = category,
            action = action,
            isWarning = isWarning,
            actorId = actorId,
            actorName = actorName,
            actorRole = actorRole
        )
    }
}
