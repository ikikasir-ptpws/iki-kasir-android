package com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto.UserDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRemoteDataSource {

    private val collection = firestore.collection("users")

    override fun getUserFlow(): Flow<List<UserDto>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(UserDto::class.java)?.apply {
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

    override suspend fun getAllUsers(): List<UserDto> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(UserDto::class.java)?.apply {
                if (id.isBlank()) id = doc.id
            }
        }
    }

    override suspend fun getUserById(id: String): UserDto? {
        val doc = collection.document(id).get().await()
        return doc.toObject(UserDto::class.java)?.apply {
            if (this.id.isBlank()) this.id = doc.id
        }
    }

    override suspend fun saveUser(userDto: UserDto) {
        collection.document(userDto.id).set(userDto).await()
    }

    override suspend fun updateUser(userDto: UserDto) {
        collection.document(userDto.id).set(userDto).await()
    }

    override suspend fun deleteUser(id: String) {
        collection.document(id).delete().await()
    }
}
