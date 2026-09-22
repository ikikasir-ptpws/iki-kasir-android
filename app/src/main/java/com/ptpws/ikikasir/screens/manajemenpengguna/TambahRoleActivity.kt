package com.ptpws.ikikasir.screens.manajemenpengguna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TambahRoleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roleId = intent.getStringExtra("ROLE_ID")
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                TambahRoleScreen(
                    roleId = roleId,
                    onBack = { finish() },
                    onSimpanRole = { finish() }
                )
            }
        }
    }
}
