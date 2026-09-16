package com.ptpws.ikikasir.feature.manajemenstok.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.manajemenstok.data.remote.dto.StockMovementDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StokAdjustmentRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : StokAdjustmentRemoteDataSource {

    /** Firestore collection: "stock_movements" sesuai schema */
    private val collection = firestore.collection("stock_movements")

    override suspend fun getAllMovements(): List<StockMovementDto> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(StockMovementDto::class.java)?.apply {
                if (movementId.isBlank()) movementId = doc.id
            }
        }
    }

    override suspend fun saveMovement(dto: StockMovementDto) {
        collection.document(dto.movementId).set(dto).await()
    }
}
