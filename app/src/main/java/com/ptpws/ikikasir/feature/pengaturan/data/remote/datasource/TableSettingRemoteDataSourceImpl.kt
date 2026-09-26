package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.TableSettingDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val COLLECTION_TABLE_SETTINGS = "table_settings"

class TableSettingRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TableSettingRemoteDataSource {

    override suspend fun getSetting(id: String): TableSettingDto? {
        val snapshot = firestore.collection(COLLECTION_TABLE_SETTINGS)
            .document(id)
            .get()
            .await()
        return if (snapshot.exists()) {
            snapshot.toObject(TableSettingDto::class.java)
        } else {
            null
        }
    }

    override suspend fun saveSetting(dto: TableSettingDto) {
        firestore.collection(COLLECTION_TABLE_SETTINGS)
            .document(dto.id)
            .set(dto)
            .await()
    }
}
