package com.ptpws.ikikasir.feature.promo.data.remote.datasource

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.promo.data.remote.dto.PromoDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PromoRemoteDataSource"
private const val COLLECTION_PROMOS = "promos"

@Singleton
class PromoRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PromoRemoteDataSource {

    override suspend fun getAllPromos(): List<PromoDto> {
        return try {
            val snapshot = firestore.collection(COLLECTION_PROMOS).get().await()
            snapshot.toObjects(PromoDto::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching promos from Firestore: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getPromoById(id: String): PromoDto? {
        return try {
            val snapshot = firestore.collection(COLLECTION_PROMOS).document(id).get().await()
            snapshot.toObject(PromoDto::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching promo '$id' from Firestore: ${e.message}", e)
            null
        }
    }

    override suspend fun savePromo(promoDto: PromoDto) {
        val docRef = if (promoDto.id.isBlank()) {
            val newDoc = firestore.collection(COLLECTION_PROMOS).document()
            promoDto.id = newDoc.id
            newDoc
        } else {
            firestore.collection(COLLECTION_PROMOS).document(promoDto.id)
        }
        docRef.set(promoDto).await()
    }

    override suspend fun updatePromo(promoDto: PromoDto) {
        firestore.collection(COLLECTION_PROMOS)
            .document(promoDto.id)
            .set(promoDto)
            .await()
    }

    override suspend fun toggleStatus(id: String, isActive: Boolean) {
        firestore.collection(COLLECTION_PROMOS)
            .document(id)
            .update(
                mapOf(
                    "isActive" to isActive,
                    "updatedAt" to Timestamp.now()
                )
            )
            .await()
    }

    override suspend fun deletePromo(id: String) {
        firestore.collection(COLLECTION_PROMOS)
            .document(id)
            .delete()
            .await()
    }
}
