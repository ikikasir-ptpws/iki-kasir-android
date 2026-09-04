package com.ptpws.ikikasir.feature.kategori.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.kategori.data.remote.dto.KategoriDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KategoriRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : KategoriRemoteDataSource {

    private val collection = firestore.collection("kategori")

    override fun getKategoriFlow(): Flow<List<KategoriDto>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { it.toObject(KategoriDto::class.java) }
                trySend(list)
            }
        }
        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getAllKategori(): List<KategoriDto> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(KategoriDto::class.java) }
    }

    override suspend fun getKategoriById(id: String): KategoriDto? {
        val doc = collection.document(id).get().await()
        return doc.toObject(KategoriDto::class.java)
    }

    override suspend fun saveKategori(kategoriDto: KategoriDto) {
        collection.document(kategoriDto.id).set(kategoriDto).await()
    }

    override suspend fun updateKategori(kategoriDto: KategoriDto) {
        collection.document(kategoriDto.id).set(kategoriDto).await()
    }

    override suspend fun deleteKategori(id: String) {
        collection.document(id).delete().await()
    }
}
