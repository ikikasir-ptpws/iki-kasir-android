package com.ptpws.ikikasir.feature.promo.data.remote.datasource

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
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
            snapshot.documents.mapNotNull { it.toPromoDto() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching promos from Firestore: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getPromoById(id: String): PromoDto? {
        return try {
            val snapshot = firestore.collection(COLLECTION_PROMOS).document(id).get().await()
            snapshot.toPromoDto()
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

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toPromoDto(): PromoDto? {
        if (!exists()) return null
        val docId = getString("id") ?: id
        val nameVal = getString("name")?.takeIf { it.isNotBlank() }
            ?: getString("nama") ?: ""
        val promoTypeVal = getString("promoType")?.takeIf { it.isNotBlank() }
            ?: getString("tipePromo") ?: ""
        val discountTypeVal = getString("discountType")?.takeIf { it.isNotBlank() }
            ?: getString("diskonType") ?: "Rp"
        val discountValueVal = getDouble("discountValue") ?: getDouble("nilaiDiskon") ?: 0.0
        val startDateVal = getString("startDate")?.takeIf { it.isNotBlank() }
            ?: getString("tanggalMulai") ?: ""
        val endDateVal = getString("endDate")?.takeIf { it.isNotBlank() }
            ?: getString("tanggalBerakhir") ?: ""
        val isActiveVal = getBoolean("isActive") ?: true
        val createdAtVal = getTimestamp("createdAt") ?: Timestamp.now()
        val updatedAtVal = getTimestamp("updatedAt") ?: Timestamp.now()
        val rawItems = get("items") as? List<Map<String, Any>> ?: emptyList()

        return PromoDto(
            id = docId,
            name = nameVal,
            promoType = promoTypeVal,
            items = rawItems,
            discountType = discountTypeVal,
            discountValue = discountValueVal,
            startDate = startDateVal,
            endDate = endDateVal,
            isActive = isActiveVal,
            createdAt = createdAtVal,
            updatedAt = updatedAtVal
        )
    }
}
