package com.ptpws.ikikasir.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

object ImageCompressorHelper {

    const val MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024L // 2MB

    /**
     * Membuat file temporer dan Content Uri via FileProvider untuk kamera.
     */
    fun createCameraTempFileUri(context: Context): Pair<File, Uri> {
        val storageDir = File(context.cacheDir, "camera_captures")
        if (!storageDir.exists()) storageDir.mkdirs()
        val tempFile = File(storageDir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        return Pair(tempFile, uri)
    }

    /**
     * Format ukuran byte menjadi format yang mudah dibaca (KB / MB).
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val df = DecimalFormat("#.##")
        return when {
            bytes >= 1024 * 1024 -> "${df.format(bytes.toDouble() / (1024 * 1024))} MB"
            bytes >= 1024 -> "${df.format(bytes.toDouble() / 1024)} KB"
            else -> "$bytes B"
        }
    }

    /**
     * Mengambil ukuran file sebenarnya dari Uri (mendukung content:// dan file://).
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            if (uri.scheme == "file") {
                uri.path?.let { File(it).length() } ?: 0L
            } else {
                context.contentResolver.openFileDescriptor(uri, "r")?.use {
                    it.statSize
                } ?: run {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.available().toLong()
                    } ?: 0L
                }
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Memproses gambar dari Uri:
     * - Jika ukuran <= maxBytes (2MB): disalin ke penyimpanan internal.
     * - Jika ukuran > maxBytes (2MB): secara otomatis dikompresi (downscale resolusi & kualitas JPEG)
     *   hingga ukuran file dijamin <= 2MB.
     *
     * @param context Android Context
     * @param sourceUri Uri gambar asal (dari galeri atau kamera)
     * @param targetSubfolder Subfolder di dalam context.filesDir (misal "payment_proofs", "product_images")
     * @param maxBytes Batas maksimal ukuran file (default 2MB)
     * @param onProgress Callback untuk update animasi progress upload/kompresi (progress 0.0f..1.0f, pesan)
     * @return File hasil akhir di penyimpanan internal yang dijamin <= maxBytes
     */
    suspend fun processAndCompressImage(
        context: Context,
        sourceUri: Uri,
        targetSubfolder: String = "payment_proofs",
        maxBytes: Long = MAX_IMAGE_SIZE_BYTES,
        onProgress: ((Float, String) -> Unit)? = null
    ): File = withContext(Dispatchers.IO) {
        onProgress?.invoke(0.15f, "Memeriksa ukuran gambar...")
        delay(80)

        val targetDir = File(context.filesDir, targetSubfolder)
        if (!targetDir.exists()) targetDir.mkdirs()
        val destinationFile = File(targetDir, "img_${System.currentTimeMillis()}.jpg")

        val initialSize = getFileSize(context, sourceUri)

        // JIKA UKURAN SUDAH <= 2MB DAN TIDAK MELEBIHI BATAS
        if (initialSize in 1..maxBytes) {
            onProgress?.invoke(0.55f, "Menyimpan gambar (${formatFileSize(initialSize)})...")
            delay(100)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Tidak dapat membaca data gambar")

            onProgress?.invoke(1.0f, "Gambar berhasil disimpan!")
            delay(80)
            return@withContext destinationFile
        }

        // JIKA UKURAN LEBIH DARI 2MB: LAKUKAN KOMPRESI OTOMATIS
        onProgress?.invoke(0.3f, "Ukuran gambar ${formatFileSize(initialSize)} (>2MB). Mengompresi otomatis...")
        delay(120)

        // 1. Baca dimensi gambar awal terlebih dahulu tanpa memuat bitmap penuh ke memori
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(sourceUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        val origWidth = options.outWidth
        val origHeight = options.outHeight

        // 2. Hitung inSampleSize untuk menghindari OutOfMemory (target dimensi max 2048px)
        var sampleSize = 1
        val maxDimension = 2048
        if (origWidth > maxDimension || origHeight > maxDimension) {
            val halfHeight = origHeight / 2
            val halfWidth = origWidth / 2
            while ((halfHeight / sampleSize) >= maxDimension && (halfWidth / sampleSize) >= maxDimension) {
                sampleSize *= 2
            }
        }

        // 3. Decode bitmap asli dengan sampleSize yang aman
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        var bitmap = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: throw IllegalStateException("Gagal memproses bitmap gambar")

        onProgress?.invoke(0.6f, "Mengoptimalkan kualitas dan ukuran gambar...")
        delay(100)

        // 4. Kompresi kualitas JPEG bertahap hingga ukuran <= maxBytes (2MB)
        val qualities = intArrayOf(85, 75, 65, 50, 40, 30)
        var compressedBytes: ByteArray? = null

        for (q in qualities) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, q, baos)
            val bytes = baos.toByteArray()
            if (bytes.size <= maxBytes) {
                compressedBytes = bytes
                break
            }
        }

        // 5. Jika kualitas terkecil masih > 2MB, lakukan scaling dimensi dan kompres ulang
        if (compressedBytes == null || compressedBytes.size > maxBytes) {
            onProgress?.invoke(0.75f, "Menyesuaikan resolusi gambar...")
            var currentScale = 0.8f
            while (currentScale > 0.3f) {
                val newW = (bitmap.width * currentScale).toInt().coerceAtLeast(400)
                val newH = (bitmap.height * currentScale).toInt().coerceAtLeast(400)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, true)

                val baos = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 65, baos)
                val bytes = baos.toByteArray()
                if (bytes.size <= maxBytes) {
                    compressedBytes = bytes
                    break
                }
                currentScale -= 0.15f
            }
        }

        // Simpan hasil kompresi ke file tujuan
        val finalBytes = compressedBytes ?: run {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos)
            baos.toByteArray()
        }

        onProgress?.invoke(0.9f, "Menyimpan file hasil kompresi (${formatFileSize(finalBytes.size.toLong())})...")
        delay(100)

        FileOutputStream(destinationFile).use { fos ->
            fos.write(finalBytes)
        }

        onProgress?.invoke(1.0f, "Kompresi berhasil (${formatFileSize(finalBytes.size.toLong())})!")
        delay(120)

        return@withContext destinationFile
    }
}
