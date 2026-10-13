package com.ptpws.ikikasir.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.ptpws.ikikasir.screens.scanner.BarcodeScannerActivity

/**
 * Hook Composable untuk memindai barcode & QR code dengan layar khusus yang responsif.
 *
 * Mengarahkan ke [BarcodeScannerActivity] yang dibangun dengan CameraX & ML Kit Bundled:
 * 1. 100% Offline tanpa ketergantungan pada modul Google Play Services eksternal.
 * 2. Mendukung penuh semua versi Android: dari Android 8 (API 26), Android 10 ke bawah, hingga Android 15 ke atas.
 * 3. Tampilan responsif adaptif (ponsel, tablet, mode portrait/landscape).
 * 4. Animasi laser scan, kontrol lampu senter (flash), ganti kamera depan/belakang, dan dialog input manual.
 */
@Composable
fun rememberBarcodeScanner(
    onBarcodeScanned: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val currentOnBarcodeScanned by rememberUpdatedState(onBarcodeScanned)

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra("SCAN_RESULT")
            if (!code.isNullOrBlank()) {
                currentOnBarcodeScanned(code)
            }
        }
    }

    return remember(context, scannerLauncher) {
        {
            val intent = Intent(context, BarcodeScannerActivity::class.java)
            scannerLauncher.launch(intent)
        }
    }
}
