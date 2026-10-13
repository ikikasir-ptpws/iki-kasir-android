package com.ptpws.ikikasir.screens.scanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ptpws.ikikasir.core.responsive.presentation.util.ProvideWindowSizeInfo
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity layar pemindai barcode khusus yang responsif.
 *
 * Menerima pemanggilan via Intent dan mengembalikan hasil barcode melalui:
 * `setResult(Activity.RESULT_OK, Intent().putExtra("SCAN_RESULT", scannedCode))`
 *
 * Mendukung Android 10 ke bawah (Android 8, 9, 10) hingga Android 15 ke atas.
 */
@AndroidEntryPoint
class BarcodeScannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProvideWindowSizeInfo {
                IKIKASIRTheme {
                    BarcodeScannerScreen(
                        onBarcodeScanned = { scannedCode ->
                            val resultIntent = Intent().apply {
                                putExtra("SCAN_RESULT", scannedCode)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        },
                        onClose = {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                    )
                }
            }
        }
    }
}
