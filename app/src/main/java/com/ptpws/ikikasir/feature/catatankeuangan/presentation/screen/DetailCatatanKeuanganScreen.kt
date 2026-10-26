package com.ptpws.ikikasir.feature.catatankeuangan.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import com.ptpws.ikikasir.commond.GlobalTextField
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.catatankeuangan.presentation.viewmodel.DetailCatatanKeuanganViewModel
import com.ptpws.ikikasir.screens.navigation.AppScreen
import com.ptpws.ikikasir.screens.navigation.popBackStackSafely
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailCatatanKeuanganScreen(
    navController: NavController,
    onBack: () -> Unit = { navController.popBackStackSafely(AppScreen.CatatanKeuangan.route) },
    onEdit: (String) -> Unit = {},
    viewModel: DetailCatatanKeuanganViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFullImageDialog by remember { mutableStateOf(false) }

    val handleBack = {
        if (!navController.popBackStackSafely(AppScreen.CatatanKeuangan.route)) {
            onBack()
        }
    }
    BackHandler(onBack = handleBack)

    // Dialog Konfirmasi Hapus Catatan
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Hapus Catatan Keuangan",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus catatan \"${state.catatan?.title.orEmpty()}\"? Data yang dihapus tidak dapat dikembalikan.",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCatatan {
                            handleBack()
                        }
                    },
                    enabled = !state.isDeleting
                ) {
                    if (state.isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFFEF4444))
                    } else {
                        Text(
                            text = "Hapus",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        text = "Batal",
                        color = Color(0xFF6B7280),
                        fontFamily = interfamily
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Dialog Preview Foto Bukti Ukuran Penuh
    if (showFullImageDialog && state.catatan?.proofImageUrl.orEmpty().isNotBlank()) {
        Dialog(onDismissRequest = { showFullImageDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    AsyncImage(
                        model = state.catatan?.proofImageUrl,
                        contentDescription = "Bukti Pembayaran Penuh",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(onClick = { showFullImageDialog = false }) {
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Catatan",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4F46E5))
            }
        } else if (state.catatan == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.errorMessage ?: "Data catatan tidak ditemukan.",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        } else {
            val item = state.catatan!!
            val formattedAmount = formatDetailRupiah(item.amount)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── 1. Hero Nominal Card ──────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isPemasukan) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (item.isPemasukan) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Type Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (item.isPemasukan) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isPemasukan) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                                    contentDescription = null,
                                    tint = if (item.isPemasukan) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (item.isPemasukan) "PEMASUKAN" else "PENGELUARAN",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (item.isPemasukan) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }
                        }

                        // Nominal Display
                        Text(
                            text = (if (item.isPemasukan) "+ " else "- ") + formattedAmount,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = if (item.isPemasukan) Color(0xFF059669) else Color(0xFFDC2626)
                        )

                        // Title / Keterangan
                        Text(
                            text = item.title,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ── 2. Rincian Informasi ─────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "RINCIAN INFORMASI",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )

                        // Tanggal Transaksi
                        DetailInfoRow(
                            icon = Icons.Outlined.CalendarMonth,
                            label = "Tanggal & Waktu",
                            value = buildString {
                                append(item.date)
                                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(item.createdAt.toDate())
                                append(" ($time WIB)")
                            }
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                        // Metode Pembayaran
                        DetailInfoRow(
                            icon = Icons.Outlined.Payment,
                            label = "Metode Pembayaran",
                            value = item.paymentMethod.ifBlank { "Tunai" }
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                        // Status Sinkronisasi
                        DetailInfoRow(
                            icon = if (item.isSynced) Icons.Outlined.CheckCircle else Icons.Outlined.Sync,
                            label = "Status Sinkronisasi",
                            value = if (item.isSynced) "Tersinkron dengan Server" else "Pending (Disimpan Lokal)",
                            valueColor = if (item.isSynced) Color(0xFF16A34A) else Color(0xFFD97706)
                        )

                        // Catatan / Deskripsi
                        if (item.note.isNotBlank()) {
                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            DetailInfoRow(
                                icon = Icons.Outlined.Description,
                                label = "Catatan Tambahan",
                                value = item.note
                            )
                        }
                    }
                }

                // ── 3. Bukti Pembayaran / Lampiran ───────────────────────────
                if (item.proofImageUrl.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BUKTI PEMBAYARAN / LAMPIRAN",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clickable { showFullImageDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = item.proofImageUrl,
                                        contentDescription = "Foto Bukti Pembayaran",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Ketuk untuk memperbesar",
                                            fontFamily = interfamily,
                                            fontSize = 10.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── 4. Action Buttons ─────────────────────────────────────────
                Button(
                    onClick = { onEdit(item.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Catatan",
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus Catatan",
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF0F172A)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF1F5F9),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                fontFamily = interfamily,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
            Text(
                text = value,
                fontFamily = interfamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

private fun formatDetailRupiah(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp " + formatter.format(amount.toLong())
}
