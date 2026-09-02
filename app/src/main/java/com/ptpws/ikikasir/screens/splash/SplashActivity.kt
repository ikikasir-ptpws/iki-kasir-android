package com.ptpws.ikikasir.screens.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import com.ptpws.ikikasir.feature.auth.presentation.screen.AuthActivity
import com.ptpws.ikikasir.screens.splash.ui.theme.IKIKASIRTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IKIKASIRTheme {
                SplashScreen()
                
                LaunchedEffect(key1 = true) {
                    delay(2000L) // Wait for 2 seconds
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    
                    android.util.Log.d("AuthStatus", "Pengecekan Sesi... Current User Email: ${auth.currentUser?.email ?: "Tidak ada (Belum Login)"}")
                    
                    if (auth.currentUser != null) {
                        android.util.Log.d("AuthStatus", "Mengarahkan ke MainActivity...")
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
