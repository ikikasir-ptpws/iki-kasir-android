package com.ptpws.ikikasir.screens.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.ptpws.ikikasir.commond.BaseActivity
import com.ptpws.ikikasir.feature.auth.presentation.screen.AuthActivity
import com.ptpws.ikikasir.feature.subscription.data.ExpiredSessionManager
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionApiConfig
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionCheckResult
import com.ptpws.ikikasir.feature.subscription.domain.CheckSubscriptionUseCase
import com.ptpws.ikikasir.feature.subscription.domain.SubscriptionFlowResult
import com.ptpws.ikikasir.feature.subscription.presentation.ExpiredPopupDialog
import com.ptpws.ikikasir.screens.splash.ui.theme.IKIKASIRTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = ExpiredSessionManager(this)
        val checkUseCase = CheckSubscriptionUseCase(sessionManager)

        setContent {
            IKIKASIRTheme {
                val coroutineScope = rememberCoroutineScope()
                var showExpiredPopup by remember { mutableStateOf(false) }
                var expiredData by remember { mutableStateOf<SubscriptionCheckResult?>(null) }
                var isChecking by remember { mutableStateOf(false) }
                var isRefreshing by remember { mutableStateOf(false) }
                var currentCheckingEmail by remember { mutableStateOf("") }

                // Tangani tombol Back saat popup kadaluarsa muncul agar aplikasi tertutup bersih (tidak bisa dibypass)
                BackHandler(enabled = showExpiredPopup) {
                    finishAffinity()
                }

                fun performSubscriptionCheck(email: String, forceRefresh: Boolean = false) {
                    coroutineScope.launch {
                        if (forceRefresh) {
                            isRefreshing = true
                        } else {
                            isChecking = true
                        }

                        val flowResult = checkUseCase.execute(email, forceRefresh = forceRefresh)
                        isChecking = false
                        isRefreshing = false

                        when (flowResult) {
                            is SubscriptionFlowResult.ProceedFromCache,
                            is SubscriptionFlowResult.ProceedFromServer -> {
                                // Status AKTIF: Baru diizinkan berpindah membuka halaman berikutnya
                                showExpiredPopup = false
                                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                                if (auth.currentUser != null) {
                                    startActivity(Intent(this@SplashActivity, com.ptpws.ikikasir.MainActivity::class.java))
                                } else {
                                    startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                                }
                                finish()
                            }
                            is SubscriptionFlowResult.ExpiredFromCache -> {
                                // KADALUARSA (dari Cache): Tahan di layar Splash & tampilkan Popup.
                                // MainActivity SAMA SEKALI TIDAK DIBUKA sehingga client tidak pernah melihat dashboard!
                                expiredData = flowResult.cached
                                showExpiredPopup = true
                            }
                            is SubscriptionFlowResult.ExpiredFromServer -> {
                                // KADALUARSA (dari Server): Tahan di layar Splash & tampilkan Popup.
                                // MainActivity SAMA SEKALI TIDAK DIBUKA sehingga client tidak pernah melihat dashboard!
                                expiredData = flowResult.result
                                showExpiredPopup = true
                            }
                            is SubscriptionFlowResult.NetworkError -> {
                                android.util.Log.e("SplashActivity", "Network error saat verifikasi lisensi: ${flowResult.errorMessage}")
                                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                                // Jika server offline tapi ada session lokal yang masih berlaku aktif
                                if (sessionManager.isSessionDataActive(email)) {
                                    if (auth.currentUser != null) {
                                        startActivity(Intent(this@SplashActivity, com.ptpws.ikikasir.MainActivity::class.java))
                                    } else {
                                        startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                                    }
                                    finish()
                                } else if (sessionManager.hasExpiredSession(email)) {
                                    // Session lokal mencatat sudah kadaluarsa
                                    expiredData = sessionManager.getCachedSession()
                                    showExpiredPopup = true
                                } else {
                                    // Belum ada session tersimpan dan server tidak terhubung
                                    android.widget.Toast.makeText(this@SplashActivity, "Gagal koneksi backend: ${flowResult.errorMessage}", android.widget.Toast.LENGTH_SHORT).show()
                                    if (auth.currentUser != null) {
                                        startActivity(Intent(this@SplashActivity, com.ptpws.ikikasir.MainActivity::class.java))
                                    } else {
                                        startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                                    }
                                    finish()
                                }
                            }
                        }
                    }
                }

                // Tampilan Splash Screen (Logo IKIKASIR + status loading jika sedang memverifikasi lisensi)
                SplashScreen(
                    isLoading = isChecking,
                    statusText = if (isChecking) "Memverifikasi status langganan..." else null
                )

                LaunchedEffect(Unit) {
                    // 1. Tampilkan splashscreen selama 1.5 detik agar tampilan logo selesai
                    delay(1500L)

                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    val currentUser = auth.currentUser

                    if (currentUser != null) {
                        try {
                            currentUser.reload().await()
                        } catch (e: Exception) {
                            if (e is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                                e.message?.contains("no user record", ignoreCase = true) == true ||
                                e.message?.contains("user-not-found", ignoreCase = true) == true) {
                                auth.signOut()
                            }
                        }
                    }

                    // 2. TEPAT SETELAH SPLASHSCREEN:
                    // Verifikasi lisensi toko IKIKASIR (menggunakan akun lisensi CRM: ikikasir.id@gmail.com)
                    // Pengecekan ini mengunci aplikasi sebelum masuk ke Dashboard maupun Login jika expired!
                    val licenseEmail = SubscriptionApiConfig.CLIENT_EMAIL
                    currentCheckingEmail = licenseEmail
                    performSubscriptionCheck(licenseEmail)
                }

                // Popup Kadaluarsa: Muncul setelah splashscreen selesai & mengunci total aplikasi
                if (showExpiredPopup) {
                    ExpiredPopupDialog(
                        subscriptionData = expiredData,
                        isLoadingRefresh = isRefreshing,
                        onRefresh = {
                            val targetEmail = SubscriptionApiConfig.CLIENT_EMAIL
                            performSubscriptionCheck(targetEmail, forceRefresh = true)
                        },
                        onExit = {
                            finishAffinity()
                        }
                    )
                }
            }
        }
    }
}
