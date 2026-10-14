package com.ptpws.ikikasir.screens.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.ptpws.ikikasir.commond.interfamily
import androidx.activity.compose.BackHandler
import java.util.concurrent.Executors
import kotlin.math.min

/**
 * Screen Pemindai Barcode & QR Code Responsif.
 *
 * Menggunakan CameraX + ML Kit (Bundled):
 * 1. 100% Offline tanpa ketergantungan Google Play Services dynamic module.
 * 2. Kompatibel penuh dari Android 8 (API 26), Android 10 ke bawah, hingga Android 15 ke atas.
 * 3. Tampilan responsif (ponsel, tablet, mode landscape/portrait).
 * 4. Dilengkapi animasi laser scan, kontrol flash/torch, pembalik kamera, dan opsi input manual.
 */
@OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeScannerScreen(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    var isClosing by remember { mutableStateOf(false) }
    val handleClose = {
        if (!isClosing) {
            isClosing = true
            onClose()
        }
    }
    BackHandler(enabled = !isClosing, onBack = handleClose)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current

    // Cek izin kamera
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionDeniedContent(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onClose = onClose
        )
        return
    }

    // State Kontrol Kamera
    var isTorchOn by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var isScanned by remember { mutableStateOf(false) }
    var showManualInputDialog by remember { mutableStateOf(false) }

    // Hitung ukuran kotak viewfinder yang responsif
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val scanBoxSizeDp = if (isLandscape) {
        min(configuration.screenHeightDp * 0.58f, 280f).dp
    } else {
        val maxBox = if (configuration.screenWidthDp >= 400) 300f else 280f
        min(configuration.screenWidthDp * 0.72f, maxBox).dp
    }

    // Animasi garis laser scan
    val infiniteTransition = rememberInfiniteTransition(label = "LaserAnimation")
    val laserPositionRatio by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── 1. CameraX PreviewView ──────────────────────────────────────────
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                var cameraInstance: Camera? = null
                val scaleGestureDetector = android.view.ScaleGestureDetector(
                    ctx,
                    object : android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        override fun onScale(detector: android.view.ScaleGestureDetector): Boolean {
                            val cam = cameraInstance ?: return false
                            val currentZoom = cam.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                            val delta = detector.scaleFactor
                            cam.cameraControl.setZoomRatio(currentZoom * delta)
                            return true
                        }
                    }
                )

                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    setOnTouchListener { _, event ->
                        scaleGestureDetector.onTouchEvent(event)
                        true
                    }
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
                    val barcodeScanner = BarcodeScanning.getClient(options)
                    val analysisExecutor = Executors.newSingleThreadExecutor()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !isScanned) {
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val raw = barcode.rawValue
                                        if (!raw.isNullOrBlank() && !isScanned) {
                                            isScanned = true
                                            playFeedback(ctx)
                                            onBarcodeScanned(raw)
                                            break
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val selector = if (useFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    try {
                        cameraProvider.unbindAll()
                        val cam = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageAnalysis
                        )
                        cameraInstance = cam
                        cameraControl = cam.cameraControl
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = {
                cameraControl?.enableTorch(isTorchOn)
            }
        )

        // ── 2. Viewfinder Dim Overlay & Cutout Box ──────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val boxSizePx = scanBoxSizeDp.toPx()
            val left = (canvasWidth - boxSizePx) / 2f
            val top = (canvasHeight - boxSizePx) / 2f

            val cutoutRect = Rect(
                offset = Offset(left, top),
                size = Size(boxSizePx, boxSizePx)
            )

            // Gelapkan area di luar kotak
            val backgroundPath = Path().apply {
                addRect(Rect(Offset.Zero, size))
            }
            val cutoutPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = cutoutRect,
                        cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                    )
                )
            }

            drawPath(
                path = Path.combine(PathOperation.Difference, backgroundPath, cutoutPath),
                color = Color.Black.copy(alpha = 0.65f)
            )

            // Gambar 4 Sudut Viewfinder (Corner Brackets)
            val cornerLength = 32.dp.toPx()
            val strokeWidth = 5.dp.toPx()
            val cornerRadius = 20.dp.toPx()
            val bracketColor = Color(0xFF60A5FA) // Biru Neon terang

            // Kiri Atas
            drawLine(bracketColor, Offset(left + cornerRadius, top), Offset(left + cornerLength, top), strokeWidth, StrokeCap.Round)
            drawLine(bracketColor, Offset(left, top + cornerRadius), Offset(left, top + cornerLength), strokeWidth, StrokeCap.Round)
            drawArc(
                bracketColor,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(left, top),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(strokeWidth)
            )

            // Kanan Atas
            val right = left + boxSizePx
            drawLine(bracketColor, Offset(right - cornerLength, top), Offset(right - cornerRadius, top), strokeWidth, StrokeCap.Round)
            drawLine(bracketColor, Offset(right, top + cornerRadius), Offset(right, top + cornerLength), strokeWidth, StrokeCap.Round)
            drawArc(
                bracketColor,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(right - cornerRadius * 2, top),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(strokeWidth)
            )

            // Kiri Bawah
            val bottom = top + boxSizePx
            drawLine(bracketColor, Offset(left + cornerRadius, bottom), Offset(left + cornerLength, bottom), strokeWidth, StrokeCap.Round)
            drawLine(bracketColor, Offset(left, bottom - cornerLength), Offset(left, bottom - cornerRadius), strokeWidth, StrokeCap.Round)
            drawArc(
                bracketColor,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(left, bottom - cornerRadius * 2),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(strokeWidth)
            )

            // Kanan Bawah
            drawLine(bracketColor, Offset(right - cornerLength, bottom), Offset(right - cornerRadius, bottom), strokeWidth, StrokeCap.Round)
            drawLine(bracketColor, Offset(right, bottom - cornerLength), Offset(right, bottom - cornerRadius), strokeWidth, StrokeCap.Round)
            drawArc(
                bracketColor,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(right - cornerRadius * 2, bottom - cornerRadius * 2),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(strokeWidth)
            )

            // Garis Laser Bergerak dengan Efek Neon Glow
            val laserY = top + (boxSizePx * laserPositionRatio)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF38BDF8),
                        Color(0xFF60A5FA),
                        Color(0xFF38BDF8),
                        Color.Transparent
                    ),
                    startX = left + 10.dp.toPx(),
                    endX = right - 10.dp.toPx()
                ),
                start = Offset(left + 10.dp.toPx(), laserY),
                end = Offset(right - 10.dp.toPx(), laserY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // ── 3. Top Header Bar (Back, Title, Flash, Flip) ────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Tombol Kembali
            IconButton(
                onClick = handleClose,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }

            Text(
                text = "Pemindai Barcode",
                fontFamily = interfamily,
                fontSize = if (configuration.screenWidthDp >= 400) 19.sp else 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Tombol Flash/Senter
                IconButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        cameraControl?.enableTorch(isTorchOn)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (isTorchOn) Color(0xFFFACC15).copy(alpha = 0.85f)
                            else Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (isTorchOn) Color.Black else Color.White
                    )
                }

                // Tombol Flip Kamera
                IconButton(
                    onClick = {
                        useFrontCamera = !useFrontCamera
                        isTorchOn = false
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Ganti Kamera",
                        tint = Color.White
                    )
                }
            }
        }

        // ── 4. Bottom Controls & Petunjuk ──────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Badge Petunjuk
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Arahkan kamera tepat pada barcode atau kode QR",
                        fontFamily = interfamily,
                        fontSize = 12.5.sp,
                        color = Color(0xFFE2E8F0),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Tombol Input Manual (Cadangan jika barcode rusak/kusut)
            OutlinedButton(
                onClick = { showManualInputDialog = true },
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.45f)
                ),
                modifier = Modifier.height(if (configuration.screenWidthDp >= 400) 48.dp else 44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Input Kode Manual",
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        // ── 5. Dialog Input Kode Manual ─────────────────────────────────────
        if (showManualInputDialog) {
            ManualBarcodeDialog(
                onDismiss = { showManualInputDialog = false },
                onSubmit = { manualCode ->
                    showManualInputDialog = false
                    playFeedback(context)
                    onBarcodeScanned(manualCode)
                }
            )
        }
    }
}

