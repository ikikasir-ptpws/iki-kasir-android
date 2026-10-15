package com.ptpws.ikikasir.screens.manajemenpengguna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.ptpws.ikikasir.commond.GlobalCrudResultDialogHost
import com.ptpws.ikikasir.commond.BaseActivity
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TambahPenggunaActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("USER_ID")
        enableEdgeToEdge()
        setContent {
            IKIKASIRTheme {
                Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                    TambahPenggunaScreen(
                        userId = userId,
                        onBack = { finish() },
                        onSimpanPengguna = { finish() }
                    )
                    GlobalCrudResultDialogHost(showSuccess = false)
                }
            }
        }
    }
}