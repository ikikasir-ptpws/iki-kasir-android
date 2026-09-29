package com.ptpws.ikikasir.feature.auditlog.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.feature.auditlog.data.local.dao.AuditLogDao
import com.ptpws.ikikasir.feature.auditlog.data.local.entity.toAuditLog
import com.ptpws.ikikasir.feature.auditlog.data.local.entity.toAuditLogEntity
import com.ptpws.ikikasir.feature.auditlog.data.remote.datasource.AuditLogRemoteDataSource
import com.ptpws.ikikasir.feature.auditlog.data.remote.dto.toAuditLog
import com.ptpws.ikikasir.feature.auditlog.data.remote.dto.toAuditLogDto
import com.ptpws.ikikasir.feature.auditlog.domain.model.AuditLog
import com.ptpws.ikikasir.feature.auditlog.domain.repository.AuditLogRepository
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    private val userDao: UserDao
) : AuditLogRepository {

    override fun getAuditLogs(): Flow<List<AuditLog>> {
        // Trigger background sync when flow is collected
        CoroutineScope(Dispatchers.IO).launch {
            syncUnsyncedLogs()
            refreshFromRemote()
        }
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

        if (finalActorName.isBlank() || finalActorId.isBlank()) {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                finalActorId = currentUser.uid
                val email = currentUser.email ?: ""
                val localUser = if (email.isNotBlank()) userDao.getUserByEmail(email) else null
                finalActorName = localUser?.fullName ?: currentUser.displayName ?: email.ifBlank { "System User" }
                finalActorRole = localUser?.roleId ?: "Staff"
            } else {
                finalActorName = "System"
                finalActorRole = "System"
            }
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

        // 2. Try saving to Firestore
        val isSavedRemote = remoteDataSource.saveAuditLog(newLog.toAuditLogDto())
        if (isSavedRemote) {
            auditLogDao.markAsSynced(newLog.id)
        }
    }

    override suspend fun syncUnsyncedLogs() {
        try {
            val unsynced = auditLogDao.getUnsyncedAuditLogs()
            for (logEntity in unsynced) {
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
        try {
            val remoteDtos = remoteDataSource.getAllAuditLogs()
            if (remoteDtos.isNotEmpty()) {
                val entities = remoteDtos.map { dto ->
                    dto.toAuditLog().toAuditLogEntity()
                }
                auditLogDao.insertAuditLogs(entities)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing audit logs from remote: ${e.message}")
        }
    }
}
