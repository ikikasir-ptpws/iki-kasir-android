package com.ptpws.ikikasir.screens.promo

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
class TambahPromoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val promoId = intent.getStringExtra("PROMO_ID") ?: intent.getStringExtra("promoId")
        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize()) {
                    TambahPromoScreen(
                        promoId = promoId,
                        onBack = { finish() },
                        onSimpanPromo = { finish() }
                    )
                    GlobalCrudResultDialogHost()
                }
            }
        }
    }
}