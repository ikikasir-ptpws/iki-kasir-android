package com.ptpws.ikikasir.feature.referral.presentation.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.referral.data.preferences.ReferralPreferences
import com.ptpws.ikikasir.feature.referral.domain.model.ReferralInfo
import com.ptpws.ikikasir.feature.referral.domain.model.UserReferralItem
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalStatus
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ReferralUiState(
    val referralInfo: ReferralInfo = ReferralInfo(),
    val userReferrals: List<UserReferralItem> = emptyList(),
    val withdrawals: List<WithdrawalTransaction> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Semua",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ReferralViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val preferences = ReferralPreferences(context)

    private val _referralInfo = MutableStateFlow(preferences.getReferralInfo())
    val referralInfo: StateFlow<ReferralInfo> = _referralInfo.asStateFlow()

    private val _userReferrals = MutableStateFlow(preferences.getUserReferrals())
    val userReferrals: StateFlow<List<UserReferralItem>> = _userReferrals.asStateFlow()

    private val _withdrawals = MutableStateFlow(preferences.getWithdrawals())
    val withdrawals: StateFlow<List<WithdrawalTransaction>> = _withdrawals.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Semua")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val filteredUserReferrals: StateFlow<List<UserReferralItem>> =
        combine(_userReferrals, _searchQuery, _selectedFilter) { list, query, filter ->
            list.filter { item ->
                val matchesQuery = query.isBlank() ||
                        item.storeName.contains(query, ignoreCase = true) ||
                        item.ownerName.contains(query, ignoreCase = true) ||
                        item.phoneNumber.contains(query, ignoreCase = true)

                val matchesFilter = when (filter) {
                    "Aktif" -> item.isActive
                    "Menunggu" -> !item.isActive
                    else -> true
                }

                matchesQuery && matchesFilter
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshData() {
        _referralInfo.value = preferences.getReferralInfo()
        _userReferrals.value = preferences.getUserReferrals()
        _withdrawals.value = preferences.getWithdrawals()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun copyReferralCode(ctx: Context) {
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Kode Referral IKIKASIR", _referralInfo.value.referralCode)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(ctx, "Kode referral ${_referralInfo.value.referralCode} berhasil disalin!", Toast.LENGTH_SHORT).show()
    }

    fun shareReferral(ctx: Context) {
        val code = _referralInfo.value.referralCode
        val shareText = """
            Yuk gabung dan kembangkan bisnismu menggunakan aplikasi kasir modern IKIKASIR! 🚀
            
            Gunakan kode referral saya saat mendaftar:
            👉 *$code*
            
            Dapatkan kemudahan transaksi POS, kelola stok otomatis, dan laporan keuangan lengkap dalam satu aplikasi.
            Unduh sekarang di Google Play Store!
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Kode Referral")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ctx.startActivity(shareIntent)
    }

    fun submitWithdrawal(
        amount: Double,
        bankOrWallet: String,
        accountNumber: String,
        accountHolder: String,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentInfo = _referralInfo.value
        if (amount <= 0.0) {
            onError("Nominal penarikan harus lebih dari Rp 0")
            return
        }
        if (amount > currentInfo.availableBalance) {
            onError("Saldo tidak mencukupi untuk melakukan penarikan ini")
            return
        }
        if (bankOrWallet.isBlank()) {
            onError("Silakan pilih bank atau e-wallet tujuan")
            return
        }
        if (accountNumber.isBlank() || accountHolder.isBlank()) {
            onError("Nomor rekening dan nama pemilik rekening wajib diisi")
            return
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID"))
        val dateId = SimpleDateFormat("yyyyMMdd", Locale("id", "ID")).format(Date())
        val randomSuffix = (100..999).random()
        val newTxId = "WD-$dateId-$randomSuffix"

        val transaction = WithdrawalTransaction(
            id = newTxId,
            amount = amount,
            bankOrWallet = bankOrWallet,
            accountNumber = accountNumber,
            accountHolder = accountHolder,
            requestDate = dateFormat.format(Date()),
            completedDate = null,
            status = WithdrawalStatus.MENUNGGU_VERIFIKASI,
            notes = notes.ifBlank { "Penarikan saldo komisi referral" }
        )

        val success = preferences.addWithdrawal(transaction)
        if (success) {
            refreshData()
            onSuccess()
        } else {
            onError("Gagal memproses penarikan saldo, periksa kembali saldo Anda")
        }
    }

    /**
     * Memungkinkan simulasi perubahan status dari "Menunggu Verifikasi finance" menjadi "Selesai"
     * untuk memverifikasi alur lengkap pada mind map: Tarik Saldo -> Menunggu Verifikasi finance -> Selesai
     */
    fun simulateApproveWithdrawal(transactionId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            preferences.updateWithdrawalStatus(transactionId, WithdrawalStatus.SELESAI)
            refreshData()
            onDone()
        }
    }

    fun updateUpline(uplineName: String, uplineCode: String) {
        preferences.updateUpline(uplineName, uplineCode)
        refreshData()
    }
}
