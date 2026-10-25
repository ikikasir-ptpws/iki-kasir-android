package com.ptpws.ikikasir.core.storage.data.sync

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ptpws.ikikasir.core.database.AppDatabase
import com.ptpws.ikikasir.core.storage.data.local.PendingImageEntity
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.NotaSettingPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.tasks.await
import java.io.File

class PendingImageSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = EntryPointAccessors.fromApplication(
            applicationContext,
            DatabaseEntryPoint::class.java
        ).database()
        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        for (item in database.pendingImageDao.getPending()) {
            try {
                val file = File(item.localPath)
                if (!file.isFile) {
                    Log.e(TAG, "Queued image no longer exists: ${item.localPath}")
                    database.pendingImageDao.remove(item.id)
                    continue
                }

                val document = firestore.collection(item.collection).document(item.documentId)
                if (!document.get().await().exists()) {
                    return Result.retry()
                }

                val safeId = item.documentId.replace(Regex("[^A-Za-z0-9_-]"), "_")
                val reference = storage.reference.child(
                    "${item.storageFolder}/${safeId}_${item.createdAt}.jpg"
                )
                reference.putFile(Uri.fromFile(file)).await()
                val downloadUrl = reference.downloadUrl.await().toString()
                document.update(item.firestoreField, downloadUrl).await()
                updateLocalImage(database, item, downloadUrl)
                database.pendingImageDao.remove(item.id)
                file.delete()
            } catch (error: Exception) {
                Log.w(TAG, "Image sync failed for ${item.id}: ${error.message}", error)
                return Result.retry()
            }
        }
        return Result.success()
    }

    private suspend fun updateLocalImage(
        database: AppDatabase,
        item: PendingImageEntity,
        downloadUrl: String
    ) {
        when (item.collection) {
            "products" -> database.produkDao.updateImageUrl(item.documentId, downloadUrl)
            "users" -> database.userDao.updatePhotoUrl(item.documentId, downloadUrl)
            "transactions" -> database.transactionDao.updatePaymentProofUrl(item.documentId, downloadUrl)
            "financial_records" -> database.catatanKeuanganDao.updateProofImageUrl(item.documentId, downloadUrl)
            "nota_settings" -> {
                database.notaSettingDao.updateLogoUrl(item.documentId, downloadUrl)
                database.notaSettingDao.getNotaSetting(item.documentId)?.let { setting ->
                    NotaSettingPreferences(applicationContext).saveSetting(setting.toDomain())
                }
            }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DatabaseEntryPoint {
        fun database(): AppDatabase
    }

    private companion object {
        const val TAG = "PendingImageSync"
    }
}
