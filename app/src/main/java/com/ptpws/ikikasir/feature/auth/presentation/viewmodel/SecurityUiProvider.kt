package com.ptpws.ikikasir.feature.auth.presentation.viewmodel

import com.ptpws.ikikasir.feature.auth.presentation.state.SecurityState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk SecurityUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface SecurityUiProvider {
    val state: StateFlow<SecurityState>
    fun updateCurrentPassword(value: String)
    fun updateNewPassword(value: String)
    fun updateConfirmPassword(value: String)
    fun clearForm()
    fun changePassword()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke SecurityViewModel asli.
 */
class HiltSecurityUiProvider(private val viewModel: SecurityViewModel) : SecurityUiProvider {
    override val state: StateFlow<SecurityState> get() = viewModel.state
    override fun updateCurrentPassword(value: String) = viewModel.updateCurrentPassword(value)
    override fun updateNewPassword(value: String) = viewModel.updateNewPassword(value)
    override fun updateConfirmPassword(value: String) = viewModel.updateConfirmPassword(value)
    override fun clearForm() = viewModel.clearForm()
    override fun changePassword() = viewModel.changePassword()
}
