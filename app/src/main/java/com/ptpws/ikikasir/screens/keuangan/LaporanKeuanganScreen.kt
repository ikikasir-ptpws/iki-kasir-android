package com.ptpws.ikikasir.screens.keuangan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.CustomDateRangePickerDialog
import com.ptpws.ikikasir.commond.interfamily
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanKeuanganScreen(
    navController: NavController,
    onBack: () -> Unit = { navController.popBackStack() },
    onLihatSemuaProduk: () -> Unit = {},
    viewModel: LaporanKeuanganViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var showDateRangePicker by remember { mutableStateOf(false) }
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
    var showDemoInfoDialog by remember { mutableStateOf(false) }

    // Handle toast messages
    LaunchedEffect(state.exportMessage) {
        state.exportMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearExportMessage()
        }
    }

    // Modal Date Range Picker (sama dengan Riwayat Transaksi & Riwayat Antrean)
    if (showDateRangePicker) {
        CustomDateRangePickerDialog(
            initialStartDateMillis = state.customStartDateMillis,
            initialEndDateMillis = state.customEndDateMillis,
            onDismissRequest = { showDateRangePicker = false },
            onDateRangeSelected = { start, end ->
                viewModel.setCustomDateRange(start, end)
                showDateRangePicker = false
            }
        )
    }

    // Dialog Info Data Demo
    if (showDemoInfoDialog) {
        AlertDialog(
            onDismissRequest = { showDemoInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Mode Pratinjau (Simulasi)",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Data finansial yang Anda lihat saat ini adalah simulasi metrik bisnis startup.\n\nBegitu Anda memproses transaksi riil pertama di menu Kasir, seluruh grafik, metrik omzet, laba kotor, laba bersih, dan produk terlaris akan otomatis terisi secara langsung (real-time).",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDemoInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Saya Mengerti", fontFamily = interfamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Laporan Keuangan",
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            color = Color(0xFF0F172A),
                            fontSize = 19.sp
                        )
                        Text(
                            text = "Ringkasan Performa & Arus Kas",
                            fontFamily = interfamily,
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    // Tombol Ekspor Excel
                    FilledTonalButton(
                        onClick = {
                            viewModel.exportLaporan(context) { uri ->
                                uri?.let { shareExcel(context, it) }
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFEFF6FF),
                            contentColor = Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        enabled = !state.isExporting,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (state.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF2563EB)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Ekspor",
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Ekspor",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── 1. Indikator Mode Demo jika Belum Ada Transaksi
            if (state.isDemoData) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showDemoInfoDialog = true },
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Mode Simulasi: Menampilkan data contoh bisnis startup. Klik untuk info.",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. Filter Periode Cepat (Urutan: Semua, Hari Ini, 7 Hari Terakhir, 30 Hari Terakhir, Bulan Ini, Pilih Tanggal)
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    PeriodeFilter.entries.forEach { filter ->
                        item(key = filter.name) {
                            val isSelected = state.selectedPeriode == filter

                            val chipText = if (filter == PeriodeFilter.KUSTOM) {
                                if (isSelected && state.customDateLabel.isNotBlank()) {
                                    state.customDateLabel
                                } else {
                                    "Pilih Tanggal"
                                }
                            } else {
                                filter.label
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (filter == PeriodeFilter.KUSTOM) {
                                        showDateRangePicker = true
                                    } else {
                                        viewModel.setPeriode(filter)
                                    }
                                },
                                leadingIcon = if (filter == PeriodeFilter.KUSTOM) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Pilih Tanggal",
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                } else null,
                                label = {
                                    Text(
                                        text = chipText,
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF374151),
                                    iconColor = Color(0xFF374151)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = Color.Transparent,
                                    borderColor = Color(0xFFE2E8F0),
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 0.dp
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            // ── 3. HERO CARD: TOTAL OMZET PENJUALAN
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF1E3A8A),
                                        Color(0xFF2563EB)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Top Row: Title & Range Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "TOTAL OMZET PENJUALAN",
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color.White.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = state.dateRangeLabel,
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Big Rupiah Value
                            Text(
                                text = formatRupiah(state.totalOmzet),
                                fontFamily = interfamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )

                            // Bottom Sub-metrics Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color(0xFF93C5FD),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Volume Penjualan",
                                                fontFamily = interfamily,
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.75f)
                                            )
                                            Text(
                                                text = "${state.totalTransaksiCount} Transaksi",
                                                fontFamily = interfamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = Color(0xFF86EFAC),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Rata-rata Order",
                                                fontFamily = interfamily,
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.75f)
                                            )
                                            Text(
                                                text = formatRupiah(state.rataRataTransaksi),
                                                fontFamily = interfamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 4. GRID KPI FINANSIAL LENGKAP (Termasuk LABA KOTOR & LABA BERSIH)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: Laba Kotor & Laba Bersih
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard(
                            title = "LABA KOTOR",
                            value = formatRupiah(state.totalLabaKotor),
                            badge = "Margin ${String.format(Locale.US, "%.1f", state.marginKotorPersen)}%",
                            icon = Icons.Default.BarChart,
                            accentColor = Color(0xFF16A34A),
                            containerColor = Color(0xFFF0FDF4),
                            borderColor = Color(0xFFBBF7D0),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "LABA BERSIH",
                            value = formatRupiah(state.totalLabaBersih),
                            badge = "Margin ${String.format(Locale.US, "%.1f", state.profitMarginPersen)}%",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            accentColor = Color(0xFF059669),
                            containerColor = Color(0xFFECFDF5),
                            borderColor = Color(0xFFA7F3D0),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Total Pesanan & Rata-rata Order
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard(
                            title = "TOTAL PESANAN",
                            value = "${state.totalTransaksiCount} Order",
                            badge = "100% Selesai",
                            icon = Icons.Default.ReceiptLong,
                            accentColor = Color(0xFF2563EB),
                            containerColor = Color(0xFFEFF6FF),
                            borderColor = Color(0xFFBFDBFE),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "TOTAL PPN",
                            value = formatRupiah(state.totalPajak),
                            badge = "Pajak Terkumpul",
                            icon = Icons.Default.AccountBalance,
                            accentColor = Color(0xFF7C3AED),
                            containerColor = Color(0xFFF5F3FF),
                            borderColor = Color(0xFFDDD6FE),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: Biaya Modal (HPP) & Diskon Promo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiCard(
                            title = "BIAYA MODAL (HPP)",
                            value = formatRupiah(state.totalHpp),
                            badge = "Beban Produk",
                            icon = Icons.Default.Storefront,
                            accentColor = Color(0xFF475569),
                            containerColor = Color(0xFFF8FAFC),
                            borderColor = Color(0xFFE2E8F0),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "DISKON & PROMO",
                            value = formatRupiah(state.totalDiskon),
                            badge = "Potongan Harga",
                            icon = Icons.Default.LocalOffer,
                            accentColor = Color(0xFFD97706),
                            containerColor = Color(0xFFFFFBEB),
                            borderColor = Color(0xFFFDE68A),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── 5. GRAFIK INTERAKTIF TREN PENJUALAN HARIAN
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Tren Penjualan Harian",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Klik batang untuk melihat rincian",
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            val peakDay = state.dailySales.maxByOrNull { it.totalOmzet }
                            if (peakDay != null && peakDay.totalOmzet > 0) {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Text(
                                        text = "Puncak: ${peakDay.dayLabel}",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1D4ED8),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Selected Bar Tooltip
                        AnimatedVisibility(visible = selectedBarIndex != null) {
                            selectedBarIndex?.let { idx ->
                                state.dailySales.getOrNull(idx)?.let { entry ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF0F172A)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${entry.dayLabel}, ${entry.dateLabel}",
                                                    fontFamily = interfamily,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                                Text(
                                                    text = formatRupiah(entry.totalOmzet),
                                                    fontFamily = interfamily,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(100.dp),
                                                    color = Color(0xFF1E293B)
                                                ) {
                                                    Text(
                                                        text = "${entry.jumlahTransaksi} Order",
                                                        fontFamily = interfamily,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF60A5FA),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { selectedBarIndex = null },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Tutup",
                                                        tint = Color(0xFF94A3B8),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bar Chart Canvas / Layout
                        val maxOmzet = state.dailySales.maxOfOrNull { it.totalOmzet }?.coerceAtLeast(1000.0) ?: 1000.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            state.dailySales.forEachIndexed { index, entry ->
                                val isSelected = selectedBarIndex == index
                                val heightFraction = (entry.totalOmzet / maxOmzet).toFloat().coerceIn(0.04f, 1f)
                                val animatedFraction by animateFloatAsState(
                                    targetValue = heightFraction,
                                    animationSpec = tween(600),
                                    label = "barHeight"
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            selectedBarIndex = if (isSelected) null else index
                                        },
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    // The Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(animatedFraction)
                                            .width(22.dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                if (isSelected) {
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                                                    )
                                                } else {
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))
                                                    )
                                                }
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Day Label
                                    Text(
                                        text = entry.dayLabel,
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 6. STRUKTUR LABA & BIAYA (P&L STATEMENT DENGAN LABA KOTOR)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Struktur Laba & Beban (P&L)",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )

                        PnLRow(
                            label = "Penjualan Kotor (Gross Sales)",
                            value = formatRupiah(state.totalOmzet + state.totalDiskon),
                            isPositive = true
                        )
                        PnLRow(
                            label = "Harga Pokok Penjualan (HPP / Modal)",
                            value = "- " + formatRupiah(state.totalHpp),
                            isPositive = false,
                            highlightRed = state.totalHpp > 0
                        )

                        // Highlight Subtotal Laba Kotor
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LABA KOTOR (GROSS PROFIT)",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF15803D)
                                )
                                Text(
                                    text = formatRupiah(state.totalLabaKotor),
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        PnLRow(
                            label = "Potongan Diskon & Promo",
                            value = "- " + formatRupiah(state.totalDiskon),
                            isPositive = false,
                            highlightRed = state.totalDiskon > 0
                        )
                        PnLRow(
                            label = "PPN / Pajak Terkumpul",
                            value = "+ " + formatRupiah(state.totalPajak),
                            isPositive = true
                        )

                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Highlight Laba Bersih Akhir
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "LABA BERSIH DITERIMA",
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF047857),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Setelah dikurangi modal, diskon & beban",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF065F46)
                                    )
                                }
                                Text(
                                    text = formatRupiah(state.totalLabaBersih),
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }
                }
            }

            // ── 7. DISTRIBUSI METODE PEMBAYARAN
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Metode Pembayaran",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )

                        if (state.metodePembayaranList.isEmpty()) {
                            Text(
                                text = "Belum ada transaksi pembayaran.",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        } else {
                            // Stacked Distribution Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(100.dp))
                            ) {
                                state.metodePembayaranList.forEachIndexed { index, item ->
                                    val barColor = when (index % 4) {
                                        0 -> Color(0xFF2563EB)
                                        1 -> Color(0xFF10B981)
                                        2 -> Color(0xFF8B5CF6)
                                        else -> Color(0xFFF59E0B)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(item.persentase.coerceAtLeast(1f))
                                            .fillMaxHeight()
                                            .background(barColor)
                                    )
                                }
                            }

                            // Items List
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                state.metodePembayaranList.forEachIndexed { index, item ->
                                    val icon = when {
                                        item.metode.contains("qris", true) -> Icons.Default.QrCode
                                        item.metode.contains("tunai", true) -> Icons.Default.Payments
                                        item.metode.contains("transfer", true) -> Icons.Default.AccountBalance
                                        else -> Icons.Default.CreditCard
                                    }
                                    val dotColor = when (index % 4) {
                                        0 -> Color(0xFF2563EB)
                                        1 -> Color(0xFF10B981)
                                        2 -> Color(0xFF8B5CF6)
                                        else -> Color(0xFFF59E0B)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = dotColor.copy(alpha = 0.12f),
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = dotColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = item.metode,
                                                    fontFamily = interfamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF1E293B)
                                                )
                                                Text(
                                                    text = "${item.jumlahTransaksi} transaksi • ${String.format(Locale.US, "%.1f", item.persentase)}%",
                                                    fontFamily = interfamily,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }

                                        Text(
                                            text = formatRupiah(item.totalNominal),
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 8. LEADERBOARD PRODUK TERLARIS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Produk Terlaris",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Berdasarkan volume penjualan",
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            TextButton(onClick = onLihatSemuaProduk) {
                                Text(
                                    text = "Lihat Semua",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }

                        if (state.topProdukList.isEmpty()) {
                            Text(
                                text = "Belum ada produk yang terjual.",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                state.topProdukList.forEach { produk ->
                                    TopProductItemRow(produk = produk)
                                }
                            }
                        }
                    }
                }
            }

            // ── 9. TOMBOL BESAR EKSPOR & BAGIKAN
            item {
                Button(
                    onClick = {
                        viewModel.exportLaporan(context) { uri ->
                            uri?.let { shareExcel(context, it) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    enabled = !state.isExporting
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (state.isExporting) "Sedang Mengekspor..." else "Ekspor & Bagikan Laporan (Excel)",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ── Komponen Reusable

@Composable
private fun KpiCard(
    title: String,
    value: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = containerColor,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = containerColor
                ) {
                    Text(
                        text = badge,
                        fontFamily = interfamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PnLRow(
    label: String,
    value: String,
    isPositive: Boolean,
    highlightRed: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = interfamily,
            fontSize = 13.sp,
            color = Color(0xFF475569)
        )
        Text(
            text = value,
            fontFamily = interfamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = when {
                highlightRed -> Color(0xFFDC2626)
                isPositive -> Color(0xFF0F172A)
                else -> Color(0xFF475569)
            }
        )
    }
}

@Composable
private fun TopProductItemRow(produk: TopProdukReport) {
    val rankColor = when (produk.rank) {
        1 -> Color(0xFFF59E0B) // Gold
        2 -> Color(0xFF64748B) // Silver
        3 -> Color(0xFFB45309) // Bronze
        else -> Color(0xFF94A3B8)
    }

    val rankBg = when (produk.rank) {
        1 -> Color(0xFFFEF3C7)
        2 -> Color(0xFFF1F5F9)
        3 -> Color(0xFFFFEDD5)
        else -> Color(0xFFF8FAFC)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Rank Badge
        Surface(
            shape = CircleShape,
            color = rankBg,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#${produk.rank}",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = rankColor
                )
            }
        }

        // Product Thumbnail
        AsyncImage(
            model = produk.imageUrl.ifBlank { R.drawable.kopi },
            contentDescription = produk.namaProduk,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
        )

        // Title and Units
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = produk.namaProduk,
                fontFamily = interfamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${produk.unitTerjual} terjual",
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "•",
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", produk.kontribusiPersen)}% kontribusi",
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2563EB)
                )
            }
        }

        // Revenue
        Text(
            text = formatRupiah(produk.totalOmzet),
            fontFamily = interfamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF0F172A)
        )
    }
}

// ── Helpers

private fun formatRupiah(nominal: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(nominal)
}

private fun shareExcel(context: Context, uri: Uri) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan Keuangan"))
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membagikan laporan: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun LaporanKeuanganScreenPreview() {
    LaporanKeuanganScreen(navController = rememberNavController())
}