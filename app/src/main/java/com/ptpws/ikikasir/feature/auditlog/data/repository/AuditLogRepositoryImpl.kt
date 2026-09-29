package com.ptpws.ikikasir.feature.auditlog.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.auditlog.data.local.dao.AuditLogDao
import com.ptpws.ikikasir.feature.auditlog.data.local.entity.toAuditLog
import com.ptpws.ikikasir.feature.auditlog.data.local.entity.toAuditLogEntity
import com.ptpws.ikikasir.feature.auditlog.data.remote.datasource.AuditLogRemoteDataSource
import com.ptpws.ikikasir.feature.auditlog.data.remote.dto.AuditLogDto
import com.ptpws.ikikasir.feature.auditlog.data.remote.dto.toAuditLog
import com.ptpws.ikikasir.feature.auditlog.data.remote.dto.toAuditLogDto
import com.ptpws.ikikasir.feature.auditlog.domain.model.AuditLog
import com.ptpws.ikikasir.feature.auditlog.domain.repository.AuditLogRepository
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.role.data.local.dao.RoleDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuditLogRepository"

@Singleton
class AuditLogRepositoryImpl @Inject constructor(
    private val auditLogDao: AuditLogDao,
    private val remoteDataSource: AuditLogRemoteDataSource,
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val networkMonitor: NetworkMonitor
) : AuditLogRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    syncUnsyncedLogs()
                    refreshFromRemote()
                }
            }
        }
    }

    override fun getAuditLogs(): Flow<List<AuditLog>> {
        return auditLogDao.getAllAuditLogs().map { entities ->
            entities.map { it.toAuditLog() }
        }
    }

    override suspend fun logActivity(
        title: String,
        description: String,
        category: String,
        action: String,
        isWarning: Boolean,
        actorId: String?,
        actorName: String?,
        actorRole: String?
    ) {
        // Resolve actor details if not supplied
        var finalActorId = actorId ?: ""
        var finalActorName = actorName ?: ""
        var finalActorRole = actorRole ?: ""

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val email = currentUser.email.orEmpty()
            val localUser = if (email.isNotBlank()) userDao.getUserByEmail(email) else null
            finalActorId = finalActorId.ifBlank { currentUser.uid }
            finalActorName = finalActorName.ifBlank {
                localUser?.fullName
                    ?: currentUser.displayName
                    ?: email.ifBlank { "System User" }
            }
            if (finalActorRole.isBlank() && localUser != null) {
                finalActorRole = resolveRoleName(localUser.roleId)
            }
        } else {
            finalActorName = finalActorName.ifBlank { "System" }
            finalActorRole = finalActorRole.ifBlank { "System" }
        }

        val newLog = AuditLog(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            category = category,
            action = action,
            actorId = finalActorId,
            actorName = finalActorName,
            actorRole = finalActorRole,
            isWarning = isWarning,
            timestamp = System.currentTimeMillis(),
            isSynced = false
        )

        // 1. Save to Room DB (offline first)
        auditLogDao.insertAuditLog(newLog.toAuditLogEntity())

        // Keep remote writes off the calling flow so offline logging never blocks the user action.
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                syncUnsyncedLogs()
            }
        }
    }

    private suspend fun resolveRoleName(roleId: String): String {
        if (roleId.isBlank()) return ""
        return roleDao.getRoleById(roleId)?.name
            ?.takeIf(String::isNotBlank)
            ?: roleDao.getRoleByName(roleId)?.name
                ?.takeIf(String::isNotBlank)
            ?: roleId
    }

    override suspend fun syncUnsyncedLogs() {
        if (!networkMonitor.isConnected()) return

        try {
            val unsynced = auditLogDao.getUnsyncedAuditLogs()
            for (logEntity in unsynced) {
                if (!networkMonitor.isConnected()) return
                val log = logEntity.toAuditLog()
                val success = remoteDataSource.saveAuditLog(log.toAuditLogDto())
                if (success) {
                    auditLogDao.markAsSynced(log.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing unsynced audit logs: ${e.message}")
        }
    }

    override suspend fun refreshFromRemote() {
        if (!networkMonitor.isConnected()) return

        try {
            val remoteDtos = remoteDataSource.getAllAuditLogs()
            if (remoteDtos.isNotEmpty()) {
                val entities = remoteDtos.map { dto ->
                    val correctedRole = resolveRemoteActorRole(dto)
                    if (correctedRole == dto.actorRole) {
                        dto.toAuditLog().toAuditLogEntity()
                    } else {
                        val correctedDto = dto.copy(actorRole = correctedRole)
                        val synced = remoteDataSource.saveAuditLog(correctedDto)
                        auditLogDao.updateSyncedActorRole(dto.id, correctedRole, synced)
                        correctedDto.toAuditLog().toAuditLogEntity().copy(isSynced = synced)
                    }
                }
                auditLogDao.insertAuditLogs(entities)
                if (entities.any { !it.isSynced }) {
                    syncUnsyncedLogs()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing audit logs from remote: ${e.message}")
        }
    }

    private suspend fun resolveRemoteActorRole(dto: AuditLogDto): String {
        val actorUser = if (dto.actorName.contains('@')) {
            userDao.getUserByEmail(dto.actorName)
        } else {
            userDao.getUserById(dto.actorId)
        }

        return if (actorUser != null) {
            resolveRoleName(actorUser.roleId)
        } else if (dto.actorName.contains('@')) {
            ""
        } else {
            dto.actorRole
        }
    }
}
