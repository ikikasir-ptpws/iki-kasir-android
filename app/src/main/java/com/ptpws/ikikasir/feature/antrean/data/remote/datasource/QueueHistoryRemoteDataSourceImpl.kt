package com.ptpws.ikikasir.feature.antrean.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ptpws.ikikasir.feature.antrean.data.remote.dto.QueueHistoryDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "QueueHistoryRemote"
private const val COLLECTION_NAME = "queueHistory"

class QueueHistoryRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : QueueHistoryRemoteDataSource {

    private val collectionRef
        get() = firestore.collection(COLLECTION_NAME)

    override suspend fun getAllHistory(): List<QueueHistoryDto> {
        return try {
            val snapshot = collectionRef
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(QueueHistoryDto::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching queueHistory from Firestore: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun saveHistory(dto: QueueHistoryDto) {
        val docRef = if (dto.id.isNotBlank()) {
            collectionRef.document(dto.id)
        } else {
            collectionRef.document()
        }
        dto.id = docRef.id
        docRef.set(dto).await()
        Log.d(TAG, "QueueHistory saved to Firestore with ID '${dto.id}'")
    }

    override fun getHistoryFlow(): Flow<List<QueueHistoryDto>> = callbackFlow {
        val listener = collectionRef
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "SnapshotListener error: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(QueueHistoryDto::class.java)
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }
}
