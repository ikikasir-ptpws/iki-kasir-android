package com.ptpws.ikikasir.feature.auditlog.data.remote.datasource

import com.ptpws.ikikasir.feature.auditlog.data.remote.dto.AuditLogDto

interface AuditLogRemoteDataSource {
    suspend fun saveAuditLog(dto: AuditLogDto): Boolean
    suspend fun getAllAuditLogs(): List<AuditLogDto>
}
