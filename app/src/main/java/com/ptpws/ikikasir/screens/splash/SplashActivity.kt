package com.ptpws.ikikasir.screens.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import com.ptpws.ikikasir.screens.auth.AuthActivity
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
                    startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                    finish()
                }
            }
        }
    }
}
