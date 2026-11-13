package com.ptpws.ikikasir.feature.manajemenpengguna.presentation.viewmodel

import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.state.UserFormState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk Form Pengguna.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface UserFormUiProvider {
    val formState: StateFlow<UserFormState>
    fun loadUserById(userId: String)
    fun onFullNameChange(value: String)
    fun onEmailChange(value: String)
    fun onCurrentPasswordChange(value: String)
    fun onPasswordChange(value: String)
    fun onConfirmPasswordChange(value: String)
    fun onRoleChange(value: String)
    fun onActiveChange(value: Boolean)
    fun onPhotoUrlChange(value: String)
    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit)
    fun resetForm()
    fun saveUser(onSuccess: () -> Unit = {})
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke UserViewModel asli.
 */
class HiltUserFormUiProvider(private val viewModel: UserViewModel) : UserFormUiProvider {
    override val formState: StateFlow<UserFormState> get() = viewModel.formState
    override fun loadUserById(userId: String) = viewModel.loadUserById(userId)
    override fun onFullNameChange(value: String) = viewModel.onFullNameChange(value)
    override fun onEmailChange(value: String) = viewModel.onEmailChange(value)
    override fun onCurrentPasswordChange(value: String) = viewModel.onCurrentPasswordChange(value)
    override fun onPasswordChange(value: String) = viewModel.onPasswordChange(value)
    override fun onConfirmPasswordChange(value: String) = viewModel.onConfirmPasswordChange(value)
    override fun onRoleChange(value: String) = viewModel.onRoleChange(value)
    override fun onActiveChange(value: Boolean) = viewModel.onActiveChange(value)
    override fun onPhotoUrlChange(value: String) = viewModel.onPhotoUrlChange(value)
    override fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) =
        viewModel.sendPasswordResetEmail(email, onResult)
    override fun resetForm() = viewModel.resetForm()
    override fun saveUser(onSuccess: () -> Unit) = viewModel.saveUser(onSuccess)
}
