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
                val list = snapshot.documents.mapNotNull { doc -> parseProdukDoc(doc) }
                trySend(list)
            }
        }
        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getAllProduk(): List<ProdukDto> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc -> parseProdukDoc(doc) }
    }

    override suspend fun getProdukById(id: String): ProdukDto? {
        val doc = collection.document(id).get().await()
        return parseProdukDoc(doc)
    }

    private fun parseProdukDoc(doc: com.google.firebase.firestore.DocumentSnapshot): ProdukDto? {
        val dto = doc.toObject(ProdukDto::class.java) ?: return null
        if (dto.id.isBlank()) {
            dto.id = doc.id
        }

        // Resolusi harga jika sellingPrice bernilai 0 atau tersimpan dengan nama field alternatif di Firestore
        if (dto.sellingPrice <= 0.0) {
            val altPrice = doc.getDouble("price")
                ?: doc.getLong("price")?.toDouble()
                ?: doc.getString("price")?.toDoubleOrNull()
                ?: doc.getDouble("harga")
                ?: doc.getLong("harga")?.toDouble()
                ?: doc.getString("harga")?.toDoubleOrNull()
                ?: doc.getDouble("selling_price")
                ?: doc.getLong("selling_price")?.toDouble()
                ?: doc.getString("selling_price")?.toDoubleOrNull()
                ?: 0.0
            if (altPrice > 0.0) {
                dto.sellingPrice = altPrice
            }
        }

        // Resolusi categoryId jika kosong atau tersimpan dengan nama field alternatif di Firestore
        if (dto.categoryId.isBlank()) {
            val altCategory = doc.getString("category")
                ?: doc.getString("category_id")
                ?: doc.getString("kategori")
                ?: doc.getString("kategoriId")
                ?: ""
            dto.categoryId = altCategory
        }

        return dto
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