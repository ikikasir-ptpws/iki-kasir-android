package com.ptpws.ikikasir.feature.catatankeuangan.data.remote.datasource

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.core.storage.domain.model.isLocalImageUri
import com.ptpws.ikikasir.feature.catatankeuangan.data.remote.dto.CatatanKeuanganDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CatatanKeuanganRemote"
private const val COLLECTION_CATATAN = "financial_records"
private const val LEGACY_COLLECTION_CATATAN = "catatan_keuangan"

@Singleton
class CatatanKeuanganRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CatatanKeuanganRemoteDataSource {

    override suspend fun getAllCatatan(): List<CatatanKeuanganDto> {
        return try {
            val legacy = firestore.collection(LEGACY_COLLECTION_CATATAN).get().await()
                .documents.mapNotNull { it.toCatatanDtoSafely() }
            val current = firestore.collection(COLLECTION_CATATAN).get().await()
                .documents.mapNotNull { it.toCatatanDtoSafely() }
            (legacy.associateBy { it.id } + current.associateBy { it.id }).values.toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching catatan from Firestore: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun saveCatatan(dto: CatatanKeuanganDto) {
        val docRef = if (dto.id.isBlank()) {
            val newDoc = firestore.collection(COLLECTION_CATATAN).document()
            dto.id = newDoc.id
            newDoc
        } else {
            firestore.collection(COLLECTION_CATATAN).document(dto.id)
        }
        docRef.set(dto).await()
    }

    override suspend fun updateCatatan(dto: CatatanKeuanganDto) {
        firestore.collection(COLLECTION_CATATAN).document(dto.id).set(dto).await()
    }

    override suspend fun deleteCatatan(id: String) {
        firestore.collection(COLLECTION_CATATAN).document(id).delete().await()
        firestore.collection(LEGACY_COLLECTION_CATATAN).document(id).delete().await()
    }

    private fun DocumentSnapshot.toCatatanDtoSafely(): CatatanKeuanganDto? {
        return try {
            toCatatanDto()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing doc $id: ${e.message}", e)
            null
        }
    }

    private fun DocumentSnapshot.toCatatanDto(): CatatanKeuanganDto? {
        if (!exists()) return null
        val docId = getString("id") ?: id
        val titleVal = getString("title") ?: getString("judul") ?: ""
        val typeVal = getString("type") ?: getString("tipe") ?: "PEMASUKAN"
        val amountVal = getDouble("amount") ?: getDouble("jumlah") ?: 0.0
        val dateVal = getString("date") ?: getString("tanggal") ?: ""
        val noteVal = getString("note") ?: getString("keterangan") ?: ""
        val paymentMethodVal = getString("paymentMethod") ?: getString("metodePembayaran") ?: "Tunai"
        val proofImageUrlVal = (getString("proofImageUrl") ?: getString("buktiFotoUrl") ?: "")
            .takeUnless(::isLocalImageUri)
            .orEmpty()
        val createdAtVal = getTimestamp("createdAt") ?: Timestamp.now()
        val updatedAtVal = getTimestamp("updatedAt") ?: Timestamp.now()

        return CatatanKeuanganDto(
            id = docId,
            title = titleVal,
            type = typeVal,
            amount = amountVal,
            date = dateVal,
            note = noteVal,
            paymentMethod = paymentMethodVal,
            proofImageUrl = proofImageUrlVal,
            createdAt = createdAtVal,
            updatedAt = updatedAtVal
        )
    }
}
