package com.ptpws.ikikasir.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.auth.domain.usecase.ChangePasswordUseCase
import com.ptpws.ikikasir.feature.auth.presentation.state.SecurityState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val logActivityUseCase: LogActivityUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SecurityState())
    val state: StateFlow<SecurityState> = _state.asStateFlow()

    fun updateCurrentPassword(value: String) = updateField { copy(currentPassword = value) }
    fun updateNewPassword(value: String) = updateField { copy(newPassword = value) }
    fun updateConfirmPassword(value: String) = updateField { copy(confirmPassword = value) }

    fun clearForm() {
        _state.value = SecurityState()
    }

    fun changePassword() {
        val current = _state.value
        if (current.isLoading) return

        when {
            current.currentPassword.isBlank() -> {
                setError("Masukkan kata sandi saat ini.")
                return
            }
            current.newPassword.length < 6 -> {
                setError("Kata sandi baru harus memiliki minimal 6 karakter.")
                return
            }
            current.newPassword != current.confirmPassword -> {
                setError("Konfirmasi kata sandi baru tidak sama.")
                return
            }
            current.currentPassword == current.newPassword -> {
                setError("Kata sandi baru harus berbeda dari kata sandi saat ini.")
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            changePasswordUseCase(current.currentPassword, current.newPassword)
                .onSuccess {
                    _state.value = SecurityState(isSuccess = true)
                    GlobalCrudResultDialog.success("Kata sandi akun berhasil diubah.")
                    try {
                        logActivityUseCase(
                            title = "Kata Sandi Diubah",
                            description = "Pengguna berhasil mengubah kata sandi akun.",
                            category = "AUTHENTICATION",
                            action = "CHANGE_PASSWORD"
                        )
                    } catch (error: Exception) {
                        GlobalCrudResultDialog.failure(
                            "Kata sandi berhasil diubah, tetapi gagal mencatat perubahan ke audit log: " +
                                (error.message ?: "kesalahan tidak diketahui.")
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Gagal mengubah kata sandi.")
                    }
                }
        }
    }

    private fun setError(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }

    private inline fun updateField(
        crossinline transform: SecurityState.() -> SecurityState
    ) {
        _state.update { it.transform().copy(errorMessage = null) }
    }
}
