package com.ptpws.ikikasir.core.util

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseStorageHelper {

    suspend fun uploadImageToStorage(
        fileUri: Uri,
        folderName: String = "financial_record_payment_proofs",
        fileExtension: String = "jpg",
        onProgress: ((Float, String) -> Unit)? = null
    ): String {
        onProgress?.invoke(0.2f, "Mengunggah gambar ke Firebase Storage...")
        
        val filename = "${UUID.randomUUID()}_${System.currentTimeMillis()}.$fileExtension"
        val storageRef = FirebaseStorage.getInstance().reference.child("$folderName/$filename")
        
        val uploadTask = storageRef.putFile(fileUri)
        uploadTask.addOnProgressListener { taskSnapshot ->
            val bytesTransferred = taskSnapshot.bytesTransferred
            val totalBytes = taskSnapshot.totalByteCount
            if (totalBytes > 0) {
                val progress = 0.2f + (bytesTransferred.toFloat() / totalBytes.toFloat()) * 0.7f
                onProgress?.invoke(progress, "Mengunggah gambar (${(bytesTransferred / 1024)} KB / ${(totalBytes / 1024)} KB)...")
            }
        }.await()

        onProgress?.invoke(0.95f, "Mendapatkan link gambar...")
        val downloadUrl = storageRef.downloadUrl.await().toString()
        onProgress?.invoke(1.0f, "Upload berhasil!")
        
        return downloadUrl
    }
}
