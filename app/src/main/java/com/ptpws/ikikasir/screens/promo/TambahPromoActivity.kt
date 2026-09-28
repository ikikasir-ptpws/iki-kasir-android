package com.ptpws.ikikasir.screens.promo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TambahPromoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val promoId = intent.getStringExtra("PROMO_ID") ?: intent.getStringExtra("promoId")
        setContent {
            MaterialTheme {
                TambahPromoScreen(
                    promoId = promoId,
                    onBack = { finish() },
                    onSimpanPromo = { finish() }
                )
            }
        }
    }
}