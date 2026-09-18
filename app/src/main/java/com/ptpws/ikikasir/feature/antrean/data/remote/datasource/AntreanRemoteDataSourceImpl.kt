package com.ptpws.ikikasir.feature.antrean.data.remote.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.antrean.data.remote.dto.AntreanDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AntreanRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AntreanRemoteDataSource {

    private val collection = firestore.collection("queues")

    override fun getAntreanFlow(): Flow<List<AntreanDto>> = callbackFlow {
        val listener = collection
            .orderBy("queueSequence")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(AntreanDto::class.java)?.apply {
                            if (id.isBlank()) id = doc.id
                        }
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getAllAntrean(): List<AntreanDto> {
        val snapshot = collection.orderBy("queueSequence").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(AntreanDto::class.java)?.apply {
                if (id.isBlank()) id = doc.id
            }
        }
    }

    override suspend fun getAntreanById(id: String): AntreanDto? {
        val doc = collection.document(id).get().await()
        return doc.toObject(AntreanDto::class.java)?.apply {
            if (this.id.isBlank()) this.id = doc.id
        }
    }

    override suspend fun saveAntrean(antreanDto: AntreanDto) {
        collection.document(antreanDto.id).set(antreanDto).await()
    }

    override suspend fun updateAntreanStatus(id: String, status: String, updatedAt: Timestamp) {
        collection.document(id).update(
            mapOf(
                "status" to status,
                "updatedAt" to updatedAt
            )
        ).await()
    }

    override suspend fun deleteAntrean(id: String) {
        collection.document(id).delete().await()
    }
}
