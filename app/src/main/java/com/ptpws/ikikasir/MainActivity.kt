package com.ptpws.ikikasir

import android.os.Bundle
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ptpws.ikikasir.commond.BaseActivity
import com.ptpws.ikikasir.core.responsive.presentation.util.ProvideWindowSizeInfo
import com.ptpws.ikikasir.feature.subscription.data.ExpiredSessionManager
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionApiConfig
import com.ptpws.ikikasir.feature.subscription.domain.CheckSubscriptionUseCase
import com.ptpws.ikikasir.feature.subscription.domain.SubscriptionFlowResult
import com.ptpws.ikikasir.feature.subscription.presentation.ExpiredPopupDialog
import com.ptpws.ikikasir.screens.navigation.AppNavHost
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Interval polling status langganan saat dashboard terbuka (5 menit) */
private const val SUBSCRIPTION_POLL_INTERVAL_MS = 5 * 60 * 1000L

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
                    val lifecycleOwner = LocalLifecycleOwner.current

                    // Proteksi: Cek apakah session lokal berstatus expired sebagai nilai awal
                    var isExpiredLocked by remember {
                        mutableStateOf(sessionManager.hasExpiredSession() && !sessionManager.isSessionDataActive())
                    }
                    var expiredData by remember { mutableStateOf(sessionManager.getCachedSession()) }
                    var isRefreshing by remember { mutableStateOf(false) }
                    var pollingJob by remember { mutableStateOf<Job?>(null) }

                    /** Helper: terapkan hasil cek subscription ke UI state */
                    fun applySubscriptionResult(result: SubscriptionFlowResult) {
                        when (result) {
                            is SubscriptionFlowResult.ExpiredFromCache -> {
                                android.util.Log.w("MainActivity", "EXPIRED (cache) - kunci dashboard!")
                                expiredData = result.cached
                                isExpiredLocked = true
                            }
                            is SubscriptionFlowResult.ExpiredFromServer -> {
                                android.util.Log.w("MainActivity", "EXPIRED (server) - kunci dashboard!")
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

                    /** Mulai loop polling berkala setiap 5 menit */
                    fun startPollingLoop() {
                        pollingJob?.cancel()
                        pollingJob = coroutineScope.launch {
                            android.util.Log.d("MainActivity", "Polling loop dimulai.")
                            while (isActive) {
                                delay(SUBSCRIPTION_POLL_INTERVAL_MS)
                                android.util.Log.d("MainActivity", "Polling 5 menit: cek subscription...")
                                try {
                                    val result = checkUseCase.execute(
                                        SubscriptionApiConfig.CLIENT_EMAIL,
                                        forceRefresh = true
                                    )
                                    applySubscriptionResult(result)
                                    if (isExpiredLocked) break // Hentikan polling jika sudah expired
                                } catch (e: Exception) {
                                    android.util.Log.w("MainActivity", "Polling error: ${e.message}")
                                }
                            }
                        }
                    }

                    /** Jalankan 1x cek ke server, lalu (re-)start polling loop */
                    fun triggerImmediateCheck() {
                        coroutineScope.launch {
                            android.util.Log.d("MainActivity", "Immediate subscription check (onResume/init)...")
                            try {
                                val result = checkUseCase.execute(
                                    SubscriptionApiConfig.CLIENT_EMAIL,
                                    forceRefresh = true
                                )
                                applySubscriptionResult(result)
                            } catch (e: Exception) {
                                android.util.Log.w("MainActivity", "Immediate check error: ${e.message}")
                            }
                            // Restart polling loop agar interval dimulai fresh setelah cek ini
                            if (!isExpiredLocked) startPollingLoop()
                        }
                    }

                    // Kunci tombol back saat dialog expired aktif
                    BackHandler(enabled = isExpiredLocked) {
                        finishAffinity()
                    }

                    // Observer lifecycle: cek ulang saat app kembali ke foreground (onResume)
                    // Dan mulai polling pertama kali saat ON_START
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> {
                                    // Setiap kali app kembali ke layar, langsung re-check
                                    triggerImmediateCheck()
                                }
                                Lifecycle.Event.ON_PAUSE -> {
                                    // Saat app di-minimize, hentikan polling untuk hemat baterai
                                    pollingJob?.cancel()
                                    android.util.Log.d("MainActivity", "Polling dijeda (app pause).")
                                }
                                else -> {}
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                            pollingJob?.cancel()
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

                        // Jika Masa Aktif Habis: Kunci dashboard & tampilkan Popup Modal Kadaluarsa
                        if (isExpiredLocked) {
                            ExpiredPopupDialog(
                                subscriptionData = expiredData,
                                isLoadingRefresh = isRefreshing,
                                onRefresh = {
                                    coroutineScope.launch {
                                        isRefreshing = true
                                        try {
                                            val refreshResult = checkUseCase.execute(
                                                SubscriptionApiConfig.CLIENT_EMAIL,
                                                forceRefresh = true
                                            )
                                            applySubscriptionResult(refreshResult)
                                            if (!isExpiredLocked) startPollingLoop()
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Refresh error: ${e.message}")
                                        } finally {
                                            isRefreshing = false
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
