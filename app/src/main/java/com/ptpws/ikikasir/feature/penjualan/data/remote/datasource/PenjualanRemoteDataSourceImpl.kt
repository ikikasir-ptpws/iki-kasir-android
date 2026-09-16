package com.ptpws.ikikasir.feature.penjualan.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.penjualan.data.remote.dto.TransactionDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PenjualanRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PenjualanRemoteDataSource {

    private val collection = firestore.collection("transactions")

    override suspend fun getAllTransactions(): List<TransactionDto> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(TransactionDto::class.java)?.apply {
                if (transactionId.isBlank()) transactionId = doc.id
            }
        }
    }

    override suspend fun saveTransaction(dto: TransactionDto) {
        collection.document(dto.transactionId).set(dto).await()
    }
}
