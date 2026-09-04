package com.ptpws.ikikasir.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object LoggedOut : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun isUserLoggedIn(): Boolean {
        return repository.isUserLoggedIn()
    }

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            _authState.value = AuthState.Error("Email dan password harus diisi")
            return
        }

        android.util.Log.d("AuthStatus", "Mencoba login dengan email: '$cleanEmail'")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signInWithEmailAndPassword(cleanEmail, cleanPassword).collect { result ->
                if (result.isSuccess) {
                    android.util.Log.d("AuthStatus", "Firebase Auth sukses mengembalikan respon")
                    _authState.value = AuthState.Success
                } else {
                    val exception = result.exceptionOrNull()
                    val rawMsg = exception?.message ?: "Terjadi kesalahan tidak dikenal"
                    android.util.Log.e("AuthStatus", "Firebase Auth error detail: $rawMsg", exception)

                    val userFriendlyMsg = when {
                        rawMsg.contains("no user record", ignoreCase = true) ||
                        rawMsg.contains("user-not-found", ignoreCase = true) ->
                            "Akun tidak ditemukan. Pastikan email sudah terdaftar di Firebase Console."

                        rawMsg.contains("password is invalid", ignoreCase = true) ||
                        rawMsg.contains("wrong-password", ignoreCase = true) ||
                        rawMsg.contains("invalid-credential", ignoreCase = true) ->
                            "Email atau kata sandi salah. Silakan periksa kembali."

                        rawMsg.contains("network error", ignoreCase = true) ||
                        rawMsg.contains("interrupted connection", ignoreCase = true) ->
                            "Gagal terhubung ke server. Periksa koneksi internet Anda."

                        rawMsg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
                        rawMsg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ||
                        rawMsg.contains("sign-in provider is disabled", ignoreCase = true) ->
                            "Metode Email/Password belum diaktifkan di Firebase Console (Authentication > Sign-in method)."

                        rawMsg.contains("blocked all requests", ignoreCase = true) ||
                        rawMsg.contains("too-many-requests", ignoreCase = true) ->
                            "Terlalu banyak percobaan login gagal. Silakan coba beberapa saat lagi."

                        else -> rawMsg
                    }

                    _authState.value = AuthState.Error(userFriendlyMsg)
                }
            }
        }
    }

    fun logout() {
        android.util.Log.d("AuthStatus", "Proses Logout...")
        repository.signOut()
        _authState.value = AuthState.LoggedOut
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
