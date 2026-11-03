package com.ptpws.ikikasir.screens.keuangan

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Payments
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
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.keuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel.DetailCatatanKeuanganViewModel
import com.ptpws.ikikasir.screens.navigation.AppScreen
import com.ptpws.ikikasir.screens.navigation.popBackStackSafely
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailCatatanKeuanganScreen(
    navController: NavController,
    viewModel: DetailCatatanKeuanganViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val handleBack: () -> Unit = {
        if (!navController.popBackStack()) {
            navController.popBackStackSafely(expectedRoute = AppScreen.DetailCatatanKeuangan.baseRoute)
        }
    }
    BackHandler(onBack = handleBack)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showFullImageDialog by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { handleBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
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
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        } else if (!state.errorMessage.isNullOrBlank() || state.catatan == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.errorMessage ?: "Data tidak ditemukan",
                    fontFamily = interfamily,
                    color = Color(0xFFDC2626)
                )
            }
        } else {
            val item = state.catatan!!
            val isPemasukan = item.jenis.equals("PEMASUKAN", ignoreCase = true)

            val headerBg = if (isPemasukan) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
            val badgeBg = if (isPemasukan) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
            val amountColor = if (isPemasukan) Color(0xFF059669) else Color(0xFFDC2626)
            val amountPrefix = if (isPemasukan) "+ " else "- "

            val fullDateStr = SimpleDateFormat("dd MMM yyyy (HH:mm 'WIB')", Locale("id", "ID")).format(Date(item.tanggalMillis))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── 1. TOP HEADER CARD (Jenis Badge, Nominal Besar & Judul)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = headerBg),
                        border = BorderStroke(1.dp, if (isPemasukan) Color(0xFFA7F3D0) else Color(0xFFFCA5A5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Badge Type
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = badgeBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isPemasukan) "↗" else "↘",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = amountColor
                                    )
                                    Text(
                                        text = if (isPemasukan) "PEMASUKAN" else "PENGELUARAN",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = amountColor,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            // Main Amount
                            Text(
                                text = "$amountPrefix${formatNominalRupiahDetail(item.nominal)}",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = amountColor,
                                textAlign = TextAlign.Center
                            )

                            // Title
                            Text(
                                text = item.judul,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // ── 2. RINCIAN INFORMASI CARD
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "RINCIAN INFORMASI",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 0.5.sp
                            )

                            // Tanggal & Waktu
                            RincianInfoRow(
                                icon = Icons.Outlined.CalendarMonth,
                                label = "Tanggal & Waktu",
                                value = fullDateStr
                            )

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            // Metode Pembayaran
                            RincianInfoRow(
                                icon = Icons.Outlined.Payments,
                                label = "Metode Pembayaran",
                                value = item.metodePembayaran
                            )

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            // Status Sinkronisasi
                            RincianInfoRow(
                                icon = Icons.Outlined.CheckCircle,
                                label = "Status Sinkronisasi",
                                value = "Tersinkron dengan Server",
                                valueColor = Color(0xFF059669)
                            )

                            // Catatan Tambahan (Keterangan)
                            if (item.keterangan.isNotBlank()) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                RincianInfoRow(
                                    icon = Icons.Outlined.Description,
                                    label = "Catatan Tambahan",
                                    value = item.keterangan
                                )
                            }
                        }
                    }
                }

                // ── 3. BUKTI PEMBAYARAN CARD (Jika ada foto lampiran)
                if (!item.buktiUri.isNullOrBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Image,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "BUKTI PEMBAYARAN / LAMPIRAN",
                                        fontFamily = interfamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                                        .clickable { showFullImageDialog = true }
                                ) {
                                    AsyncImage(
                                        model = item.buktiUri,
                                        contentDescription = "Bukti Pembayaran",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(bottomStart = 10.dp),
                                        color = Color(0xFF0F172A).copy(alpha = 0.75f),
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Text(
                                            text = "Ketuk untuk memperbesar",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 4. TOMBOL EDIT & HAPUS
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Edit Catatan Button
                        Button(
                            onClick = {
                                navController.navigate(AppScreen.TambahCatatanKeuangan.routeWith(item.id))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
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
                        }

                        // Hapus Catatan Button
                        OutlinedButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFFDC2626)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
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
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Modal View Bukti Pembayaran Dialog (Sesuai Gambar 4)
    if (showFullImageDialog && state.catatan?.buktiUri != null) {
        Dialog(onDismissRequest = { showFullImageDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header: Title & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bukti Pembayaran",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(
                            onClick = { showFullImageDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Image Box Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = state.catatan?.buktiUri,
                            contentDescription = "Bukti Pembayaran",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Action Button: Tutup
                    Button(
                        onClick = { showFullImageDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text(
                            text = "Tutup",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Modal Konfirmasi Hapus
    if (showDeleteConfirmDialog && state.catatan != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Hapus Catatan Keuangan?",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "Catatan \"${state.catatan?.judul}\" akan dihapus secara permanen.",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteCatatan {
                            GlobalCrudResultDialog.success("Catatan keuangan berhasil dihapus.")
                            handleBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Hapus", fontFamily = interfamily, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal", fontFamily = interfamily, color = Color(0xFF64748B))
                }
            }
        )
    }
}

@Composable
private fun RincianInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF0F172A)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF1F5F9),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = interfamily,
                fontSize = 11.5.sp,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = interfamily,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

private fun formatNominalRupiahDetail(nominal: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${formatter.format(nominal.toLong())}"
}
