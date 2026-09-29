package com.ptpws.ikikasir.screens.manajemenpengguna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.ptpws.ikikasir.commond.GlobalCrudResultDialogHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TambahPenggunaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("USER_ID")
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize()) {
                    TambahPenggunaScreen(
                        userId = userId,
                        onBack = { finish() },
                        onSimpanPengguna = { finish() }
                    )
                    GlobalCrudResultDialogHost()
                }
            }
        }
    }
}