package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.NotaSettingDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotaSettingRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotaSettingRemoteDataSource {

    private val collectionRef = firestore.collection("nota_settings")

    override fun getNotaSettingFlow(id: String): Flow<NotaSettingDto?> = callbackFlow {
        val registration = collectionRef.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else {
                trySend(snapshot?.takeIf { it.exists() }?.toObject(NotaSettingDto::class.java))
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getNotaSetting(id: String): NotaSettingDto? {
        val snapshot = collectionRef.document(id).get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(NotaSettingDto::class.java)
        } else {
            null
        }
    }

    override suspend fun saveNotaSetting(setting: NotaSettingDto) {
        collectionRef.document(setting.id).set(setting).await()
    }
}
