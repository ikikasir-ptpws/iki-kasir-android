package com.ptpws.ikikasir.feature.referral.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ptpws.ikikasir.feature.referral.domain.model.ReferralToken
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionApiConfig

/**
 * Penyimpanan token referral global pelanggan di perangkat (APK).
 *
 * Mirip pola [com.ptpws.ikikasir.feature.subscription.data.ExpiredSessionManager]:
 * data lisensi & referral disimpan di SharedPreferences sehingga tetap tersedia
 * setelah aplikasi ditutup / dijalankan ulang, dan tetap dipakai saat server offline.
 *
 * Token ini adalah "referral token" milik pelanggan yang sedang memakai lisensi ini:
 * berisi email, customerId, kode referral, nama toko, dan informasi upline.
 * Dipakai oleh:
 *   1. Halaman Program Referral (menampilkan kode & saldo referral milik sendiri)
 *   2. Form "Ajukan Calon Client" (otomatis terisi kode referral pengundang)
 */
object ReferralSessionManager {

    private const val PREF_NAME = "ikikasir_referral_session"

    private const val KEY_EMAIL = "email"
    private const val KEY_CUSTOMER_ID = "customer_id"
    private const val KEY_REFERRAL_CODE = "referral_code"
    private const val KEY_CUSTOMER_NAME = "customer_name"
    private const val KEY_BUSINESS_NAME = "business_name"
    private const val KEY_UPLINE_NAME = "upline_name"
    private const val KEY_UPLINE_CODE = "upline_code"
    private const val KEY_UPLINE_BOUND_DATE = "upline_bound_date"
    private const val KEY_IS_BOUND = "is_bound"
    private const val KEY_LAST_CHECKED = "last_checked"

    @Volatile
    private var prefs: SharedPreferences? = null

    /**
     * Inisialisasi penyimpanan. Dipanggil sekali dari [com.ptpws.ikikasir.BaseApplication].
     */
    fun init(context: Context) {
        if (prefs == null) {
            synchronized(this) {
                if (prefs == null) {
                    prefs = context.applicationContext
                        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                }
            }
        }
        applyToGlobalConfig()
    }

    /**
     * Simpan token referral pelanggan terbaru ke penyimpanan lokal.
     */
    fun saveToken(token: ReferralToken) {
        val store = prefs ?: return
        store.edit()
            .putString(KEY_EMAIL, token.email)
            .putString(KEY_CUSTOMER_ID, token.customerId)
            .putString(KEY_REFERRAL_CODE, token.referralCode)
            .putString(KEY_CUSTOMER_NAME, token.customerName)
            .putString(KEY_BUSINESS_NAME, token.businessName)
            .putString(KEY_UPLINE_NAME, token.uplineName)
            .putString(KEY_UPLINE_CODE, token.uplineCode)
            .putString(KEY_UPLINE_BOUND_DATE, token.uplineBoundDate)
            .putBoolean(KEY_IS_BOUND, token.isBound)
            .putLong(KEY_LAST_CHECKED, token.lastChecked)
            .apply()
        applyToGlobalConfig()
        Log.d(
            "ReferralSession",
            "Token referral disimpan: code=${token.referralCode}, customer=${token.customerId}"
        )
    }

    /**
     * Ambil token referral tersimpan (null bila belum pernah sync dari server).
     */
    fun getToken(): ReferralToken? {
        val store = prefs ?: return null
        if (!store.contains(KEY_REFERRAL_CODE)) return null

        val token = ReferralToken(
            email = store.getString(KEY_EMAIL, "") ?: "",
            customerId = store.getString(KEY_CUSTOMER_ID, "") ?: "",
            referralCode = store.getString(KEY_REFERRAL_CODE, "") ?: "",
            customerName = store.getString(KEY_CUSTOMER_NAME, "") ?: "",
            businessName = store.getString(KEY_BUSINESS_NAME, "") ?: "",
            uplineName = store.getString(KEY_UPLINE_NAME, "") ?: "",
            uplineCode = store.getString(KEY_UPLINE_CODE, "") ?: "",
            uplineBoundDate = store.getString(KEY_UPLINE_BOUND_DATE, "") ?: "",
            isBound = store.getBoolean(KEY_IS_BOUND, false),
            lastChecked = store.getLong(KEY_LAST_CHECKED, 0L)
        )
        return token.takeIf { it.referralCode.isNotBlank() }
    }

    /**
     * Hapus token referral (misal saat pelanggan ganti lisensi / logout).
     */
    fun clear() {
        prefs?.edit()?.clear()?.apply()
        SubscriptionApiConfig.REFERRAL_CODE = ""
        SubscriptionApiConfig.CUSTOMER_ID = ""
        SubscriptionApiConfig.CUSTOMER_NAME = ""
        SubscriptionApiConfig.BUSINESS_NAME = ""
        SubscriptionApiConfig.REFERRAL_UPLINE_NAME = ""
        SubscriptionApiConfig.REFERRAL_UPLINE_CODE = ""
    }

    /**
     * Salin token tersimpan ke variabel global [SubscriptionApiConfig]
     * sehingga seluruh fitur (referral, share, ajakan client) memakai data yang sama.
     */
    fun applyToGlobalConfig() {
        val token = getToken() ?: return
        if (token.email.isNotBlank()) {
            SubscriptionApiConfig.CLIENT_EMAIL = token.email
        }
        if (token.customerId.isNotBlank()) {
            SubscriptionApiConfig.CUSTOMER_ID = token.customerId
        }
        if (token.referralCode.isNotBlank()) {
            SubscriptionApiConfig.REFERRAL_CODE = token.referralCode
        }
        if (token.customerName.isNotBlank()) {
            SubscriptionApiConfig.CUSTOMER_NAME = token.customerName
        }
        if (token.businessName.isNotBlank()) {
            SubscriptionApiConfig.BUSINESS_NAME = token.businessName
        }
        if (token.uplineName.isNotBlank()) {
            SubscriptionApiConfig.REFERRAL_UPLINE_NAME = token.uplineName
        }
        if (token.uplineCode.isNotBlank()) {
            SubscriptionApiConfig.REFERRAL_UPLINE_CODE = token.uplineCode
        }
    }
}
