package com.ptpws.ikikasir.feature.subscription.data

data class SubscriptionCheckResult(
    val isActive: Boolean,
    val canAccess: Boolean,
    val status: String, // "ACTIVE", "EXPIRED", "SUSPENDED", "NOT_FOUND"
    val isExpired: Boolean,
    val customerName: String? = null,
    val businessName: String? = null,
    val email: String? = null,
    val packageName: String? = null,
    val expiresAt: String? = null,
    val expiresAtMillis: Long = 0L,
    val daysRemaining: Int = 0,
    val message: String = "",
    val serverTime: String? = null
)
