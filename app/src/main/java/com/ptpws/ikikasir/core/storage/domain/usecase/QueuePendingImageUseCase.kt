package com.ptpws.ikikasir.core.storage.domain.usecase

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ptpws.ikikasir.core.storage.data.local.PendingImageDao
import com.ptpws.ikikasir.core.storage.data.local.PendingImageEntity
import com.ptpws.ikikasir.core.storage.data.sync.PendingImageSyncWorker
import com.ptpws.ikikasir.core.storage.domain.model.PendingImageTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueuePendingImageUseCase @Inject constructor(
    private val pendingImageDao: PendingImageDao,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        localFilePathOrUri: String,
        target: PendingImageTarget,
        documentId: String
    ) {
        val localPath = when {
            localFilePathOrUri.startsWith("file://") ->
                android.net.Uri.parse(localFilePathOrUri).path
            else -> localFilePathOrUri
        } ?: error("Lokasi gambar lokal tidak valid.")
        require(File(localPath).isFile) { "File gambar lokal tidak ditemukan." }

        val queueId = "${target.collection}/$documentId/${target.field}"
        pendingImageDao.enqueue(
            PendingImageEntity(
                id = queueId,
                localPath = localPath,
                storageFolder = target.storageFolder,
                collection = target.collection,
                documentId = documentId,
                firestoreField = target.field
            )
        )

        val request = OneTimeWorkRequestBuilder<PendingImageSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    private companion object {
        const val WORK_NAME = "pending-image-sync"
    }
}
