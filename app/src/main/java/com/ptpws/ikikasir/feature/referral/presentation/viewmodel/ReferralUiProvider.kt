package com.ptpws.ikikasir.feature.referral.presentation.viewmodel

import android.content.Context
import com.ptpws.ikikasir.feature.referral.domain.model.ReferralInfo
import com.ptpws.ikikasir.feature.referral.domain.model.UserReferralItem
import com.ptpws.ikikasir.feature.referral.domain.model.WithdrawalTransaction
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk ReferralUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface ReferralUiProvider {
    val referralInfo: StateFlow<ReferralInfo>
    val userReferrals: StateFlow<List<UserReferralItem>>
    val filteredUserReferrals: StateFlow<List<UserReferralItem>>
    val withdrawals: StateFlow<List<WithdrawalTransaction>>
    val searchQuery: StateFlow<String>
    val selectedFilter: StateFlow<String>
    val isLoading: StateFlow<Boolean>
    fun refreshData()
    fun setSearchQuery(query: String)
    fun setSelectedFilter(filter: String)
    fun copyReferralCode(ctx: Context)
    fun shareReferral(ctx: Context)
    fun submitClient(name: String, businessName: String, phone: String, email: String, notes: String, onSuccess: (String) -> Unit, onError: (String) -> Unit)
    fun submitWithdrawal(amount: Double, bankOrWallet: String, accountNumber: String, accountHolder: String, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit)
    fun simulateApproveWithdrawal(transactionId: String, onDone: () -> Unit = {})
    fun updateUpline(uplineName: String, uplineCode: String)
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke ReferralViewModel asli.
 */
class HiltReferralUiProvider(private val viewModel: ReferralViewModel) : ReferralUiProvider {
    override val referralInfo: StateFlow<ReferralInfo> get() = viewModel.referralInfo
    override val userReferrals: StateFlow<List<UserReferralItem>> get() = viewModel.userReferrals
    override val filteredUserReferrals: StateFlow<List<UserReferralItem>> get() = viewModel.filteredUserReferrals
    override val withdrawals: StateFlow<List<WithdrawalTransaction>> get() = viewModel.withdrawals
    override val searchQuery: StateFlow<String> get() = viewModel.searchQuery
    override val selectedFilter: StateFlow<String> get() = viewModel.selectedFilter
    override val isLoading: StateFlow<Boolean> get() = viewModel.isLoading
    override fun refreshData() = viewModel.refreshData()
    override fun setSearchQuery(query: String) = viewModel.setSearchQuery(query)
    override fun setSelectedFilter(filter: String) = viewModel.setSelectedFilter(filter)
    override fun copyReferralCode(ctx: Context) = viewModel.copyReferralCode(ctx)
    override fun shareReferral(ctx: Context) = viewModel.shareReferral(ctx)
    override fun submitClient(name: String, businessName: String, phone: String, email: String, notes: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) = viewModel.submitClient(name, businessName, phone, email, notes, onSuccess, onError)
    override fun submitWithdrawal(amount: Double, bankOrWallet: String, accountNumber: String, accountHolder: String, notes: String, onSuccess: () -> Unit, onError: (String) -> Unit) = viewModel.submitWithdrawal(amount, bankOrWallet, accountNumber, accountHolder, notes, onSuccess, onError)
    override fun simulateApproveWithdrawal(transactionId: String, onDone: () -> Unit) = viewModel.simulateApproveWithdrawal(transactionId, onDone)
    override fun updateUpline(uplineName: String, uplineCode: String) = viewModel.updateUpline(uplineName, uplineCode)
}
