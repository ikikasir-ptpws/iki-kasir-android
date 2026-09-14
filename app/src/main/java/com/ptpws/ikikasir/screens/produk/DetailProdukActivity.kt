package com.ptpws.ikikasir.screens.produk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ptpws.ikikasir.screens.produk.ui.theme.IKIKASIRTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailProdukActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val produkId = intent.getStringExtra("produkId")
        setContent {
            IKIKASIRTheme {
                DetailProdukScreen(
                    onBack = { finish() },
                    produkId = produkId
                )
            }
        }
    }
}