/**
 * Dialog untuk mengetik kode barcode secara manual jika barcode fisik rusak atau pudar.
 */
@Composable
private fun ManualBarcodeDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var textCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Input Kode Manual",
                fontFamily = interfamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ketik nomor barcode atau ID produk:",
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                OutlinedTextField(
                    value = textCode,
                    onValueChange = { textCode = it },
                    placeholder = { Text("Contoh: 8991234567890", fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (textCode.isNotBlank()) onSubmit(textCode.trim())
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (textCode.isNotBlank()) onSubmit(textCode.trim()) },
                enabled = textCode.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B32D1))
            ) {
                Text("Gunakan Kode", fontFamily = interfamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", fontFamily = interfamily, color = Color(0xFF64748B))
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

/**
 * Tampilan saat izin kamera ditolak oleh pengguna.
 */
@Composable
private fun CameraPermissionDeniedContent(
    onRequestPermission: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF1E293B),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "Izin Kamera Diperlukan",
                fontFamily = interfamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Aplikasi membutuhkan akses kamera untuk memindai barcode dan kode QR produk Anda secara langsung.",
                fontFamily = interfamily,
                fontSize = 13.5.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRequestPermission,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B32D1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Izinkan Akses Kamera",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            TextButton(onClick = onClose) {
                Text(
                    text = "Kembali",
                    fontFamily = interfamily,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * Memberikan feedback haptic (getar singkat) dan nada beep saat barcode sukses terdeteksi.
 */
@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
private fun playFeedback(context: Context) {
    try {
        // Nada beep pendek
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 120)

        // Getar pendek
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(80)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
