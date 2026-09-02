package com.ptpws.ikikasir.feature.auth.presentation.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ptpws.ikikasir.BaseApplication
import com.ptpws.ikikasir.MainActivity
import com.ptpws.ikikasir.feature.auth.presentation.viewmodel.AuthState
import com.ptpws.ikikasir.feature.auth.presentation.viewmodel.AuthViewModel
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IKIKASIRTheme {
                val authState by viewModel.authState.collectAsState()

                when (authState) {
                    is AuthState.Success -> {
                        android.util.Log.d("AuthStatus", "Login Berhasil! Pindah ke MainActivity")
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                        viewModel.resetState()
                    }
                    is AuthState.Error -> {
                        val errorMsg = (authState as AuthState.Error).message
                        android.util.Log.e("AuthStatus", "Login Gagal: $errorMsg")
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                    }
                    else -> {}
                }

                LoginScreen(
                    isLoading = authState is AuthState.Loading,
                    onLoginClick = { email, password ->
                        viewModel.login(email, password)
                        
                    }
                )
            }
        }
    }
}
