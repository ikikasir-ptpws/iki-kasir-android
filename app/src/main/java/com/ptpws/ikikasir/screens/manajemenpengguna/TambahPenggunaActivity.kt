package com.ptpws.ikikasir.screens.manajemenpengguna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TambahPenggunaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("USER_ID")
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                TambahPenggunaScreen(
                    userId = userId,
                    onBack = { finish() },
                    onSimpanPengguna = { finish() }
                )
            }
        }
    }
}