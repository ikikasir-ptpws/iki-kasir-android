package com.ptpws.ikikasir.feature.pengaturan.presentation.viewmodel

import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk ProfilUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface ProfilUiProvider {
    val userProfileState: StateFlow<UserProfileState>
    val isOnline: StateFlow<Boolean>
    val notaSetting: StateFlow<NotaSetting>
    val taxSetting: StateFlow<TaxSetting>
    val paymentMethodSetting: StateFlow<PaymentMethodSetting>
    val tableSetting: StateFlow<TableSetting>
    fun updateUserProfile(newFullName: String, newPhotoUrl: String, onComplete: (Boolean) -> Unit = {})
    fun saveNotaSetting(setting: NotaSetting, onComplete: (Boolean) -> Unit = {})
    fun saveTaxSetting(setting: TaxSetting, onComplete: (Boolean) -> Unit = {})
    fun savePaymentMethodSetting(setting: PaymentMethodSetting, onComplete: (Boolean) -> Unit = {})
    fun saveTableSetting(setting: TableSetting, onComplete: (Boolean) -> Unit = {})
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke ProfilViewModel asli.
 */
class HiltProfilUiProvider(private val viewModel: ProfilViewModel) : ProfilUiProvider {
    override val userProfileState: StateFlow<UserProfileState> get() = viewModel.userProfileState
    override val isOnline: StateFlow<Boolean> get() = viewModel.isOnline
    override val notaSetting: StateFlow<NotaSetting> get() = viewModel.notaSetting
    override val taxSetting: StateFlow<TaxSetting> get() = viewModel.taxSetting
    override val paymentMethodSetting: StateFlow<PaymentMethodSetting> get() = viewModel.paymentMethodSetting
    override val tableSetting: StateFlow<TableSetting> get() = viewModel.tableSetting
    override fun updateUserProfile(newFullName: String, newPhotoUrl: String, onComplete: (Boolean) -> Unit) = viewModel.updateUserProfile(newFullName, newPhotoUrl, onComplete)
    override fun saveNotaSetting(setting: NotaSetting, onComplete: (Boolean) -> Unit) = viewModel.saveNotaSetting(setting, onComplete)
    override fun saveTaxSetting(setting: TaxSetting, onComplete: (Boolean) -> Unit) = viewModel.saveTaxSetting(setting, onComplete)
    override fun savePaymentMethodSetting(setting: PaymentMethodSetting, onComplete: (Boolean) -> Unit) = viewModel.savePaymentMethodSetting(setting, onComplete)
    override fun saveTableSetting(setting: TableSetting, onComplete: (Boolean) -> Unit) = viewModel.saveTableSetting(setting, onComplete)
}
