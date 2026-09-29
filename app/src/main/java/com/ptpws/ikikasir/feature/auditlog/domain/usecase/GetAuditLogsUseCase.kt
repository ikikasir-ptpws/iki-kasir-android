package com.ptpws.ikikasir.feature.auditlog.domain.usecase

import com.ptpws.ikikasir.feature.auditlog.domain.model.AuditLog
import com.ptpws.ikikasir.feature.auditlog.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAuditLogsUseCase @Inject constructor(
    private val repository: AuditLogRepository
) {
    operator fun invoke(): Flow<List<AuditLog>> {
        return repository.getAuditLogs()
    }
}
