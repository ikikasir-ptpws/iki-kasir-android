package com.ptpws.ikikasir.feature.referral.presentation.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.referral.data.ReferralApiService
import com.ptpws.ikikasir.feature.referral.data.preferences.ReferralPreferences
import com.ptpws.ikikasir.feature.referral.data.preferences.ReferralSessionManager
import com.ptpws.ikikasir.feature.referral.domain.model.ReferralInfo
import com.ptpws.ikikasir.feature.referral.domain.model.UserReferralItem
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalStatus
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalTransaction
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionApiConfig
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
    private val apiService = ReferralApiService()

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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

    init {
        hydrateFromSession()
        loadReferralDataFromServer()
    }

    /**
     * Memuat token referral global dari penyimpanan lokal saat ViewModel dibuat.
     * Membuat kode referral milik sendiri tetap tampil walau server sedang offline.
     */
    private fun hydrateFromSession() {
        val token = ReferralSessionManager.getToken() ?: return
        ReferralSessionManager.applyToGlobalConfig()

        val current = _referralInfo.value
        if (current.referralCode.isBlank() && token.referralCode.isNotBlank()) {
            _referralInfo.value = current.copy(
                referralCode = token.referralCode,
                uplineName = token.uplineName,
                uplineCode = token.uplineCode,
                uplineBoundDate = token.uplineBoundDate,
                isBound = token.isBound
            )
            Log.d("ReferralViewModel", "Token referral dimuat dari session: ${token.referralCode}")
        }
    }

    /**
     * Memuat data ringkasan referral langsung dari Backend Manajemen IKIKASIR.
     * Menggunakan CLIENT_EMAIL dari SubscriptionApiConfig (global token).
     * Fallback: empty state (bukan data dummy) jika server tidak dapat dijangkau.
     */
    fun loadReferralDataFromServer(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val email = SubscriptionApiConfig.CLIENT_EMAIL

            // Invalidasi cache jika email berbeda (ganti pengguna)
            preferences.invalidateCacheIfEmailChanged(email)

            val result = apiService.getReferralOverview(email)

            result.onSuccess { overview ->
                _referralInfo.value = overview.referralInfo
                _userReferrals.value = overview.userReferrals
                _withdrawals.value = overview.withdrawals

                // Simpan ke cache lokal
                preferences.saveReferralInfo(overview.referralInfo)
                preferences.saveUserReferrals(overview.userReferrals)
                preferences.saveWithdrawals(overview.withdrawals)

                // Perbarui REFERRAL_CODE global dari data server
                if (overview.referralInfo.referralCode.isNotBlank()) {
                    SubscriptionApiConfig.REFERRAL_CODE = overview.referralInfo.referralCode
                }

                Log.d("ReferralViewModel", "Data referral berhasil dimuat: " +
                    "${overview.userReferrals.size} downline, " +
                    "kode=${overview.referralInfo.referralCode}")

            }.onFailure { err ->
                Log.w("ReferralViewModel", "Gagal memuat referral live: ${err.message}")
                // Gunakan cache jika ada, BUKAN data dummy
                val cachedInfo = preferences.getReferralInfo()
                val cachedReferrals = preferences.getUserReferrals()
                val cachedWithdrawals = preferences.getWithdrawals()

                _referralInfo.value = cachedInfo
                _userReferrals.value = cachedReferrals
                _withdrawals.value = cachedWithdrawals
            }

            _isLoading.value = false
            onComplete?.invoke()
        }
    }

    fun refreshData() {
        loadReferralDataFromServer()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun copyReferralCode(ctx: Context) {
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // Gunakan kode dari state atau fallback ke variabel global
        val code = _referralInfo.value.referralCode.ifBlank { SubscriptionApiConfig.REFERRAL_CODE }
        val clip = ClipData.newPlainText("Kode Referral IKIKASIR", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(ctx, "Kode referral $code berhasil disalin!", Toast.LENGTH_SHORT).show()
    }

    fun shareReferral(ctx: Context) {
        val code = _referralInfo.value.referralCode.ifBlank { SubscriptionApiConfig.REFERRAL_CODE }
        val namaUsaha = SubscriptionApiConfig.BUSINESS_NAME.ifBlank { "toko saya" }
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

    /**
     * Mengajukan calon client baru menggunakan kode referral akun ini.
     * Calon client akan otomatis terikat dan masuk ke antrean verifikasi Finance/Admin.
     * Ketika diverifikasi & aktif, akun ini akan mendapatkan komisi Rp 15.000.
     */
    fun submitClient(
        name: String,
        businessName: String,
        phone: String,
        email: String,
        notes: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError("Nama pemilik/calon client wajib diisi")
            return
        }
        if (businessName.isBlank()) {
            onError("Nama toko/usaha wajib diisi")
            return
        }
        if (phone.isBlank()) {
            onError("Nomor WhatsApp wajib diisi")
            return
        }
        if (email.isBlank()) {
            onError("Email calon client wajib diisi")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            // Token referral global: prioritas dari server, fallback ke session lokal
            val sessionToken = ReferralSessionManager.getToken()
            val currentCode = _referralInfo.value.referralCode
                .ifBlank { SubscriptionApiConfig.REFERRAL_CODE }
                .ifBlank { sessionToken?.referralCode.orEmpty() }
            val userEmail = SubscriptionApiConfig.CLIENT_EMAIL
                .ifBlank { sessionToken?.email.orEmpty() }
            val customerId = SubscriptionApiConfig.CUSTOMER_ID
                .ifBlank { sessionToken?.customerId.orEmpty() }

            if (currentCode.isBlank()) {
                _isLoading.value = false
                onError("Kode referral belum tersedia. Silakan hubungkan aplikasi ke server terlebih dahulu.")
                return@launch
            }

            val result = apiService.submitClient(
                name = name,
                businessName = businessName,
                phone = phone,
                email = email,
                notes = notes,
                referralCode = currentCode,
                userEmail = userEmail,
                customerId = customerId
            )

            _isLoading.value = false

            result.onSuccess { message ->
                // Refresh data live dari backend agar calon client baru langsung muncul di daftar
                loadReferralDataFromServer()
                onSuccess(message)
            }.onFailure { err ->
                onError(err.message ?: "Gagal mengajukan calon client.")
            }
        }
    }

    /**
     * Mengajukan penarikan saldo komisi ke Backend Manajemen IKIKASIR
     */
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

        viewModelScope.launch {
            _isLoading.value = true
            val email = SubscriptionApiConfig.CLIENT_EMAIL

            val result = apiService.requestWithdrawal(
                email = email,
                amount = amount,
                bankOrWallet = bankOrWallet,
                accountNumber = accountNumber,
                accountHolder = accountHolder,
                notes = notes
            )

            result.onSuccess {
                // Berhasil tercatat di server, reload overview
                loadReferralDataFromServer()
                _isLoading.value = false
                onSuccess()
            }.onFailure { err ->
                // Jika backend offline, fallback catat secara lokal di Preferences
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

                val localSuccess = preferences.addWithdrawal(transaction)
                _isLoading.value = false
                if (localSuccess) {
                    _referralInfo.value = preferences.getReferralInfo()
                    _withdrawals.value = preferences.getWithdrawals()
                    onSuccess()
                } else {
                    onError(err.message ?: "Gagal memproses penarikan saldo.")
                }
            }
        }
    }

    /**
     * Memungkinkan simulasi perubahan status dari "Menunggu Verifikasi finance" menjadi "Selesai"
     * untuk memverifikasi alur lengkap pada mind map: Tarik Saldo -> Menunggu Verifikasi finance -> Selesai
     */
    fun simulateApproveWithdrawal(transactionId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            preferences.updateWithdrawalStatus(transactionId, WithdrawalStatus.SELESAI)
            _withdrawals.value = preferences.getWithdrawals()
            onDone()
        }
    }

    fun updateUpline(uplineName: String, uplineCode: String) {
        preferences.updateUpline(uplineName, uplineCode)
        _referralInfo.value = preferences.getReferralInfo()
    }
}
