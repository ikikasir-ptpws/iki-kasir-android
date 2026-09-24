package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.TaxSettingDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxSettingRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TaxSettingRemoteDataSource {

    private val collection = firestore.collection("tax_settings")

    override suspend fun getTaxSetting(): TaxSettingDto? {
        val doc = collection.document("default").get().await()
        return if (doc.exists()) {
            doc.toObject(TaxSettingDto::class.java)?.apply {
                id = doc.id
            }
        } else {
            null
        }
    }

    override suspend fun saveTaxSetting(dto: TaxSettingDto) {
        collection.document(dto.id.ifBlank { "default" }).set(dto).await()
    }
}
