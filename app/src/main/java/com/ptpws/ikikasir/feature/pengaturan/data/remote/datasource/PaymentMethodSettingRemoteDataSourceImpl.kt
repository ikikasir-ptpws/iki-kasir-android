package com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.PaymentMethodSettingDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val COLLECTION_PAYMENT_METHOD_SETTINGS = "payment_method_settings"

class PaymentMethodSettingRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PaymentMethodSettingRemoteDataSource {

    override suspend fun getSetting(id: String): PaymentMethodSettingDto? {
        val snapshot = firestore.collection(COLLECTION_PAYMENT_METHOD_SETTINGS)
            .document(id)
            .get()
            .await()
        return if (snapshot.exists()) {
            snapshot.toObject(PaymentMethodSettingDto::class.java)
        } else {
            null
        }
    }

    override suspend fun saveSetting(dto: PaymentMethodSettingDto) {
        firestore.collection(COLLECTION_PAYMENT_METHOD_SETTINGS)
            .document(dto.id)
            .set(dto)
            .await()
    }
}
