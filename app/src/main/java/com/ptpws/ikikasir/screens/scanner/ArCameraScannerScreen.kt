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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.min

/**
 * Pemindai Kamera AR (Augmented Reality) Pokémon GO Style & Visualisasi Detail Produk Hologram.
 *
 * Fitur:
 * 1. Foto Produk Hologram Statis (Tetap diam lurus tanpa rotasi).
 * 2. Kartu Informasi Detail Produk Hologram Spasial Lengkap (Kategori Produk, Harga Jual, Modal, Laba,
 *    Stok Saat Ini, Batas Minimum Stok, Barcode, Diskon).
 * 3. Tombol Kasir dihapus, hanya menyisakan tombol Scan Lainnya.
 */
@OptIn(ExperimentalGetImage::class)
@Composable
fun ArCameraScannerScreen(
    show: Boolean = true,
    produkList: List<Produk> = emptyList(),
    kategoriList: List<Kategori> = emptyList(),
    initialProduk: Produk? = null,
    onClose: () -> Unit,
    onAddToCart: ((Produk) -> Unit)? = null
) {
    if (!show) return

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        val isPreview = LocalInspectionMode.current
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // State Kontrol Kamera & AR
        var hasCameraPermission by remember {
            mutableStateOf(
                isPreview ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasCameraPermission = isGranted
        }

        LaunchedEffect(Unit) {
            if (!isPreview && !hasCameraPermission) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        var isTorchOn by remember { mutableStateOf(false) }
        var useFrontCamera by remember { mutableStateOf(false) }
        var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

        // State AR Product
        var scannedProduct by remember(initialProduk) { mutableStateOf<Produk?>(initialProduk) }
        var isScanned by remember(initialProduk) { mutableStateOf(initialProduk != null) }
        var invalidBarcodeMessage by remember { mutableStateOf<String?>(null) }

        val handleBarcodeDetected: (String) -> Unit = { rawCode ->
            val clean = rawCode.trim()
            val matched = produkList.find { p ->
                p.barcode.equals(clean, ignoreCase = true) ||
                    (clean.isNotBlank() && p.barcode.isNotBlank() && p.barcode.trimStart('0') == clean.trimStart('0'))
            }

            if (matched != null) {
                playArBeep(context)
                scannedProduct = matched
                isScanned = true
                invalidBarcodeMessage = null
            } else {
                invalidBarcodeMessage = "Kode \"$clean\" belum terdaftar di katalog produk"
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // ── 1. Live CameraX Feed ──────────────────────────────────────────
            if (hasCameraPermission && !isPreview) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
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
                                                    handleBarcodeDetected(raw)
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
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Memuat Kamera AR...",
                        fontFamily = interfamily,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // ── 2. Overlay AR Spatial Grid & Target Reticle ────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridStep = 44.dp.toPx()
                val w = size.width
                val h = size.height

                // Titik Grid Ruang Spasial AR
                for (x in 0..(w / gridStep).toInt()) {
                    for (y in 0..(h / gridStep).toInt()) {
                        drawCircle(
                            color = Color(0xFF6366F1).copy(alpha = 0.18f),
                            radius = 1.5.dp.toPx(),
                            center = Offset(x * gridStep, y * gridStep)
                        )
                    }
                }

                // Jika belum ada produk ter-scan, tampilkan retikel target pemindaian AR
                if (!isScanned) {
                    val boxSizePx = min(w * 0.7f, 280.dp.toPx())

                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = 0.35f),
                        radius = boxSizePx * 0.6f,
                        center = Offset(w / 2f, h / 2f - 30.dp.toPx()),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                        )
                    )
                }
            }

            // ── 3. Header Controls (Tutup, Torch, Status AR) ─────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onClose,
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

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.85f),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Text(
                            text = if (isScanned) "AR 3D Mode Active" else "Scanning AR...",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Color.White
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            cameraControl?.enableTorch(isTorchOn)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (isTorchOn) Color(0xFFFACC15).copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isTorchOn) Color.Black else Color.White
                        )
                    }
                }
            }

            // ── 4. Visualisasi Hologram Foto & Detail Informasi Produk 3D ──────
            if (isScanned && scannedProduct != null) {
                val p = scannedProduct!!
                val categoryName = remember(p, kategoriList) {
                    val catId = p.categoryId.trim()
                    val matched = kategoriList.find { k ->
                        k.id.equals(catId, ignoreCase = true) || k.name.equals(catId, ignoreCase = true)
                    }
                    when {
                        matched != null -> matched.name
                        catId.contains("-") && catId.length >= 20 -> "Umum"
                        catId.isNotBlank() -> catId
                        else -> "Umum"
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(top = 60.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PokemonGoStyleArHologramShowcase(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            produk = p,
                            categoryName = categoryName,
                            onScanLainnya = {
                                isScanned = false
                                scannedProduct = null
                            }
                        )
                    }
                }
            } else {
                // ── 5. Petunjuk Scan Kamera AR (saat menunggu barcode) ───────────
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 32.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (invalidBarcodeMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = invalidBarcodeMessage!!,
                                fontFamily = interfamily,
                                fontSize = 12.5.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ViewInAr,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Arahkan kamera ke barcode/QR produk untuk menampilkan AR 3D",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                color = Color(0xFFE2E8F0),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visualizer Hologram Foto Produk Statis & Detail Informasi Lengkap Spasial Style Pokémon GO.
 * Menampilkan Foto Produk Statis Lurus di atas Landasan AR dan Kartu Informasi Detail
 * Produk Lengkap (Kategori Produk, Harga Jual, Modal, Laba, Stok, Minimum Stok, Barcode, Diskon)
 * Tanpa Tombol Kasir (Hanya Tombol Scan Lainnya).
 */
@Composable
fun PokemonGoStyleArHologramShowcase(
    modifier: Modifier = Modifier,
    produk: Produk?,
    categoryName: String = "Umum",
    onScanLainnya: (() -> Unit)? = null
) {
    val p = produk
    val formatRupiah = remember {
        { amount: Double ->
            NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // ── 1. Pedestal Spasial AR & Ring Hologram (Statis) ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f + 30.dp.toPx()
            val baseRadius = min(size.width, size.height) * 0.38f

            // Sinar Hologram Emitter Tegak Lurus Dari Landasan
            val beamPath = Path().apply {
                moveTo(cx - baseRadius * 0.55f, cy + baseRadius * 0.3f)
                lineTo(cx + baseRadius * 0.55f, cy + baseRadius * 0.3f)
                lineTo(cx + baseRadius * 0.8f, cy - baseRadius * 1.2f)
                lineTo(cx - baseRadius * 0.8f, cy - baseRadius * 1.2f)
                close()
            }

            drawPath(
                path = beamPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.02f),
                        Color(0xFF6366F1).copy(alpha = 0.18f),
                        Color(0xFF10B981).copy(alpha = 0.35f)
                    )
                )
            )

            // Bayangan & Glow Landasan Pedestal
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.65f),
                        Color(0xFF6366F1).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(cx - baseRadius, cy + baseRadius * 0.15f),
                size = Size(baseRadius * 2f, baseRadius * 0.45f)
            )

            // Cincin Pedestal AR 1
            drawArc(
                color = Color(0xFF10B981).copy(alpha = 0.85f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(cx - baseRadius * 1.1f, cy + baseRadius * 0.08f),
                size = Size(baseRadius * 2.2f, baseRadius * 0.55f),
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f))
                )
            )

            // Cincin Pedestal AR 2
            drawArc(
                color = Color(0xFF38BDF8).copy(alpha = 0.75f),
                startAngle = 45f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(cx - baseRadius * 0.9f, cy + baseRadius * 0.22f),
                size = Size(baseRadius * 1.8f, baseRadius * 0.4f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            )
        }

        // ── 2. Objek Hologram Foto Produk (DIAM / STATIS) & Detail Informasi ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // A. FOTO PRODUK HOLOGRAM (STATIS TEGAK LURUS)
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
                border = BorderStroke(3.dp, Color(0xFF6366F1)),
                modifier = Modifier.size(width = 180.dp, height = 180.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val imgUrl = p?.imageUrl.orEmpty()
                    if (imgUrl.isNotBlank()) {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = p?.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(28.dp))
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEEF2FF),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.ViewInAr,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = p?.name ?: "Foto Produk",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Top Right Hologram Badge
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 14.dp, topEnd = 24.dp),
                        color = Color(0xFF10B981),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "3D AR",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // B. KARTU INFORMASI DETAIL PRODUK HOLOGRAM LENGKAP
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.90f),
                border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                shadowElevation = 14.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Status Hologram
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = "✦ HOLOGRAM PRODUCT DETAILS ✦",
                            fontFamily = interfamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981),
                            letterSpacing = 1.sp
                        )
                    }

                    // 1. Nama Produk
                    Text(
                        text = p?.name ?: "Produk Tidak Dipilih",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Grid Rincian Detail Produk Lengkap (Kategori, Harga Jual, Modal, Laba, Stok, Minimum, Barcode, Diskon)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Row 1: Kategori Produk & Harga Jual
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ArDetailPillItem(
                                label = "Kategori Produk",
                                value = categoryName.ifBlank { "Umum" },
                                valueColor = Color(0xFF60A5FA),
                                modifier = Modifier.weight(1f)
                            )
                            ArDetailPillItem(
                                label = "Harga Jual",
                                value = if (p != null) "Rp ${formatRupiah(p.sellingPrice)}" else "Rp 0",
                                valueColor = Color(0xFF34D399),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2: Harga Beli (Modal) & Estimasi Laba Kotor
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val labaKotor = if (p != null) (p.sellingPrice - p.costPrice) else 0.0
                            val labaText = if (labaKotor >= 0) "+Rp ${formatRupiah(labaKotor)}" else "-Rp ${formatRupiah(-labaKotor)}"
                            ArDetailPillItem(
                                label = "Harga Beli (Modal)",
                                value = if (p != null) "Rp ${formatRupiah(p.costPrice)}" else "Rp 0",
                                modifier = Modifier.weight(1f)
                            )
                            ArDetailPillItem(
                                label = "Estimasi Laba",
                                value = labaText,
                                valueColor = if (labaKotor >= 0) Color(0xFF60A5FA) else Color(0xFFF87171),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 3: Stok Saat Ini & Batas Minimum Stok
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val isLowStock = (p?.stock ?: 0) <= (p?.lowStockThreshold ?: 5)
                            ArDetailPillItem(
                                label = "Stok Saat Ini",
                                value = "${p?.stock ?: 0} item",
                                valueColor = if (isLowStock) Color(0xFFF87171) else Color(0xFFE2E8F0),
                                modifier = Modifier.weight(1f)
                            )
                            ArDetailPillItem(
                                label = "Min. Stok",
                                value = "${p?.lowStockThreshold ?: 5} item",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 4: Kode Barcode & Diskon Produk (jika ada)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ArDetailPillItem(
                                label = "Kode Barcode",
                                value = p?.barcode?.ifBlank { "Kode belum diisi" } ?: "Kode belum diisi",
                                modifier = Modifier.weight(1f)
                            )
                            if (p != null && p.discount > 0) {
                                val diskonText = if (p.discountType.equals("PERCENT", ignoreCase = true) || p.discountType == "%") {
                                    "${p.discount.toInt()}%"
                                } else {
                                    "Rp ${formatRupiah(p.discount)}"
                                }
                                ArDetailPillItem(
                                    label = "Diskon Produk",
                                    value = diskonText,
                                    valueColor = Color(0xFFFBBF24),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 4. Tombol Aksi (HANYA Tombol Scan Lainnya, Tombol +Kasir Dihapus)
                    if (onScanLainnya != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = onScanLainnya,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scan Lainnya",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item Kapsul/Pill Rincian Detail Produk Hologram.
 */
@Composable
private fun ArDetailPillItem(
    label: String,
    value: String,
    valueColor: Color = Color(0xFFE2E8F0),
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.10f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp)
        ) {
            Text(
                text = label,
                fontFamily = interfamily,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = interfamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
private fun playArBeep(context: Context) {
    try {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 150)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(100)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
