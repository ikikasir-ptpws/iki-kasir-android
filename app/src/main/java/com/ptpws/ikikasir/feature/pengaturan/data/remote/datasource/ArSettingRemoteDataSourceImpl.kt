package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.ArSettingDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val COLLECTION_AR_SETTINGS = "ar_settings"

class ArSettingRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ArSettingRemoteDataSource {

    override suspend fun getSetting(id: String): ArSettingDto? {
        val snapshot = firestore.collection(COLLECTION_AR_SETTINGS)
            .document(id)
            .get()
            .await()
        return if (snapshot.exists()) {
            snapshot.toObject(ArSettingDto::class.java)
        } else {
            null
        }
    }

    override suspend fun saveSetting(dto: ArSettingDto) {
        firestore.collection(COLLECTION_AR_SETTINGS)
            .document(dto.id)
            .set(dto)
            .await()
    }
}
