package com.ptpws.ikikasir.core.storage.domain.usecase

import com.ptpws.ikikasir.core.storage.domain.model.StoragePath
import com.ptpws.ikikasir.core.storage.domain.repository.StorageRepository

import javax.inject.Inject

/**
 * Use case untuk mengupload gambar produk ke Firebase Storage.
 * Mengembalikan download URL yang disimpan di Firestore.
 */
class UploadProductImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        localFilePathOrUri: String,
        productId: String,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        val fileName = "product_${productId}_${System.currentTimeMillis()}.jpg"
        return storageRepository.uploadImage(
            localFilePathOrUri = localFilePathOrUri,
            folder = StoragePath.PRODUCT_IMAGES,
            fileName = fileName,
            onProgress = onProgress
        )
    }
}

/**
 * Use case untuk mengupload foto profil pengguna ke Firebase Storage.
 */
class UploadProfilePhotoUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        localFilePathOrUri: String,
        userId: String,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        val fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg"
        return storageRepository.uploadImage(
            localFilePathOrUri = localFilePathOrUri,
            folder = StoragePath.PROFILE_PHOTOS,
            fileName = fileName,
            onProgress = onProgress
        )
    }
}

/**
 * Use case untuk mengupload bukti pembayaran ke Firebase Storage.
 */
class UploadPaymentProofUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        localFilePathOrUri: String,
        transactionId: String,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        val fileName = "payment_${transactionId}_${System.currentTimeMillis()}.jpg"
        return storageRepository.uploadImage(
            localFilePathOrUri = localFilePathOrUri,
            folder = StoragePath.PAYMENT_PROOFS,
            fileName = fileName,
            onProgress = onProgress
        )
    }
}

class UploadFinancialRecordProofUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        localFilePathOrUri: String,
        recordId: String,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        val fileName = "financial_record_${recordId}_${System.currentTimeMillis()}.jpg"
        return storageRepository.uploadImage(
            localFilePathOrUri = localFilePathOrUri,
            folder = StoragePath.FINANCIAL_RECORD_PAYMENT_PROOFS,
            fileName = fileName,
            onProgress = onProgress
        )
    }
}

/**
 * Use case untuk mengupload logo toko (struk/nota) ke Firebase Storage.
 */
class UploadStoreLogoUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        localFilePathOrUri: String,
        storeId: String = "default",
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        val fileName = "logo_${storeId}_${System.currentTimeMillis()}.jpg"
        return storageRepository.uploadImage(
            localFilePathOrUri = localFilePathOrUri,
            folder = StoragePath.STORE_LOGOS,
            fileName = fileName,
            onProgress = onProgress
        )
    }
}

/**
 * Use case untuk mengupload gambar kategori ke Firebase Storage.
 */
class UploadCategoryImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(
        localFilePathOrUri: String,
        categoryId: String,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String> {
        val fileName = "category_${categoryId}_${System.currentTimeMillis()}.jpg"
        return storageRepository.uploadImage(
            localFilePathOrUri = localFilePathOrUri,
            folder = StoragePath.CATEGORY_IMAGES,
            fileName = fileName,
            onProgress = onProgress
        )
    }
}

/**
 * Use case untuk menghapus gambar dari Firebase Storage.
 * Berguna ketika produk/kategori/pengguna dihapus.
 */
class DeleteStorageImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(imageUrl: String): Result<Unit> {
        if (imageUrl.isBlank()) return Result.success(Unit)
        val isStorageUrl = imageUrl.startsWith("gs://") ||
            imageUrl.startsWith("https://firebasestorage.googleapis.com/")
        if (
            imageUrl.startsWith("http://") ||
            imageUrl.startsWith("https://") ||
            imageUrl.startsWith("data:") ||
            imageUrl.startsWith("file://") ||
            imageUrl.startsWith("content://")
        ) {
            if (!isStorageUrl) return Result.success(Unit)
        }
        return storageRepository.deleteImage(imageUrl)
    }
}
