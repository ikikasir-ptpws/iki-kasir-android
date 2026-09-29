package com.ptpws.ikikasir.screens.produk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.ptpws.ikikasir.commond.GlobalCrudResultDialogHost
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
                Box(Modifier.fillMaxSize()) {
                    DetailProdukScreen(
                        onBack = { finish() },
                        produkId = produkId
                    )
                    GlobalCrudResultDialogHost()
                }
            }
        }
    }
}
