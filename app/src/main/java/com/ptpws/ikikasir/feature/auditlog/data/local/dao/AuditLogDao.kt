package com.ptpws.ikikasir.feature.auditlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ptpws.ikikasir.feature.auditlog.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE isSynced = 0")
    suspend fun getUnsyncedAuditLogs(): List<AuditLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(auditLog: AuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuditLogs(auditLogs: List<AuditLogEntity>)

    @Query("UPDATE audit_logs SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("UPDATE audit_logs SET actorRole = :actorRole, isSynced = :isSynced WHERE id = :id AND isSynced = 1")
    suspend fun updateSyncedActorRole(id: String, actorRole: String, isSynced: Boolean)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAll()
}
