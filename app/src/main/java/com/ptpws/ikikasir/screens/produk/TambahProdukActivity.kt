package com.ptpws.ikikasir.screens.produk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.ptpws.ikikasir.commond.GlobalCrudResultDialogHost
import dagger.hilt.android.AndroidEntryPoint
import com.ptpws.ikikasir.screens.produk.ui.theme.IKIKASIRTheme

@AndroidEntryPoint
class TambahProdukActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IKIKASIRTheme {
                Box(Modifier.fillMaxSize()) {
                    TambahProdukScreen(onBack = { finish() })
                    GlobalCrudResultDialogHost()
                }
            }
        }
    }
}