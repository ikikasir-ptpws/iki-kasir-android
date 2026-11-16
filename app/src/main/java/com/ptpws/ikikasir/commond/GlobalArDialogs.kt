package com.ptpws.ikikasir.commond

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.screens.scanner.PokemonGoStyleArHologramShowcase
import com.ptpws.ikikasir.screens.scanner.PokemonGoStyleArHologramShowcase
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

/**
 * Dialog Pop-Up Setting AR yang dipanggil dari Halaman Profil.
 * Berisi Switch toggle untuk mengaktifkan atau menonaktifkan fitur AR saat scan produk.
 */
@Composable
fun ArSettingDialog(
    initialIsArEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onSaveSetting: (Boolean) -> Unit
) {
    var isArEnabled by remember { mutableStateOf(initialIsArEnabled) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Handle Bar Indicator
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF6366F1))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Setting AR",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Atur preferensi penggunaan fitur Augmented Reality (AR) untuk memindai produk.",
                    fontSize = 12.sp,
                    fontFamily = interfamily,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Container Card Switch Setting AR
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isArEnabled) Color(0xFFEEF2FF) else Color(0xFFF1F5F9),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.ViewInAr,
                                        contentDescription = "Setting AR",
                                        tint = if (isArEnabled) Color(0xFF4F46E5) else Color(0xFF64748B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Fitur AR Mode",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (isArEnabled) "Aktif (Tampilkan opsi AR saat scan)" else "Nonaktif (Langsung scan barcode)",
                                    fontFamily = interfamily,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Switch(
                            checked = isArEnabled,
                            onCheckedChange = { isArEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4F46E5),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = "Batal",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = {
                            onSaveSetting(isArEnabled)
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text(
                            text = "Simpan",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog Pop-Up Pilihan Mode Scan (Scan Barcode vs Mode AR).
 * Dipanggil dari Halaman Produk saat tombol scan ditekan dan settingan AR aktif.
 */
@Composable
fun PilihModeScanDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onScanBarcodeSelected: () -> Unit,
    onArSelected: () -> Unit,
    title: String = "Pilih Mode Pemindaian",
    subtitle: String = "Fitur AR aktif. Pilih metode pemindaian produk yang ingin digunakan:"
) {
    if (!show) return

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Handle Bar Indicator
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF6366F1))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontFamily = interfamily,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Row: Dua Kartu Pilihan Utama (Scan Barcode & Mode AR)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // OPSI 1: SCAN BARCODE (Kartu Indigo)
                    VerticalScanOptionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.QrCodeScanner,
                        backgroundColor = Color(0xFF5B5CF0),
                        title = "Scan Barcode",
                        subtitle = "Pindai kode QR / Barcode",
                        onClick = {
                            onDismissRequest()
                            onScanBarcodeSelected()
                        }
                    )

                    // OPSI 2: MODE AR (Kartu Hijau / Emerald)
                    VerticalScanOptionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.ViewInAr,
                        backgroundColor = Color(0xFF05A66B),
                        title = "Mode AR",
                        subtitle = "Tampilan AR",
                        onClick = {
                            onDismissRequest()
                            onArSelected()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Subcaption
                Text(
                    text = "Pengaturan AR dapat diubah kapan saja di menu Profil > Setting AR",
                    fontSize = 11.sp,
                    fontFamily = interfamily,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol Batal
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Batal",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalScanOptionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    backgroundColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontFamily = interfamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontFamily = interfamily,
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.88f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Dialog AR 3D Viewfinder & Visualisasi Detail Produk 3 Dimensi.
 * Menampilkan objek 3D interaktif yang dapat diputar 360° dengan gestur sentuh,
 * overlay kisi ruang AR, detail spek produk, stok, harga, serta opsi pilih produk.
 */
@Composable
fun Ar3DProductViewerDialog(
    show: Boolean,
    produk: Produk?,
    availableProducts: List<Produk> = emptyList(),
    onDismissRequest: () -> Unit,
    onProductSelected: (Produk) -> Unit = {},
    onAddToCart: ((Produk) -> Unit)? = null
) {
    if (!show) return

    val context = LocalContext.current
    var currentProduk by remember(produk, availableProducts) {
        mutableStateOf(produk ?: availableProducts.firstOrNull())
    }

    var rotationX by remember { mutableStateOf(-15f) }
    var rotationY by remember { mutableStateOf(35f) }
    var isAutoRotate by remember { mutableStateOf(true) }
    var showProductDropdown by remember { mutableStateOf(false) }

    // Loop animasi rotasi 3D otomatis
    LaunchedEffect(isAutoRotate) {
        while (isAutoRotate) {
            delay(16)
            rotationY = (rotationY + 1f) % 360f
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
        ) {
            // ── 1. AR Spatial Grid Background Overlay ──
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridStep = 40.dp.toPx()
                val w = size.width
                val h = size.height

                // Titik-titik Kisi Ruang Spasial AR
                for (x in 0..(w / gridStep).toInt()) {
                    for (y in 0..(h / gridStep).toInt()) {
                        drawCircle(
                            color = Color(0xFF6366F1).copy(alpha = 0.15f),
                            radius = 1.5.dp.toPx(),
                            center = Offset(x * gridStep, y * gridStep)
                        )
                    }
                }

                // AR Target Reticle Circle
                drawCircle(
                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                    radius = min(w, h) * 0.28f,
                    center = Offset(w / 2f, h / 2f - 40.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                    )
                )
            }

            // ── 2. Header Top Bar AR ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup AR",
                        tint = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = "AR 3D Active",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = {
                        rotationX = -15f
                        rotationY = 35f
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RotateRight,
                        contentDescription = "Reset 3D",
                        tint = Color.White
                    )
                }
            }

            // ── 3. Interactive Hologram Object & Detail Viewport ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 60.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                PokemonGoStyleArHologramShowcase(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    produk = currentProduk,
                    onScanLainnya = onDismissRequest
                )
            }
        }
    }
}

/**
 * Komponen Renderer Objek 3D (Kubus/Box Produk) Interaktif berbasis Isometric Canvas.
 */
@Composable
fun Interactive3DProductCube(
    modifier: Modifier = Modifier,
    productName: String,
    rotationX: Float,
    rotationY: Float,
    onRotationChange: (Float, Float) -> Unit
) {
    PokemonGoStyleArHologramShowcase(
        modifier = modifier,
        produk = null,
        onScanLainnya = null
    )
}
