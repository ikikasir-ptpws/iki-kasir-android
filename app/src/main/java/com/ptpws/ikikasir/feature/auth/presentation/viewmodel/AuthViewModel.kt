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
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email dan password harus diisi")
            return
        }

        android.util.Log.d("AuthStatus", "Mencoba login dengan email: $email")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signInWithEmailAndPassword(email, password).collect { result ->
                if (result.isSuccess) {
                    android.util.Log.d("AuthStatus", "Firebase Auth sukses mengembalikan respon")
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Login gagal"
                    android.util.Log.e("AuthStatus", "Firebase Auth error: $errorMsg")
                    _authState.value = AuthState.Error("Email atau sandi salah") // Custom user friendly error
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
