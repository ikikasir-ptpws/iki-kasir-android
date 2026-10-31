package com.ptpws.ikikasir.feature.referral.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ptpws.ikikasir.feature.referral.domain.model.ReferralInfo
import com.ptpws.ikikasir.feature.referral.domain.model.UserReferralItem
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalStatus
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalTransaction

/**
 * Cache lokal untuk data Program Referral.
 * TIDAK menggunakan seed data dummy — semua data harus berasal dari server backend.
 * Jika server tidak dapat dijangkau, tampilkan state kosong (belum ada data).
 */
class ReferralPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ReferralPreferences_v2", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_REFERRAL_INFO   = "key_referral_info"
        private const val KEY_USER_REFERRALS  = "key_user_referrals"
        private const val KEY_WITHDRAWALS     = "key_withdrawals"
        private const val KEY_LAST_SYNCED_EMAIL = "key_last_synced_email"
    }

    /**
     * Ambil info referral dari cache.
     * Mengembalikan ReferralInfo kosong jika belum pernah sync dari server.
     */
    fun getReferralInfo(): ReferralInfo {
        val json = prefs.getString(KEY_REFERRAL_INFO, null) ?: return ReferralInfo()
        return try {
            gson.fromJson(json, ReferralInfo::class.java) ?: ReferralInfo()
        } catch (e: Exception) {
            ReferralInfo()
        }
    }

    fun saveReferralInfo(info: ReferralInfo) {
        prefs.edit().putString(KEY_REFERRAL_INFO, gson.toJson(info)).apply()
    }

    fun getUserReferrals(): List<UserReferralItem> {
        val json = prefs.getString(KEY_USER_REFERRALS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<UserReferralItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveUserReferrals(list: List<UserReferralItem>) {
        prefs.edit().putString(KEY_USER_REFERRALS, gson.toJson(list)).apply()
    }

    fun getWithdrawals(): List<WithdrawalTransaction> {
        val json = prefs.getString(KEY_WITHDRAWALS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<WithdrawalTransaction>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveWithdrawals(list: List<WithdrawalTransaction>) {
        prefs.edit().putString(KEY_WITHDRAWALS, gson.toJson(list)).apply()
    }

    /**
     * Invalidasi cache jika email berubah (pengguna berbeda)
     */
    fun invalidateCacheIfEmailChanged(email: String) {
        val lastEmail = prefs.getString(KEY_LAST_SYNCED_EMAIL, "") ?: ""
        if (lastEmail.isNotBlank() && !lastEmail.equals(email.trim(), ignoreCase = true)) {
            clearAll()
        }
        prefs.edit().putString(KEY_LAST_SYNCED_EMAIL, email.trim()).apply()
    }

    fun clearAll() {
        prefs.edit()
            .remove(KEY_REFERRAL_INFO)
            .remove(KEY_USER_REFERRALS)
            .remove(KEY_WITHDRAWALS)
            .apply()
    }

    fun addWithdrawal(transaction: WithdrawalTransaction): Boolean {
        val currentInfo = getReferralInfo()
        if (currentInfo.availableBalance < transaction.amount) {
            return false
        }

        val updatedInfo = currentInfo.copy(
            availableBalance = currentInfo.availableBalance - transaction.amount,
            totalWithdrawn = currentInfo.totalWithdrawn + transaction.amount
        )
        saveReferralInfo(updatedInfo)

        val currentList = getWithdrawals().toMutableList()
        currentList.add(0, transaction)
        saveWithdrawals(currentList)
        return true
    }

    fun updateWithdrawalStatus(transactionId: String, newStatus: WithdrawalStatus): Boolean {
        val currentList = getWithdrawals().toMutableList()
        val index = currentList.indexOfFirst { it.id == transactionId }
        if (index != -1) {
            val oldItem = currentList[index]
            val completedTime = if (newStatus == WithdrawalStatus.SELESAI) {
                java.text.SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", java.util.Locale("id", "ID"))
                    .format(java.util.Date())
            } else oldItem.completedDate

            currentList[index] = oldItem.copy(
                status = newStatus,
                completedDate = completedTime
            )
            saveWithdrawals(currentList)
            return true
        }
        return false
    }

    fun updateUpline(uplineName: String, uplineCode: String) {
        val currentInfo = getReferralInfo()
        val updated = currentInfo.copy(
            uplineName = uplineName,
            uplineCode = uplineCode,
            isBound = true,
            uplineBoundDate = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id", "ID"))
                .format(java.util.Date())
        )
        saveReferralInfo(updated)
    }
}
