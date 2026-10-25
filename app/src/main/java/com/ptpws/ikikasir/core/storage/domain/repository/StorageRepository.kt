package com.ptpws.ikikasir.core.storage.domain.repository

import com.ptpws.ikikasir.core.storage.domain.model.StoragePath
import kotlinx.coroutines.flow.Flow

/**
 * Interface domain untuk operasi Firebase Storage.
 * Clean Architecture — Domain Layer (dikonsumsi oleh UseCase).
 */
interface StorageRepository {

    /**
     * Upload gambar ke Firebase Storage dan kembalikan URL download-nya.
     * @param localFilePathOrUri Path file lokal atau URI string
     * @param folder Folder tujuan di Storage (lihat [StoragePath])
     * @param fileName Nama file di Storage (jika null, digenerate otomatis)
     * @param onProgress Callback progress upload (0.0 - 1.0)
     * @return Download URL gambar yang sudah diupload
     */
    suspend fun uploadImage(
        localFilePathOrUri: String,
        folder: String,
        fileName: String? = null,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String>

    /**
     * Hapus gambar dari Firebase Storage berdasarkan download URL atau path storage.
     * @param imageUrl Download URL atau storage path gambar
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit>

    /**
     * Upload gambar dengan progress tracking sebagai Flow.
     */
    fun uploadImageFlow(
        localFilePathOrUri: String,
        folder: String,
        fileName: String? = null
    ): Flow<StorageUploadState>
}

/**
 * State untuk upload progress berbasis Flow.
 */
sealed class StorageUploadState {
    data class Progress(val progress: Float, val message: String = "") : StorageUploadState()
    data class Success(val downloadUrl: String) : StorageUploadState()
    data class Failure(val exception: Throwable) : StorageUploadState()
}
