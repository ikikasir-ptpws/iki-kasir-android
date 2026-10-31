package com.ptpws.ikikasir.feature.referral.data

import android.util.Log
import com.google.gson.Gson
import com.ptpws.ikikasir.feature.referral.data.preferences.ReferralSessionManager
import com.ptpws.ikikasir.feature.referral.data.remote.ReferralApiResponse
import com.ptpws.ikikasir.feature.referral.data.remote.ReferralOverviewResponseDto
import com.ptpws.ikikasir.feature.referral.data.remote.ReferralRetrofitApi
import com.ptpws.ikikasir.feature.referral.data.remote.ReferralWithdrawRequestDto
import com.ptpws.ikikasir.feature.referral.data.remote.SubmitClientRequestDto
import com.ptpws.ikikasir.feature.referral.domain.model.ReferralInfo
import com.ptpws.ikikasir.feature.referral.domain.model.ReferralToken
import com.ptpws.ikikasir.feature.referral.domain.model.UserReferralItem
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalStatus
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalTransaction
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

data class ReferralOverviewResult(
    val referralInfo: ReferralInfo,
    val userReferrals: List<UserReferralItem>,
    val withdrawals: List<WithdrawalTransaction>
)

class ReferralApiService {

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

    private fun getCandidateUrls(): List<String> {
        return mutableListOf<String>().apply {
            add(SubscriptionApiConfig.BASE_URL)
            for (url in SubscriptionApiConfig.CANDIDATE_URLS) {
                if (!contains(url)) add(url)
            }
        }
    }

    /**
     * Mengambil ringkasan referral live dari backend (overview, downlines, withdrawals)
     */
    suspend fun getReferralOverview(email: String): Result<ReferralOverviewResult> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        var lastException: Exception? = null

        for (baseUrl in getCandidateUrls()) {
            try {
                val formattedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                Log.d("ReferralApi", "Mencoba fetch Overview ke: $formattedBaseUrl (email: $cleanEmail)")

                val retrofit = Retrofit.Builder()
                    .baseUrl(formattedBaseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(ReferralRetrofitApi::class.java)
                val response = api.getReferralOverview(cleanEmail)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val data = body.data ?: ReferralOverviewResponseDto()

                    SubscriptionApiConfig.BASE_URL = baseUrl

                    val infoDto = data.resolveReferralInfo()
                    val info = ReferralInfo(
                        referralCode = infoDto.referralCode.orEmpty(),
                        uplineName = infoDto.uplineName.orEmpty(),
                        uplineCode = infoDto.uplineCode.orEmpty(),
                        uplineBoundDate = infoDto.uplineBoundDate.orEmpty(),
                        isBound = infoDto.isBound ?: false,
                        availableBalance = infoDto.availableBalance ?: 0.0,
                        totalEarnings = infoDto.totalEarnings ?: 0.0,
                        totalWithdrawn = infoDto.totalWithdrawn ?: 0.0,
                        rewardPerActiveUser = infoDto.rewardPerActiveUser ?: 15000.0
                    )

                    val userReferrals = data.resolveUserReferrals().map { item ->
                        UserReferralItem(
                            id = item.id.ifBlank { "REF-${System.currentTimeMillis()}" },
                            storeName = item.storeName ?: "-",
                            ownerName = item.ownerName ?: "-",
                            phoneNumber = item.phoneNumber ?: "-",
                            registeredDate = item.registeredDate ?: "-",
                            isActive = item.isActive ?: false,
                            status = item.status ?: if (item.isActive == true) "Aktif" else "Menunggu Verifikasi",
                            commissionEarned = item.commissionEarned ?: 0.0
                        )
                    }

                    val withdrawals = (data.withdrawals ?: emptyList()).map { tx ->
                        val statusEnum = when {
                            tx.status.equals("Selesai", ignoreCase = true) || tx.status.equals("COMPLETED", ignoreCase = true) -> WithdrawalStatus.SELESAI
                            tx.status.equals("Ditolak", ignoreCase = true) || tx.status.equals("REJECTED", ignoreCase = true) -> WithdrawalStatus.DITOLAK
                            else -> WithdrawalStatus.MENUNGGU_VERIFIKASI
                        }
                        WithdrawalTransaction(
                            id = tx.id.ifBlank { "WD-${System.currentTimeMillis()}" },
                            amount = tx.amount ?: 0.0,
                            bankOrWallet = tx.bankOrWallet ?: "BCA",
                            accountNumber = tx.accountNumber ?: "-",
                            accountHolder = tx.accountHolder ?: "-",
                            requestDate = tx.requestDate ?: "-",
                            completedDate = tx.completedDate,
                            status = statusEnum,
                            notes = tx.notes ?: ""
                        )
                    }

                    // Simpan token referral global pelanggan ke penyimpanan lokal
                    // (kode referral sendiri + identitas pengundang/upline)
                    ReferralSessionManager.saveToken(
                        ReferralToken(
                            email = cleanEmail,
                            customerId = data.customerId.orEmpty(),
                            referralCode = info.referralCode,
                            customerName = SubscriptionApiConfig.CUSTOMER_NAME,
                            businessName = SubscriptionApiConfig.BUSINESS_NAME,
                            uplineName = info.uplineName,
                            uplineCode = info.uplineCode,
                            uplineBoundDate = info.uplineBoundDate,
                            isBound = info.isBound,
                            lastChecked = System.currentTimeMillis()
                        )
                    )

                    return@withContext Result.success(
                        ReferralOverviewResult(
                            referralInfo = info,
                            userReferrals = userReferrals,
                            withdrawals = withdrawals
                        )
                    )
                } else {
                    val err = response.errorBody()?.string().orEmpty()
                    Log.w("ReferralApi", "Fetch overview failed HTTP ${response.code()}: $err")
                }
            } catch (e: Exception) {
                Log.w("ReferralApi", "Gagal konek overview ke $baseUrl: ${e.message}")
                lastException = e
            }
        }

