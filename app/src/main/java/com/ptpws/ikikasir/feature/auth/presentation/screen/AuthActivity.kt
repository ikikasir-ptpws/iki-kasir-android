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
import androidx.compose.runtime.rememberCoroutineScope
import com.ptpws.ikikasir.BaseApplication
import com.ptpws.ikikasir.MainActivity
import com.ptpws.ikikasir.feature.auth.presentation.viewmodel.AuthState
import com.ptpws.ikikasir.feature.auth.presentation.viewmodel.AuthViewModel
import com.ptpws.ikikasir.feature.subscription.data.ExpiredSessionManager
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionApiConfig
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionCheckResult
import com.ptpws.ikikasir.feature.subscription.domain.CheckSubscriptionUseCase
import com.ptpws.ikikasir.feature.subscription.domain.SubscriptionFlowResult
import com.ptpws.ikikasir.feature.subscription.presentation.ExpiredPopupDialog
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

import com.ptpws.ikikasir.commond.BaseActivity

@AndroidEntryPoint
class AuthActivity : BaseActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IKIKASIRTheme {
                val coroutineScope = rememberCoroutineScope()
                val sessionManager = remember { ExpiredSessionManager(this@AuthActivity) }
                val checkUseCase = remember { CheckSubscriptionUseCase(sessionManager) }

                var showExpiredDialog by remember { mutableStateOf(false) }
                var expiredSubscriptionData by remember { mutableStateOf<SubscriptionCheckResult?>(null) }
                var isCheckingSubscription by remember { mutableStateOf(false) }
                var lastEnteredEmail by remember { mutableStateOf("") }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    val authState by viewModel.authState.collectAsState()
                    var showErrorDialog by remember { mutableStateOf(false) }
                    var showSuccessDialog by remember { mutableStateOf(false) }
                    var errorMessage by remember { mutableStateOf("") }

                    // Observe auth state to trigger dialogs
                    LaunchedEffect(authState) {
                        when (authState) {
                            is AuthState.Success -> {
                                Log.d("AuthStatus", "Login Berhasil! Menampilkan Popup")
                                showSuccessDialog = true
                            }
                            is AuthState.Error -> {
                                val msg = (authState as AuthState.Error).message
                                Log.e("AuthStatus", "Login Gagal: $msg")
                                errorMessage = msg
                                showErrorDialog = true
                            }
                            else -> {}
                        }
                    }

                    if (showSuccessDialog) {
                        LaunchedEffect(Unit) {
                            delay(3000)
                            showSuccessDialog = false
                            viewModel.resetState()
                            startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                            finish()
                        }

                        Dialog(onDismissRequest = { }) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Login Berhasil!",
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFF111827)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Selamat datang kembali.",
                                        fontFamily = interfamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B7280),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (showErrorDialog) {
                        Dialog(onDismissRequest = { 
                            showErrorDialog = false
                            viewModel.resetState()
                        }) {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Gagal Masuk",
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFF111827)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = errorMessage,
                                        fontFamily = interfamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B7280),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            showErrorDialog = false
                                            viewModel.resetState()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = "Coba Lagi",
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tampilkan Popup Kadaluarsa jika masa aktif pelanggan telah berakhir
                    if (showExpiredDialog) {
                        ExpiredPopupDialog(
                            subscriptionData = expiredSubscriptionData,
                            isLoadingRefresh = isCheckingSubscription,
                            onRefresh = {
                                coroutineScope.launch {
                                    isCheckingSubscription = true
                                    val refreshResult = checkUseCase.execute(SubscriptionApiConfig.CLIENT_EMAIL, forceRefresh = true)
                                    isCheckingSubscription = false

                                    when (refreshResult) {
                                        is SubscriptionFlowResult.ProceedFromCache,
                                        is SubscriptionFlowResult.ProceedFromServer -> {
                                            showExpiredDialog = false
                                            Toast.makeText(this@AuthActivity, "Status langganan aktif! Silakan login kembali.", Toast.LENGTH_SHORT).show()
                                        }
                                        is SubscriptionFlowResult.ExpiredFromCache -> {
                                            expiredSubscriptionData = refreshResult.cached
                                        }
                                        is SubscriptionFlowResult.ExpiredFromServer -> {
                                            expiredSubscriptionData = refreshResult.result
                                        }
                                        is SubscriptionFlowResult.NetworkError -> {
                                            Toast.makeText(this@AuthActivity, "Gagal memverifikasi ke server: ${refreshResult.errorMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onExit = {
                                finishAffinity()
                            }
                        )
                    }

                    LoginScreen(
                        isLoading = authState is AuthState.Loading || isCheckingSubscription,
                        onLoginClick = { email, password ->
                            lastEnteredEmail = email.trim()
                            if (lastEnteredEmail.isBlank() || password.isBlank()) {
                                viewModel.login(email, password)
                                return@LoginScreen
                            }

                            // Eksekusi Diagram Alur Pengecekan Expired:
                            // 1. Cek session/local cache expired
                            // 2. Jika ada -> Apakah masih aktif?
                            //    - Ya -> viewModel.login
                            //    - Tidak -> Tampilkan ExpiredPopupDialog
                            // 3. Jika tidak ada -> Fetch API data expired diserver
                            //    - Apakah masih aktif?
                            //      - Ya -> Simpan di Session -> viewModel.login
                            //      - Tidak -> Tampilkan ExpiredPopupDialog
                            coroutineScope.launch {
                                isCheckingSubscription = true
                                // Cek lisensi toko (ikikasir.id@gmail.com) yang terdaftar di CRM IKIKASIR
                                val flowResult = checkUseCase.execute(SubscriptionApiConfig.CLIENT_EMAIL)
                                isCheckingSubscription = false

                                when (flowResult) {
                                    is SubscriptionFlowResult.ProceedFromCache,
                                    is SubscriptionFlowResult.ProceedFromServer -> {
                                        showExpiredDialog = false
                                        viewModel.login(email, password)
                                    }
                                    is SubscriptionFlowResult.ExpiredFromCache -> {
                                        expiredSubscriptionData = flowResult.cached
                                        showExpiredDialog = true
                                    }
                                    is SubscriptionFlowResult.ExpiredFromServer -> {
                                        expiredSubscriptionData = flowResult.result
                                        showExpiredDialog = true
                                    }
                                    is SubscriptionFlowResult.NetworkError -> {
                                        // Jika server backend tidak terjangkau, tampilkan notifikasi diagnostik lalu lanjutkan percobaan login Firebase
                                        Toast.makeText(
                                            this@AuthActivity,
                                            "Peringatan backend: ${flowResult.errorMessage}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        viewModel.login(email, password)
                                    }
                                }
                            }
                        },
                        onSendPasswordReset = { email, onResult ->
                            viewModel.sendPasswordResetEmail(email, onResult)
                        }
                    )
                }
            }
        }
    }
}
