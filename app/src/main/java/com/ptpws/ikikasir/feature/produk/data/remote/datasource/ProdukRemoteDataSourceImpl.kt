package com.ptpws.ikikasir.feature.produk.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.produk.data.remote.dto.ProdukDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProdukRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProdukRemoteDataSource {

    private val collection = firestore.collection("products")

    override fun getProdukFlow(): Flow<List<ProdukDto>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ProdukDto::class.java)?.apply {
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

    override suspend fun getAllProduk(): List<ProdukDto> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(ProdukDto::class.java)?.apply {
                if (id.isBlank()) id = doc.id
            }
        }
    }

    override suspend fun getProdukById(id: String): ProdukDto? {
        val doc = collection.document(id).get().await()
        return doc.toObject(ProdukDto::class.java)?.apply {
            if (this.id.isBlank()) this.id = doc.id
        }
    }

    override suspend fun saveProduk(produkDto: ProdukDto) {
        collection.document(produkDto.id).set(produkDto).await()
    }

    override suspend fun updateProduk(produkDto: ProdukDto) {
        collection.document(produkDto.id).set(produkDto).await()
    }

    override suspend fun deleteProduk(id: String) {
        collection.document(id).delete().await()
    }
}