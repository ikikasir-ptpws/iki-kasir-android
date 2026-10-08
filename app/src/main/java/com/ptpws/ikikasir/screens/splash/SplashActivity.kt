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
import kotlinx.coroutines.tasks.await

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IKIKASIRTheme {
                SplashScreen()
                
                LaunchedEffect(key1 = true) {
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    val currentUser = auth.currentUser
                    var canProceedToMain = false

                    if (currentUser != null) {
                        try {
                            currentUser.reload().await()
                            canProceedToMain = auth.currentUser != null
                        } catch (e: Exception) {
                            if (e is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                                e.message?.contains("no user record", ignoreCase = true) == true ||
                                e.message?.contains("user-not-found", ignoreCase = true) == true) {
                                auth.signOut()
                                canProceedToMain = false
                            } else {
                                canProceedToMain = true
                            }
                        }
                    }

                    delay(1500L)
                    if (canProceedToMain) {
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
