package com.ptpws.ikikasir.feature.keuangan.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ptpws.ikikasir.feature.keuangan.data.remote.dto.CatatanKeuanganDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatatanKeuanganRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CatatanKeuanganRemoteDataSource {

    private val collection = firestore.collection("financial_records")

    override fun getCatatanFlow(): Flow<List<CatatanKeuanganDto>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CatatanKeuanganDto::class.java)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveCatatan(dto: CatatanKeuanganDto) {
        collection.document(dto.id).set(dto).await()
    }

    override suspend fun deleteCatatan(id: String) {
        collection.document(id).delete().await()
    }
}
