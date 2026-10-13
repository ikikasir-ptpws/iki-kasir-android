package com.ptpws.ikikasir.screens.penjualan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import com.ptpws.ikikasir.screens.penjualan.ui.theme.IKIKASIRTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailTransaksiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val transactionId = intent.getStringExtra("TRANSACTION_ID") ?: ""

        setContent {
            IKIKASIRTheme {
                Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                    DetailTransaksiScreen(
                        transactionId = transactionId,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}
