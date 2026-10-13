package com.ptpws.ikikasir.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.ptpws.ikikasir.screens.scanner.BarcodeScannerActivity

/**
 * Hook Composable untuk memindai barcode & QR code dengan arsitektur Dual-Engine Cerdas:
 *
 * 1. Android 11 ke atas (API >= 30):
 *    - Mengutamakan Google Code Scanner (play-services-code-scanner) dengan antarmuka modern & fitur Auto-Zoom.
 *    - Auto-Fallback Cerdas: Jika Google Play Services melempar error apa pun (misal modul belum terunduh,
 *      offline, atau HP non-GMS), sistem langsung otomatis membuka BarcodeScannerActivity in-app.
 *
 * 2. Android 10 ke bawah (API < 30):
 *    - Langsung membuka BarcodeScannerActivity in-app (CameraX + ML Kit) yang responsif, 100% offline,
 *      dan tanpa risiko kendala Google Play Services.
 */
@Composable
fun rememberBarcodeScanner(
    onBarcodeScanned: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val currentOnBarcodeScanned by rememberUpdatedState(onBarcodeScanned)

    // Launcher untuk Screen Scanner Responsif In-App (CameraX)
    val customScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra("SCAN_RESULT")
            if (!code.isNullOrBlank()) {
                currentOnBarcodeScanned(code)
            }
        }
    }

    val launchCustomScanner: () -> Unit = remember(context, customScannerLauncher) {
        {
            val intent = Intent(context, BarcodeScannerActivity::class.java)
            customScannerLauncher.launch(intent)
        }
    }

    val launchGmsScanner: () -> Unit = remember(context, launchCustomScanner) {
        {
            try {
                val options = GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .enableAutoZoom()
                    .build()

                val scanner = GmsBarcodeScanning.getClient(context, options)
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            currentOnBarcodeScanned(rawValue)
                        }
                    }
                    .addOnCanceledListener {
                        // Pengguna sengaja membatalkan scan (tekan tombol back/tutup), abaikan
                    }
                    .addOnFailureListener {
                        // Auto-Fallback Cerdas: Begitu GMS melempar error apa pun,
                        // langsung buka screen scanner kamera in-app tanpa membuat pengguna macet!
                        launchCustomScanner()
                    }
            } catch (e: Exception) {
                // Jika terjadi exception saat memanggil GMS, langsung buka screen scanner in-app
                launchCustomScanner()
            }
        }
    }

    return remember(context, launchGmsScanner, launchCustomScanner) {
        {
            // Android 11 (API 30) ke atas: Utamakan Google Code Scanner dengan Auto-Zoom
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                launchGmsScanner()
            } else {
                // Android 10 ke bawah: Langsung gunakan Screen Scanner in-app yang stabil & responsif
                launchCustomScanner()
            }
        }
    }
}