        Result.failure(lastException ?: Exception("Gagal terhubung ke backend Manajemen IKIKASIR."))
    }

    /**
     * Mengajukan calon client baru langsung terikat ke kode referral
     *
     * Token referral global (kode referral, email lisensi, dan ID pelanggan) ikut
     * dikirim supaya backend dapat mendeteksi siapa pengundangnya secara otomatis.
     */
    suspend fun submitClient(
        name: String,
        businessName: String,
        phone: String,
        email: String,
        notes: String,
        referralCode: String?,
        userEmail: String?,
        customerId: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        val req = SubmitClientRequestDto(
            name = name.trim(),
            businessName = businessName.trim(),
            phone = phone.trim(),
            email = email.trim(),
            notes = notes.trim(),
            referralCode = referralCode?.trim()?.takeIf { it.isNotBlank() },
            userEmail = userEmail?.trim()?.takeIf { it.isNotBlank() },
            customerId = customerId?.trim()?.takeIf { it.isNotBlank() }
        )

        for (baseUrl in getCandidateUrls()) {
            try {
                val formattedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                val retrofit = Retrofit.Builder()
                    .baseUrl(formattedBaseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(ReferralRetrofitApi::class.java)
                val response = api.submitClient(req)

                if (response.isSuccessful && response.body() != null) {
                    SubscriptionApiConfig.BASE_URL = baseUrl
                    val msg = response.body()?.message ?: "Calon client berhasil diajukan!"
                    return@withContext Result.success(msg)
                } else {
                    val errBody = response.errorBody()?.string().orEmpty()
                    val parsed = try {
                        Gson().fromJson(errBody, ReferralApiResponse::class.java)
                    } catch (_: Exception) { null }
                    val errMsg = parsed?.message ?: "Gagal mengajukan calon client (HTTP ${response.code()})"
                    return@withContext Result.failure(Exception(errMsg))
                }
            } catch (e: Exception) {
                Log.w("ReferralApi", "Gagal submit client ke $baseUrl: ${e.message}")
                lastException = e
            }
        }

        Result.failure(lastException ?: Exception("Gagal terhubung ke server saat mengajukan client."))
    }

    /**
     * Mengajukan permohonan penarikan saldo komisi referral ke backend
     */
    suspend fun requestWithdrawal(
        email: String,
        amount: Double,
        bankOrWallet: String,
        accountNumber: String,
        accountHolder: String,
        notes: String
    ): Result<String> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        val req = ReferralWithdrawRequestDto(
            email = email.trim(),
            amount = amount,
            bankOrWallet = bankOrWallet.trim(),
            accountNumber = accountNumber.trim(),
            accountHolder = accountHolder.trim(),
            notes = notes.trim()
        )

        for (baseUrl in getCandidateUrls()) {
            try {
                val formattedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                val retrofit = Retrofit.Builder()
                    .baseUrl(formattedBaseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(ReferralRetrofitApi::class.java)
                val response = api.requestWithdrawal(req)

                if (response.isSuccessful && response.body() != null) {
                    SubscriptionApiConfig.BASE_URL = baseUrl
                    val msg = response.body()?.message ?: "Permintaan penarikan berhasil diajukan!"
                    return@withContext Result.success(msg)
                } else {
                    val errBody = response.errorBody()?.string().orEmpty()
                    val parsed = try {
                        Gson().fromJson(errBody, ReferralApiResponse::class.java)
                    } catch (_: Exception) { null }
                    val errMsg = parsed?.message ?: "Gagal memproses penarikan saldo (HTTP ${response.code()})"
                    return@withContext Result.failure(Exception(errMsg))
                }
            } catch (e: Exception) {
                Log.w("ReferralApi", "Gagal request withdrawal ke $baseUrl: ${e.message}")
                lastException = e
            }
        }

        Result.failure(lastException ?: Exception("Gagal terhubung ke server saat penarikan saldo."))
    }
}
