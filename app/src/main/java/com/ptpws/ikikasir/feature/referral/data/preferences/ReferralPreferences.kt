package com.ptpws.ikikasir.feature.referral.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ptpws.ikikasir.feature.referral.domain.model.ReferralInfo
import com.ptpws.ikikasir.feature.referral.domain.model.UserReferralItem
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalStatus
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalTransaction

class ReferralPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ReferralPreferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_REFERRAL_INFO = "key_referral_info"
        private const val KEY_USER_REFERRALS = "key_user_referrals"
        private const val KEY_WITHDRAWALS = "key_withdrawals"
        private const val KEY_IS_INITIALIZED = "key_is_initialized"
    }

    init {
        if (!prefs.getBoolean(KEY_IS_INITIALIZED, false)) {
            seedInitialData()
        }
    }

    private fun seedInitialData() {
        val initialInfo = ReferralInfo(
            referralCode = "IKIKASIR-MUH88",
            uplineName = "MUhib",
            uplineCode = "MUB-9921",
            uplineBoundDate = "10 September 2026",
            isBound = true,
            availableBalance = 1250000.0,
            totalEarnings = 2750000.0,
            totalWithdrawn = 1500000.0,
            rewardPerActiveUser = 15000.0
        )

        val initialUserReferrals = listOf(
            UserReferralItem(
                id = "REF-001",
                storeName = "Kopi Santai Kenangan",
                ownerName = "Budi Hartono",
                phoneNumber = "0812-3456-7890",
                registeredDate = "20 Okt 2026",
                isActive = true,
                status = "Aktif",
                commissionEarned = 50000.0
            ),
            UserReferralItem(
                id = "REF-002",
                storeName = "Warung Makan Berkah Abadi",
                ownerName = "Siti Rahma",
                phoneNumber = "0857-1122-3344",
                registeredDate = "18 Okt 2026",
                isActive = true,
                status = "Aktif",
                commissionEarned = 50000.0
            ),
            UserReferralItem(
                id = "REF-003",
                storeName = "Toko Kelontong Sentosa",
                ownerName = "Hendro Wibowo",
                phoneNumber = "0878-9988-7766",
                registeredDate = "15 Okt 2026",
                isActive = true,
                status = "Aktif",
                commissionEarned = 50000.0
            ),
            UserReferralItem(
                id = "REF-004",
                storeName = "Apotek Sehat Prima",
                ownerName = "dr. Amanda",
                phoneNumber = "0813-4455-6677",
                registeredDate = "12 Okt 2026",
                isActive = true,
                status = "Aktif",
                commissionEarned = 50000.0
            ),
            UserReferralItem(
                id = "REF-005",
                storeName = "Distro Cloth Nusantara",
                ownerName = "Dimas Anggara",
                phoneNumber = "0821-3322-1100",
                registeredDate = "08 Okt 2026",
                isActive = true,
                status = "Aktif",
                commissionEarned = 50000.0
            ),
            UserReferralItem(
                id = "REF-006",
                storeName = "Laundry Express Kilat",
                ownerName = "Nurul Fadilah",
                phoneNumber = "0896-7788-9900",
                registeredDate = "05 Okt 2026",
                isActive = false,
                status = "Menunggu Verifikasi",
                commissionEarned = 0.0
            ),
            UserReferralItem(
                id = "REF-007",
                storeName = "Bakso & Mie Ayam Podomoro",
                ownerName = "Agus Prasetyo",
                phoneNumber = "0819-0011-2233",
                registeredDate = "01 Okt 2026",
                isActive = true,
                status = "Aktif",
                commissionEarned = 50000.0
            ),
            UserReferralItem(
                id = "REF-008",
                storeName = "Kafe Senja Bahagia",
                ownerName = "Rian Saputra",
                phoneNumber = "0852-6677-8899",
                registeredDate = "28 Sep 2026",
                isActive = true,
                status = "Aktif",
                commissionEarned = 50000.0
            )
        )

        val initialWithdrawals = listOf(
            WithdrawalTransaction(
                id = "WD-20261022-003",
                amount = 250000.0,
                bankOrWallet = "BCA",
                accountNumber = "8230192841",
                accountHolder = "Super Admin",
                requestDate = "22 Okt 2026, 09:15 WIB",
                completedDate = null,
                status = WithdrawalStatus.MENUNGGU_VERIFIKASI,
                notes = "Pencairan komisi referral mingguan"
            ),
            WithdrawalTransaction(
                id = "WD-20261015-002",
                amount = 500000.0,
                bankOrWallet = "Mandiri",
                accountNumber = "1370018273641",
                accountHolder = "Super Admin",
                requestDate = "15 Okt 2026, 14:20 WIB",
                completedDate = "15 Okt 2026, 16:05 WIB",
                status = WithdrawalStatus.SELESAI,
                notes = "Tarik saldo referral tahap 2"
            ),
            WithdrawalTransaction(
                id = "WD-20261001-001",
                amount = 1000000.0,
                bankOrWallet = "BCA",
                accountNumber = "8230192841",
                accountHolder = "Super Admin",
                requestDate = "01 Okt 2026, 10:00 WIB",
                completedDate = "01 Okt 2026, 11:30 WIB",
                status = WithdrawalStatus.SELESAI,
                notes = "Pencairan saldo komisi pertama"
            )
        )

        saveReferralInfo(initialInfo)
        saveUserReferrals(initialUserReferrals)
        saveWithdrawals(initialWithdrawals)
        prefs.edit().putBoolean(KEY_IS_INITIALIZED, true).apply()
    }

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

    fun addWithdrawal(transaction: WithdrawalTransaction): Boolean {
        val currentInfo = getReferralInfo()
        if (currentInfo.availableBalance < transaction.amount) {
            return false
        }

        // Kurangi saldo tersedia, tambah total ditarik
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
