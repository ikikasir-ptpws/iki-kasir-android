package com.ptpws.ikikasir.feature.referral.domain.model

enum class WithdrawalStatus(val label: String) {
    MENUNGGU_VERIFIKASI("Menunggu Verifikasi finance"),
    SELESAI("Selesai"),
    DITOLAK("Ditolak")
}

data class ReferralInfo(
    val referralCode: String = "",
    val uplineName: String = "",
    val uplineCode: String = "",
    val uplineBoundDate: String = "",
    val isBound: Boolean = false,
    val availableBalance: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val rewardPerActiveUser: Double = 15000.0
)

data class UserReferralItem(
    val id: String,
    val storeName: String,
    val ownerName: String,
    val phoneNumber: String,
    val registeredDate: String,
    val isActive: Boolean = true,
    val status: String = "Aktif",
    val commissionEarned: Double = 50000.0
)

data class WithdrawalTransaction(
    val id: String,
    val amount: Double,
    val bankOrWallet: String,
    val accountNumber: String,
    val accountHolder: String,
    val requestDate: String,
    val completedDate: String? = null,
    val status: WithdrawalStatus = WithdrawalStatus.MENUNGGU_VERIFIKASI,
    val notes: String = ""
)

/**
 * Token referral global pelanggan.
 *
 * Disimpan lokal (SharedPreferences) supaya tetap tersedia walaupun aplikasi
 * dijalankan ulang / sedang offline. Token ini dipakai untuk:
 *   1. Menampilkan kode referral milik pelanggan di halaman Program Referral
 *   2. Otomatis mengikat calon client yang diundang dari aplikasi
 */
data class ReferralToken(
    val email: String = "",
    val customerId: String = "",
    val referralCode: String = "",
    val customerName: String = "",
    val businessName: String = "",
    val uplineName: String = "",
    val uplineCode: String = "",
    val uplineBoundDate: String = "",
    val isBound: Boolean = false,
    val lastChecked: Long = 0L
) {
    /** True bila token sudah possesses data referralCode yang valid */
    val isValid: Boolean
        get() = referralCode.isNotBlank() && referralCode.startsWith("IKIKASIR-")
}
