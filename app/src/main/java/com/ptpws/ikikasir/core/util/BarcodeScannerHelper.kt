package com.ptpws.ikikasir.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.ptpws.ikikasir.screens.scanner.BarcodeScannerActivity

/**
 * Hook Composable untuk memindai barcode & QR code dengan pemisahan eksklusif (Strict Separation):
 *
 * 1. Android 11 (API 30) ke atas:
 *    - HANYA menggunakan Google Code Scanner (play-services-code-scanner) dengan Auto-Zoom.
 *    - Scanner Android 10 ke bawah (BarcodeScannerActivity) dinonaktifkan sepenuhnya sehingga tidak aktif dobel.
 *
 * 2. Android 10 (API < 30) ke bawah:
 *    - HANYA menggunakan Screen Scanner in-app (CameraX + ML Kit).
 *    - Google Code Scanner dinonaktifkan sepenuhnya.
 *
 * Proteksi Anti-Dobel: Menggunakan flag debounce [isScanningInProgress] agar penekanan berulang
 * tidak memicu scan ganda.
 */
@Composable
fun rememberBarcodeScanner(
    onBarcodeScanned: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val currentOnBarcodeScanned by rememberUpdatedState(onBarcodeScanned)
    var isScanningInProgress by remember { mutableStateOf(false) }

    // Launcher untuk Screen Scanner In-App (CameraX - Eksklusif Android 10 ke bawah)
    val customScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isScanningInProgress = false
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra("SCAN_RESULT")
            if (!code.isNullOrBlank()) {
                currentOnBarcodeScanned(code)
            }
        }
    }

    val isAndroid11OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    return remember(context, isAndroid11OrAbove) {
        {
            if (isScanningInProgress) {
                // Cegah pemicuan dobel jika scanner sedang berjalan
                return@remember
            }

            if (isAndroid11OrAbove) {
                // ── Android 11+ : Eksklusif Google Code Scanner ──
                // Scanner kamera in-app nonaktif sepenuhnya agar tidak dobel
                isScanningInProgress = true
                try {
                    val options = GmsBarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .enableAutoZoom()
                        .build()

                    val scanner = GmsBarcodeScanning.getClient(context, options)
                    scanner.startScan()
                        .addOnSuccessListener { barcode ->
                            isScanningInProgress = false
                            val rawValue = barcode.rawValue
                            if (!rawValue.isNullOrBlank()) {
                                currentOnBarcodeScanned(rawValue)
                            }
                        }
                        .addOnCanceledListener {
                            isScanningInProgress = false
                        }
                        .addOnFailureListener { e ->
                            isScanningInProgress = false
                            val errorMsg = e.localizedMessage ?: "Gagal membuka scanner"
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    isScanningInProgress = false
                    Toast.makeText(context, "Error scanner: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // ── Android 10 ke bawah : Eksklusif Screen Scanner In-App (CameraX) ──
                // Google Code Scanner nonaktif sepenuhnya
                isScanningInProgress = true
                val intent = Intent(context, BarcodeScannerActivity::class.java)
                customScannerLauncher.launch(intent)
            }
        }
    }
}
