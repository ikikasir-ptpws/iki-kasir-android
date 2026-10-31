package com.ptpws.ikikasir.feature.subscription.data.remote

import com.google.gson.annotations.SerializedName

data class SubscriptionApiResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SubscriptionDataDto? = null
)

data class SubscriptionDataDto(
    @SerializedName("isActive") val isActive: Boolean? = false,
    @SerializedName("canAccess") val canAccess: Boolean? = false,
    @SerializedName("status") val status: String? = "EXPIRED",
    @SerializedName("isExpired") val isExpired: Boolean? = true,
    @SerializedName("customerName") val customerName: String? = null,
    @SerializedName("businessName") val businessName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("packageName") val packageName: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null,
    @SerializedName("daysRemaining") val daysRemaining: Int? = 0,
    @SerializedName("message") val message: String? = null,
    @SerializedName("serverTime") val serverTime: String? = null,
    // Tambahan: field referral yang dikembalikan bersama data subscription
    @SerializedName("referralCode") val referralCode: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("id") val id: String? = null
)
