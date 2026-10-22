package com.ptpws.ikikasir.feature.referral.domain.model

enum class WithdrawalStatus(val label: String) {
    MENUNGGU_VERIFIKASI("Menunggu Verifikasi finance"),
    SELESAI("Selesai"),
    DITOLAK("Ditolak")
}

data class ReferralInfo(
    val referralCode: String = "IKIKASIR-MUH88",
    val uplineName: String = "MUhib",
    val uplineCode: String = "MUB-9921",
    val uplineBoundDate: String = "10 September 2026",
    val isBound: Boolean = true,
    val availableBalance: Double = 1250000.0,
    val totalEarnings: Double = 2750000.0,
    val totalWithdrawn: Double = 1500000.0,
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
