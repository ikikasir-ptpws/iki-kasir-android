package com.ptpws.ikikasir.feature.subscription.data

import android.util.Log
import com.google.gson.Gson
import com.ptpws.ikikasir.feature.subscription.data.remote.SubscriptionApiResponse
import com.ptpws.ikikasir.feature.subscription.data.remote.SubscriptionDataDto
import com.ptpws.ikikasir.feature.subscription.data.remote.SubscriptionRetrofitApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class SubscriptionApiService {

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(3500, TimeUnit.MILLISECONDS)
            .readTimeout(4000, TimeUnit.MILLISECONDS)
            .addInterceptor(logging)
            .build()
    }

    /**
     * Memeriksa status langganan menggunakan Retrofit
     */
    suspend fun checkSubscription(email: String): Result<SubscriptionCheckResult> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val candidateUrls = mutableListOf<String>().apply {
            add(SubscriptionApiConfig.BASE_URL)
            for (url in SubscriptionApiConfig.CANDIDATE_URLS) {
                if (!contains(url)) add(url)
            }
        }

        var lastException: Exception? = null

        for (baseUrl in candidateUrls) {
            try {
                val formattedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                Log.d("SubscriptionApi", "Mencoba fetch API via Retrofit ke: $formattedBaseUrl (email: $cleanEmail)")

                val retrofit = Retrofit.Builder()
                    .baseUrl(formattedBaseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(SubscriptionRetrofitApi::class.java)
                val response = api.checkSubscription(cleanEmail)

                Log.d("SubscriptionApi", "Retrofit Response Code: ${response.code()} dari $formattedBaseUrl")

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    val data = apiResponse.data ?: SubscriptionDataDto()

                    // Simpan URL yang berhasil terhubung sebagai BASE_URL aktif berikutnya
                    SubscriptionApiConfig.BASE_URL = baseUrl

                    val isActive = data.isActive ?: data.canAccess ?: false
                    val canAccess = data.canAccess ?: isActive
                    val status = data.status ?: if (isActive) "ACTIVE" else "EXPIRED"
                    val isExpired = data.isExpired ?: !isActive
                    val customerName = data.customerName?.takeIf { it.isNotBlank() }
                    val businessName = data.businessName?.takeIf { it.isNotBlank() }
                    val resEmail = data.email?.takeIf { it.isNotBlank() } ?: cleanEmail
                    val packageName = data.packageName?.takeIf { it.isNotBlank() }
                    val expiresAtStr = data.expiresAt?.takeIf { it.isNotBlank() }
                    val daysRemaining = data.daysRemaining ?: 0
                    val message = data.message?.takeIf { it.isNotBlank() }
                        ?: apiResponse.message?.takeIf { it.isNotBlank() }
                        ?: ""
                    val serverTime = data.serverTime?.takeIf { it.isNotBlank() }

                    val expiresAtMillis = expiresAtStr?.let {
                        parseIsoToMillis(it)
                    } ?: (System.currentTimeMillis() + (daysRemaining * 86400000L))

                    val result = SubscriptionCheckResult(
                        isActive = isActive,
                        canAccess = canAccess,
                        status = status,
                        isExpired = isExpired,
                        customerName = customerName,
                        businessName = businessName,
                        email = resEmail,
                        packageName = packageName,
                        expiresAt = expiresAtStr,
                        expiresAtMillis = expiresAtMillis,
                        daysRemaining = daysRemaining,
                        message = message,
                        serverTime = serverTime
                    )

                    return@withContext Result.success(result)
                } else {
                    // Response di luar 2xx (misal 404 NOT_FOUND atau 400)
                    val errorBodyStr = response.errorBody()?.string().orEmpty()
                    Log.w("SubscriptionApi", "Retrofit Error Body: $errorBodyStr")

                    val parsedError = try {
                        Gson().fromJson(errorBodyStr, SubscriptionApiResponse::class.java)
                    } catch (_: Exception) {
                        null
                    }

                    val data = parsedError?.data
                    val isActive = data?.isActive ?: false
                    val canAccess = data?.canAccess ?: false
                    val status = data?.status ?: "NOT_FOUND"
                    val message = data?.message ?: parsedError?.message ?: "Status langganan tidak aktif."

                    val result = SubscriptionCheckResult(
                        isActive = isActive,
                        canAccess = canAccess,
                        status = status,
                        isExpired = true,
                        customerName = data?.customerName,
                        businessName = data?.businessName,
                        email = data?.email ?: cleanEmail,
                        packageName = data?.packageName,
                        expiresAt = data?.expiresAt,
                        expiresAtMillis = data?.expiresAt?.let { parseIsoToMillis(it) } ?: 0L,
                        daysRemaining = data?.daysRemaining ?: 0,
                        message = message,
                        serverTime = data?.serverTime
                    )

                    return@withContext Result.success(result)
                }
            } catch (e: Exception) {
                Log.w("SubscriptionApi", "Kandidat $baseUrl via Retrofit gagal: ${e.message}")
                lastException = e
            }
        }

        Log.e("SubscriptionApi", "Semua kandidat server Retrofit gagal terhubung: ${lastException?.message}", lastException)
        Result.failure(lastException ?: Exception("Gagal terhubung ke server backend IKIKASIR via Retrofit."))
    }

    private fun parseIsoToMillis(isoString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            format.parse(isoString)?.time ?: 0L
        } catch (_: Exception) {
            try {
                val formatNoMs = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                formatNoMs.parse(isoString)?.time ?: 0L
            } catch (_: Exception) {
                0L
            }
        }
    }
}
