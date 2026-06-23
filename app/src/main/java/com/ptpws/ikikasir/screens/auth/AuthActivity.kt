package com.ptpws.ikikasir.screens.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ikikasir.app.ui.screen.LoginScreen
import com.ptpws.ikikasir.MainActivity
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IKIKASIRTheme {
                LoginScreen(
                    onLoginClick = { email, password ->
                        // Navigate to MainActivity when login is clicked
                        startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
