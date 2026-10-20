package com.ptpws.ikikasir.feature.subscription.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class ExpiredSessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ikikasir_subscription_session"
        private const val KEY_HAS_SESSION = "has_session"
        private const val KEY_IS_ACTIVE = "is_active"
        private const val KEY_EXPIRES_AT_MILLIS = "expires_at_millis"
        private const val KEY_EXPIRES_AT_STR = "expires_at_str"
        private const val KEY_PACKAGE_NAME = "package_name"
        private const val KEY_BUSINESS_NAME = "business_name"
        private const val KEY_CUSTOMER_NAME = "customer_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_STATUS = "status"
        private const val KEY_MESSAGE = "message"
        private const val KEY_LAST_CHECKED = "last_checked"
    }

    /**
     * Diagram Node: "Apakah ada data di session cache expired ?"
     * Mengembalikan true jika session cache sudah pernah tersimpan
     */
    fun hasExpiredSession(email: String? = null): Boolean {
        val hasSession = prefs.getBoolean(KEY_HAS_SESSION, false)
        if (!hasSession) return false

        // Jika email diberikan, validasi apakah cache cocok dengan email pengguna yang sedang login
        if (!email.isNullOrBlank()) {
            val cachedEmail = prefs.getString(KEY_EMAIL, "") ?: ""
            if (cachedEmail.isNotBlank() && !cachedEmail.equals(email.trim(), ignoreCase = true)) {
                Log.d("ExpiredSession", "Cache email ($cachedEmail) tidak cocok dengan $email. Dianggap belum ada cache.")
                return false
            }
        }

        return true
    }

    /**
     * Diagram Node: "Apakah session data masih aktif ?"
     * Memeriksa apakah status di cache aktif DAN waktu sekarang belum melewati tanggal kedaluwarsa
     */
    fun isSessionDataActive(email: String? = null): Boolean {
        if (!hasExpiredSession(email)) return false

        val isActive = prefs.getBoolean(KEY_IS_ACTIVE, false)
        val expiresAtMillis = prefs.getLong(KEY_EXPIRES_AT_MILLIS, 0L)
        val now = System.currentTimeMillis()

        val isTimeValid = expiresAtMillis > 0L && now <= expiresAtMillis
        val result = isActive && isTimeValid

        Log.d("ExpiredSession", "Cek session aktif: isActive=$isActive, expiresAtMillis=$expiresAtMillis, now=$now, masihAktif=$result")
        return result
    }

    /**
     * Diagram Node: "Simpan di Session"
     * Menyimpan hasil respon server ke local cache Android
     */
    fun saveSession(data: SubscriptionCheckResult) {
        prefs.edit().apply {
            putBoolean(KEY_HAS_SESSION, true)
            putBoolean(KEY_IS_ACTIVE, data.isActive)
            putLong(KEY_EXPIRES_AT_MILLIS, data.expiresAtMillis)
            putString(KEY_EXPIRES_AT_STR, data.expiresAt.orEmpty())
            putString(KEY_PACKAGE_NAME, data.packageName.orEmpty())
            putString(KEY_BUSINESS_NAME, data.businessName.orEmpty())
            putString(KEY_CUSTOMER_NAME, data.customerName.orEmpty())
            putString(KEY_EMAIL, data.email.orEmpty())
            putString(KEY_STATUS, data.status)
            putString(KEY_MESSAGE, data.message)
            putLong(KEY_LAST_CHECKED, System.currentTimeMillis())
            apply()
        }
        Log.d("ExpiredSession", "Data expired berhasil disimpan di session cache: email=${data.email}, isActive=${data.isActive}")
    }

    /**
     * Mengambil data session cache yang tersimpan
     */
    fun getCachedSession(): SubscriptionCheckResult? {
        if (!prefs.getBoolean(KEY_HAS_SESSION, false)) return null

        val isActive = prefs.getBoolean(KEY_IS_ACTIVE, false)
        val expiresAtMillis = prefs.getLong(KEY_EXPIRES_AT_MILLIS, 0L)
        val now = System.currentTimeMillis()
        val isTimeValid = expiresAtMillis > 0L && now <= expiresAtMillis

        val daysRemaining = if (expiresAtMillis > now) {
            ((expiresAtMillis - now) / (1000 * 60 * 60 * 24)).toInt()
        } else 0

        val customerName = prefs.getString(KEY_CUSTOMER_NAME, null)?.takeIf { it.isNotBlank() }
        val businessName = prefs.getString(KEY_BUSINESS_NAME, null)?.takeIf { it.isNotBlank() }
        val packageName = prefs.getString(KEY_PACKAGE_NAME, null)?.takeIf { it.isNotBlank() }
        val status = prefs.getString(KEY_STATUS, if (isActive && isTimeValid) "ACTIVE" else "EXPIRED") ?: "EXPIRED"

        return SubscriptionCheckResult(
            isActive = isActive && isTimeValid,
            canAccess = isActive && isTimeValid,
            status = status,
            isExpired = !isTimeValid || !isActive,
            customerName = customerName,
            businessName = businessName,
            email = prefs.getString(KEY_EMAIL, null),
            packageName = packageName,
            expiresAt = prefs.getString(KEY_EXPIRES_AT_STR, null),
            expiresAtMillis = expiresAtMillis,
            daysRemaining = daysRemaining,
            message = prefs.getString(KEY_MESSAGE, "") ?: ""
        )
    }

    /**
     * Menghapus cache session (misal saat logout atau saat pengguna menekan tombol "Cek Status Lagi")
     */
    fun clearSession() {
        prefs.edit().clear().apply()
        Log.d("ExpiredSession", "Session cache expired telah dibersihkan.")
    }
}
