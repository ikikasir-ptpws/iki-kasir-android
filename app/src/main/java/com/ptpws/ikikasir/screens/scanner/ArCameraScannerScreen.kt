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
import android.widget.Toast
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
 * 1. Foto Produk Hologram Statis dengan fitur Putar dan Zoom aktif.
 * 2. Kartu Informasi Detail Produk tanpa garis penghubung dengan jarak rapat ke foto produk:
 *    - Atas: Kode Barcode
 *    - Bawah Barcode: Nama Produk & Kategori
 *    - Tengah: Foto Produk Hologram
 *    - Bawah: Harga Jual & Stok Tersedia
 * 3. Tanpa lingkaran/kotak biru pedestal & tanpa tombol kamera shutter.
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
                            text = if (isScanned) "Mode AR Aktif" else "Scanning AR...",
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
                        .padding(top = 60.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PokemonGoStyleArHologramShowcase(
                        modifier = Modifier.fillMaxSize(),
                        produk = p,
                        categoryName = categoryName,
                        onScanLainnya = {
                            isScanned = false
                            scannedProduct = null
                        }
                    )
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
 * Visualizer Hologram Foto Produk & Detail Informasi Lengkap Style AR Pokémon GO
 * (Tanpa garis penghubung, jarak kartu informasi rapat / dekat dengan foto produk).
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

    var scaleFactor by remember { mutableStateOf(1f) }
    var rotationAngle by remember { mutableStateOf(0f) }

    val effectivePrice = p?.sellingPrice ?: 0.0

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // ── Kartu Informasi Spasial Melayang & Foto Produk (Rapat & Terpusat) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TOP SECTION: Kode Barcode & (Nama Produk & Kategori)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Kode Barcode
                    ArGlassCard(
                        title = "Kode Barcode",
                        icon = Icons.Default.QrCode,
                        iconTint = Color(0xFFFBBF24),
                        modifier = Modifier.width(220.dp)
                    ) {
                        Text(
                            text = p?.barcode?.ifBlank { "Belum ada barcode" } ?: "Belum ada barcode",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Nama Produk (Kiri) & Kategori (Kanan)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ArGlassCard(
                            title = "Nama Produk",
                            icon = Icons.Default.LocalOffer,
                            iconTint = Color(0xFF10B981),
                            modifier = Modifier.weight(1f).padding(end = 6.dp)
                        ) {
                            Text(
                                text = p?.name ?: "Produk",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        ArGlassCard(
                            title = "Kategori Produk",
                            icon = Icons.Default.Category,
                            iconTint = Color(0xFF60A5FA),
                            modifier = Modifier.weight(1f).padding(start = 6.dp)
                        ) {
                            Text(
                                text = categoryName.ifBlank { "Umum" },
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF60A5FA),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 2. MIDDLE SECTION: FOTO PRODUK HOLOGRAM DI TENGAH
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                    border = BorderStroke(2.5.dp, Color(0xFF10B981)),
                    modifier = Modifier
                        .size(width = (145 * scaleFactor).dp, height = (145 * scaleFactor).dp)
                        .graphicsLayer(rotationZ = rotationAngle)
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
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEEF2FF),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.ViewInAr,
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = p?.name ?: "Produk",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Badge 3D AR
                        Surface(
                            shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 24.dp),
                            color = Color(0xFF10B981),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = "3D AR",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // 3. BOTTOM SECTION: Harga Jual (Kiri) & Stok Tersedia (Kanan)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ArGlassCard(
                        title = "Harga Jual",
                        icon = Icons.Default.LocalOffer,
                        iconTint = Color(0xFF34D399),
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    ) {
                        Text(
                            text = "Rp ${formatRupiah(effectivePrice)}",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF34D399),
                            maxLines = 1
                        )
                    }

                    val isLowStock = (p?.stock ?: 0) <= (p?.lowStockThreshold ?: 5)
                    ArGlassCard(
                        title = "Stok Tersedia",
                        icon = Icons.Default.Inventory,
                        iconTint = if (isLowStock) Color(0xFFF87171) else Color(0xFF60A5FA),
                        modifier = Modifier.weight(1f).padding(start = 6.dp)
                    ) {
                        Text(
                            text = "${p?.stock ?: 0} item",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isLowStock) Color(0xFFF87171) else Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // C. BOTTOM ACTION CONTROLS (Putar, Perbesar, Perkecil) & TOMBOL LIHAT PRODUK LAINNYA
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Floating Control Bar (Putar, Perbesar, Perkecil)
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.Black.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Putar
                    ArControlButton(
                        icon = Icons.Default.Refresh,
                        label = "Putar",
                        onClick = {
                            rotationAngle = (rotationAngle + 90f) % 360f
                        }
                    )
                    // Perbesar
                    ArControlButton(
                        icon = Icons.Default.ZoomIn,
                        label = "Perbesar",
                        onClick = {
                            if (scaleFactor < 1.4f) scaleFactor += 0.15f
                        }
                    )
                    // Perkecil
                    ArControlButton(
                        icon = Icons.Default.ZoomOut,
                        label = "Perkecil",
                        onClick = {
                            if (scaleFactor > 0.7f) scaleFactor -= 0.15f
                        }
                    )
                }
            }

            // Bottom Button: Lihat Produk Lainnya
            if (onScanLainnya != null) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clickable(onClick = onScanLainnya)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ViewInAr,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Lihat Produk Lainnya",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Komponen Kartu Glassmorphic Spasial AR untuk Informasi Rincian Produk.
 */
@Composable
fun ArGlassCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = title,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            content()
        }
    }
}

/**
 * Tombol Kontrol AR di Control Bar Bawah (Putar, Perbesar, Perkecil).
 */
@Composable
fun ArControlButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = interfamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFE2E8F0)
        )
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
