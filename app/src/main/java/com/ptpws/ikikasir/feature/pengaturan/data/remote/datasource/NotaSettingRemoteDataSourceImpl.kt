package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.NotaSettingDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotaSettingRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotaSettingRemoteDataSource {

    private val collectionRef = firestore.collection("nota_settings")

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
