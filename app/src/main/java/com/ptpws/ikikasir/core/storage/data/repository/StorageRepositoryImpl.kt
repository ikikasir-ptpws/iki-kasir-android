package com.ptpws.ikikasir.core.storage.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.ptpws.ikikasir.core.util.ImageCompressorHelper
import com.ptpws.ikikasir.core.storage.domain.model.StoragePath
import com.ptpws.ikikasir.core.storage.domain.repository.StorageRepository
import com.ptpws.ikikasir.core.storage.domain.repository.StorageUploadState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StorageRepositoryImpl"

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : StorageRepository {

    override suspend fun uploadImage(
        localFilePathOrUri: String,
        folder: String,
        fileName: String?,
        onProgress: ((Float) -> Unit)?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Resolve URI / File
            val sourceUri = resolveUri(localFilePathOrUri)

            // 2. Kompresi & proses gambar terlebih dahulu (maks 2MB)
            onProgress?.invoke(0.1f)
            val compressedFile = ImageCompressorHelper.processAndCompressImage(
                context = context,
                sourceUri = sourceUri,
                targetSubfolder = folder,
                onProgress = { p, _ ->
                    // Kompresi menempati 60% dari total progress (0.1 → 0.7)
                    onProgress?.invoke(0.1f + p * 0.6f)
                }
            )

            // 3. Tentukan nama file di Storage
            val finalFileName = fileName ?: "${UUID.randomUUID()}.jpg"
            val storageRef = firebaseStorage.reference.child("$folder/$finalFileName")

            onProgress?.invoke(0.7f)

            // 4. Upload ke Firebase Storage
            val fileUri = Uri.fromFile(compressedFile)
            val uploadTask = storageRef.putFile(fileUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
                // Upload menempati 25% dari total progress (0.7 → 0.95)
                onProgress?.invoke(0.7f + progress * 0.25f)
            }.await()

            onProgress?.invoke(1.0f)

            // 5. Ambil download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "Upload sukses: $folder/$finalFileName -> $downloadUrl")

            // 6. Bersihkan file sementara di penyimpanan internal
            try {
                compressedFile.delete()
            } catch (e: Exception) {
                Log.w(TAG, "Gagal hapus file sementara: ${e.message}")
            }

            Result.success(downloadUrl)
        } catch (e: StorageException) {
            Log.e(TAG, "Firebase Storage error: ${e.message}", e)
            Result.failure(Exception("Gagal upload ke Storage: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Upload error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteImage(imageUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val storageRef = if (
                imageUrl.startsWith("https://") ||
                imageUrl.startsWith("http://") ||
                imageUrl.startsWith("gs://")
            ) {
                // URL download lengkap → pakai getReferenceFromUrl
                firebaseStorage.getReferenceFromUrl(imageUrl)
            } else {
                // Path relatif di Storage
                firebaseStorage.reference.child(imageUrl)
            }
            storageRef.delete().await()
            Log.d(TAG, "Gambar berhasil dihapus dari Storage: $imageUrl")
            Result.success(Unit)
        } catch (e: StorageException) {
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                Log.w(TAG, "Gambar tidak ditemukan di Storage (sudah dihapus?): $imageUrl")
                Result.success(Unit) // Anggap sukses jika file tidak ditemukan
            } else {
                Log.e(TAG, "Storage delete error: ${e.message}", e)
                Result.failure(Exception("Gagal hapus gambar: ${e.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Delete error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun uploadImageFlow(
        localFilePathOrUri: String,
        folder: String,
        fileName: String?
    ): Flow<StorageUploadState> = callbackFlow {
        try {
            val sourceUri = resolveUri(localFilePathOrUri)

            trySend(StorageUploadState.Progress(0.1f, "Memproses gambar..."))

            val compressedFile = withContext(Dispatchers.IO) {
                ImageCompressorHelper.processAndCompressImage(
                    context = context,
                    sourceUri = sourceUri,
                    targetSubfolder = folder,
                    onProgress = { p, msg ->
                        trySend(StorageUploadState.Progress(0.1f + p * 0.6f, msg))
                    }
                )
            }

            val finalFileName = fileName ?: "${UUID.randomUUID()}.jpg"
            val storageRef = firebaseStorage.reference.child("$folder/$finalFileName")
            val fileUri = Uri.fromFile(compressedFile)

            trySend(StorageUploadState.Progress(0.7f, "Mengunggah ke server..."))

            val uploadTask = storageRef.putFile(fileUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
                trySend(StorageUploadState.Progress(0.7f + progress * 0.25f, "Mengunggah ${(progress * 100).toInt()}%..."))
            }

            uploadTask.await()

            val downloadUrl = storageRef.downloadUrl.await().toString()

            try { compressedFile.delete() } catch (_: Exception) {}

            trySend(StorageUploadState.Success(downloadUrl))
        } catch (e: Exception) {
            trySend(StorageUploadState.Failure(e))
        }
    }

    /**
     * Resolving URI dari string (bisa berupa file path, content:// URI, atau file:// URI).
     */
    private fun resolveUri(localFilePathOrUri: String): Uri {
        return when {
            localFilePathOrUri.startsWith("content://") ||
                    localFilePathOrUri.startsWith("file://") -> Uri.parse(localFilePathOrUri)
            else -> Uri.fromFile(File(localFilePathOrUri))
        }
    }
}
