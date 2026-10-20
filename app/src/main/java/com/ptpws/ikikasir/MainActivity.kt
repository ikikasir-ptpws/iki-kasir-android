package com.ptpws.ikikasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ptpws.ikikasir.commond.BaseActivity
import com.ptpws.ikikasir.core.responsive.presentation.util.ProvideWindowSizeInfo
import com.ptpws.ikikasir.feature.subscription.data.ExpiredSessionManager
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionApiConfig
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionCheckResult
import com.ptpws.ikikasir.feature.subscription.domain.CheckSubscriptionUseCase
import com.ptpws.ikikasir.feature.subscription.domain.SubscriptionFlowResult
import com.ptpws.ikikasir.feature.subscription.presentation.ExpiredPopupDialog
import com.ptpws.ikikasir.screens.navigation.AppNavHost
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProvideWindowSizeInfo {
                IKIKASIRTheme {
                    val context = this@MainActivity
                    val sessionManager = remember { ExpiredSessionManager(context) }
                    val checkUseCase = remember { CheckSubscriptionUseCase(sessionManager) }
                    val coroutineScope = rememberCoroutineScope()
                    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
                    val currentUser = auth.currentUser

                    // Proteksi ganda: Cek apakah session lokal berstatus expired
                    var isExpiredLocked by remember {
                        mutableStateOf(sessionManager.hasExpiredSession() && !sessionManager.isSessionDataActive())
                    }
                    var expiredData by remember { mutableStateOf(sessionManager.getCachedSession()) }
                    var isRefreshing by remember { mutableStateOf(false) }

                    // Kunci tombol back saat dialog expired aktif agar aplikasi keluar bersih
                    BackHandler(enabled = isExpiredLocked) {
                        finishAffinity()
                    }

                    // Verifikasi berkala saat halaman utama dibuka / di-resume
                    LaunchedEffect(Unit) {
                        val licenseEmail = SubscriptionApiConfig.CLIENT_EMAIL
                        val result = checkUseCase.execute(licenseEmail)
                        when (result) {
                            is SubscriptionFlowResult.ExpiredFromCache -> {
                                expiredData = result.cached
                                isExpiredLocked = true
                            }
                            is SubscriptionFlowResult.ExpiredFromServer -> {
                                expiredData = result.result
                                isExpiredLocked = true
                            }
                            is SubscriptionFlowResult.ProceedFromCache,
                            is SubscriptionFlowResult.ProceedFromServer -> {
                                isExpiredLocked = false
                            }
                            else -> {}
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        // Layar Dashboard Utama: Sembunyikan total jika status terkunci/expired
                        if (!isExpiredLocked) {
                            AppNavHost()
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White)
                            )
                        }

                        // Jika Masa Aktif Habis: Kunci total tampilan kasir & tampilkan Popup Modal Kadaluarsa
                        if (isExpiredLocked) {
                            ExpiredPopupDialog(
                                subscriptionData = expiredData,
                                isLoadingRefresh = isRefreshing,
                                onRefresh = {
                                    val licenseEmail = SubscriptionApiConfig.CLIENT_EMAIL
                                    coroutineScope.launch {
                                        isRefreshing = true
                                        val refreshResult = checkUseCase.execute(licenseEmail, forceRefresh = true)
                                        isRefreshing = false
                                        when (refreshResult) {
                                            is SubscriptionFlowResult.ProceedFromCache,
                                            is SubscriptionFlowResult.ProceedFromServer -> {
                                                isExpiredLocked = false
                                            }
                                            is SubscriptionFlowResult.ExpiredFromCache -> {
                                                expiredData = refreshResult.cached
                                                isExpiredLocked = true
                                            }
                                            is SubscriptionFlowResult.ExpiredFromServer -> {
                                                expiredData = refreshResult.result
                                                isExpiredLocked = true
                                            }
                                            else -> {}
                                        }
                                    }
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
    }
}