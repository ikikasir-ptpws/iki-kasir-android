package com.ptpws.ikikasir.feature.auditlog.data.remote.datasource

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ptpws.ikikasir.feature.auditlog.data.remote.dto.AuditLogDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuditLogRemoteDataSource"
private const val COLLECTION_AUDIT_LOGS = "audit_logs"

@Singleton
class AuditLogRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AuditLogRemoteDataSource {

    override suspend fun saveAuditLog(dto: AuditLogDto): Boolean {
        return try {
            firestore.collection(COLLECTION_AUDIT_LOGS)
                .document(dto.id)
                .set(dto)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving audit log to Firestore: ${e.message}", e)
            false
        }
    }

    override suspend fun getAllAuditLogs(): List<AuditLogDto> {
        return try {
            val snapshot = firestore.collection(COLLECTION_AUDIT_LOGS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val title = doc.getString("title") ?: ""
                    val description = doc.getString("description") ?: ""
                    val category = doc.getString("category") ?: "TRANSACTION"
                    val action = doc.getString("action") ?: ""
                    val actorId = doc.getString("actorId") ?: ""
                    val actorName = doc.getString("actorName") ?: ""
                    val actorRole = doc.getString("actorRole") ?: ""
                    val isWarning = doc.getBoolean("isWarning") ?: false
                    val createdAt = doc.getTimestamp("createdAt")

                    AuditLogDto(
                        id = id,
                        title = title,
                        description = description,
                        category = category,
                        action = action,
                        actorId = actorId,
                        actorName = actorName,
                        actorRole = actorRole,
                        isWarning = isWarning,
                        createdAt = createdAt
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing audit log doc ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching audit logs from Firestore: ${e.message}", e)
            emptyList()
        }
    }
}
