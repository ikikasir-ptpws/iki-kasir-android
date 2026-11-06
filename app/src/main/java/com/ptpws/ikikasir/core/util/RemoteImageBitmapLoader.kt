package com.ptpws.ikikasir.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.net.HttpURLConnection

object RemoteImageBitmapLoader {

    fun loadReceiptLogo(
        context: Context,
        remoteUrl: String,
        localPath: String,
        maxDimension: Int = 0
    ): Bitmap? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val capabilities = connectivityManager?.activeNetwork?.let(connectivityManager::getNetworkCapabilities)
        val isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        return if (isOnline) {
            load(context, remoteUrl, maxDimension) ?: load(context, localPath, maxDimension)
        } else {
            load(context, localPath, maxDimension)
        }
    }

    fun load(context: Context, imageUrl: String, maxDimension: Int = 0): Bitmap? {
        if (imageUrl.isBlank()) return null

        return try {
            val bitmap = when {
                imageUrl.startsWith("data:image") || imageUrl.startsWith("data:application") -> {
                    val encodedData = imageUrl.substringAfter("base64,")
                    val imageBytes = Base64.decode(encodedData, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                }
                imageUrl.startsWith("https://", ignoreCase = true) ||
                    imageUrl.startsWith("http://", ignoreCase = true) -> {
                    val connection = java.net.URL(imageUrl).openConnection() as HttpURLConnection
                    try {
                        connection.connectTimeout = 15_000
                        connection.readTimeout = 15_000
                        connection.instanceFollowRedirects = true
                        if (connection.responseCode !in 200..299) {
                            throw IllegalStateException("Gagal memuat logo (HTTP ${connection.responseCode})")
                        }
                        connection.inputStream.use(BitmapFactory::decodeStream)
                    } finally {
                        connection.disconnect()
                    }
                }
                else -> {
                    val uri = Uri.parse(imageUrl)
                    if (uri.scheme == null) {
                        BitmapFactory.decodeFile(uri.path ?: imageUrl)
                    } else {
                        context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
                    }
                }
            } ?: return null

            if (maxDimension > 0 && (bitmap.width > maxDimension || bitmap.height > maxDimension)) {
                val scale = minOf(
                    maxDimension.toFloat() / bitmap.width,
                    maxDimension.toFloat() / bitmap.height
                )
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                bitmap
            }
        } catch (error: Exception) {
            Log.w("RemoteImageBitmapLoader", "Gagal memuat gambar logo", error)
            null
        }
    }
}
