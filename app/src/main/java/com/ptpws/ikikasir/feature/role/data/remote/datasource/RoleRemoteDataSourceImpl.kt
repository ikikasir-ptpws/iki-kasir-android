package com.ptpws.ikikasir.feature.role.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.role.data.remote.dto.RoleDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RoleRemoteDataSource {

    private val collection = firestore.collection("roles")

    override fun getRoleFlow(): Flow<List<RoleDto>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RoleDto::class.java)?.apply {
                        if (id.isBlank()) id = doc.id
                    }
                }
                trySend(list)
            }
        }
        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getAllRoles(): List<RoleDto> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(RoleDto::class.java)?.apply {
                if (id.isBlank()) id = doc.id
            }
        }
    }

    override suspend fun getRoleById(id: String): RoleDto? {
        val doc = collection.document(id).get().await()
        return doc.toObject(RoleDto::class.java)?.apply {
            if (this.id.isBlank()) this.id = doc.id
        }
    }

    override suspend fun saveRole(roleDto: RoleDto) {
        collection.document(roleDto.id).set(roleDto).await()
    }

    override suspend fun updateRole(roleDto: RoleDto) {
        collection.document(roleDto.id).set(roleDto).await()
    }

    override suspend fun deleteRole(id: String) {
        collection.document(id).delete().await()
    }
}
