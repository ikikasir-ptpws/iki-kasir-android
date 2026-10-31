package com.ptpws.ikikasir.feature.referral.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response pembungkus umum API Referral Backend IKIKASIR
 */
data class ReferralApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

/**
 * Data ringkasan referral lengkap dari backend.
 *
 * Backend mengirim data dalam struktur datar (flat). Aplikasi Android lama
 * membaca data tersebut lewat object `referralInfo` + list `userReferrals`,
 * jadi keduanya tetap didukung di sini.
 */
data class ReferralOverviewResponseDto(
    // ── Struktur datar (backend terbaru) ──
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("referralCode") val referralCode: String? = null,
    @SerializedName("uplineName") val uplineName: String? = null,
    @SerializedName("uplineCode") val uplineCode: String? = null,
    @SerializedName("uplineBoundDate") val uplineBoundDate: String? = null,
    @SerializedName("isBound") val isBound: Boolean? = null,
    @SerializedName("availableBalance") val availableBalance: Double? = null,
    @SerializedName("totalEarnings") val totalEarnings: Double? = null,
    @SerializedName("totalWithdrawn") val totalWithdrawn: Double? = null,
    @SerializedName("rewardPerActiveUser") val rewardPerActiveUser: Double? = null,
    @SerializedName("referredUsers") val referredUsers: List<UserReferralDto>? = null,

    // ── Struktur object (aplikasi Android versi lama) ──
    @SerializedName("referralInfo") val referralInfo: ReferralInfoDto? = null,
    @SerializedName("userReferrals") val userReferrals: List<UserReferralDto>? = null,

    @SerializedName("withdrawals") val withdrawals: List<ReferralWithdrawalDto>? = null
) {
    /** Daftar downline: ambil dari `referredUsers`, fallback ke `userReferrals`. */
    fun resolveUserReferrals(): List<UserReferralDto> =
        referredUsers ?: userReferrals ?: emptyList()

    /** Ringkasan info referral: ambil dari struktur datar, fallback ke object lama. */
    fun resolveReferralInfo(): ReferralInfoDto {
        val info = referralInfo
        return ReferralInfoDto(
            referralCode = referralCode ?: info?.referralCode,
            uplineName = uplineName ?: info?.uplineName,
            uplineCode = uplineCode ?: info?.uplineCode,
            uplineBoundDate = uplineBoundDate ?: info?.uplineBoundDate,
            isBound = isBound ?: info?.isBound,
            availableBalance = availableBalance ?: info?.availableBalance,
            totalEarnings = totalEarnings ?: info?.totalEarnings,
            totalWithdrawn = totalWithdrawn ?: info?.totalWithdrawn,
            rewardPerActiveUser = rewardPerActiveUser ?: info?.rewardPerActiveUser
        )
    }
}

data class ReferralInfoDto(
    @SerializedName("referralCode") val referralCode: String? = null,
    @SerializedName("uplineName") val uplineName: String? = null,
    @SerializedName("uplineCode") val uplineCode: String? = null,
    @SerializedName("uplineBoundDate") val uplineBoundDate: String? = null,
    @SerializedName("isBound") val isBound: Boolean? = false,
    @SerializedName("availableBalance") val availableBalance: Double? = 0.0,
    @SerializedName("totalEarnings") val totalEarnings: Double? = 0.0,
    @SerializedName("totalWithdrawn") val totalWithdrawn: Double? = 0.0,
    @SerializedName("rewardPerActiveUser") val rewardPerActiveUser: Double? = 15000.0
)

data class UserReferralDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("storeName") val storeName: String? = null,
    @SerializedName("ownerName") val ownerName: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("registeredDate") val registeredDate: String? = null,
    @SerializedName("isActive") val isActive: Boolean? = false,
    @SerializedName("status") val status: String? = "Menunggu Verifikasi",
    @SerializedName("commissionEarned") val commissionEarned: Double? = 0.0
)

data class ReferralWithdrawalDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("amount") val amount: Double? = 0.0,
    @SerializedName("bankOrWallet") val bankOrWallet: String? = null,
    @SerializedName("accountNumber") val accountNumber: String? = null,
    @SerializedName("accountHolder") val accountHolder: String? = null,
    @SerializedName("requestDate") val requestDate: String? = null,
    @SerializedName("completedDate") val completedDate: String? = null,
    @SerializedName("status") val status: String? = "Menunggu Verifikasi finance",
    @SerializedName("notes") val notes: String? = null
)

/**
 * Request payload untuk mengajukan calon client baru via kode referral
 */
data class SubmitClientRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("businessName") val businessName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("referralCode") val referralCode: String? = null,
    @SerializedName("userEmail") val userEmail: String? = null,
    @SerializedName("customerId") val customerId: String? = null
)

/**
 * Response pengajuan calon client — memuat siapa pengundang (referrer)
 */
data class SubmitClientResponseDto(
    @SerializedName("referralCode") val referralCode: String? = null,
    @SerializedName("referredBy") val referredBy: ReferredByDto? = null
)

data class ReferredByDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("businessName") val businessName: String? = null,
    @SerializedName("referralCode") val referralCode: String? = null
)

/**
 * Request payload untuk mengajukan penarikan saldo komisi referral
 */
data class ReferralWithdrawRequestDto(
    @SerializedName("email") val email: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("amount") val amount: Double,
    @SerializedName("bankOrWallet") val bankOrWallet: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("accountHolder") val accountHolder: String,
    @SerializedName("notes") val notes: String? = null
)
