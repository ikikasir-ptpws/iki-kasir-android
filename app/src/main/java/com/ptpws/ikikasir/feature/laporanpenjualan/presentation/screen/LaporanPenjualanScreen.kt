package com.ptpws.ikikasir.feature.laporanpenjualan.presentation.screen

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ptpws.ikikasir.commond.CustomDateRangePickerDialog
import com.ptpws.ikikasir.commond.LaporanKpiCardShimmer
import com.ptpws.ikikasir.commond.ProdukTerjualShimmerCard
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.ProdukTerjualItem
import com.ptpws.ikikasir.feature.laporanpenjualan.presentation.viewmodel.LaporanPenjualanViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanPenjualanScreen(
    navController: NavController,
    onBack: () -> Unit = { navController.popBackStack() },
    viewModel: LaporanPenjualanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showDateRangePicker by remember { mutableStateOf(false) }
    var showExportSuccessDialog by remember { mutableStateOf(false) }
    var showExportErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.exportedFileUri) {
        if (state.exportedFileUri != null) {
            showExportSuccessDialog = true
        }
    }

    LaunchedEffect(state.exportError) {
        if (!state.exportError.isNullOrBlank()) {
            showExportErrorDialog = true
        }
    }

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

    // Export Success Dialog
    if (showExportSuccessDialog && state.exportedFileUri != null) {
        val exportedUri = state.exportedFileUri
        AlertDialog(
            onDismissRequest = {
                showExportSuccessDialog = false
                viewModel.clearExportState()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(52.dp)
                )
            },
            title = {
                Text(
                    text = "Ekspor Excel Berhasil!",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column {
                    Text(
                        text = "File Excel laporan penjualan berhasil diunduh dan tersimpan di HP Anda.",
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📁 Lokasi Simpan (Berkas HP):",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Memori Internal > Download > IkiKasir",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF4F46E5)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Laporan: ${state.summary.filterLabel} (${state.summary.items.size} variasi produk)",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (exportedUri != null) {
                            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(
                                    exportedUri,
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                )
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            val chooser = Intent.createChooser(openIntent, "Buka dengan")
                            try {
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Tidak ada aplikasi untuk membuka file Excel",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        showExportSuccessDialog = false
                        viewModel.clearExportState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Buka File", fontFamily = interfamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportSuccessDialog = false
                        viewModel.clearExportState()
                    }
                ) {
                    Text("Tutup", fontFamily = interfamily, color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Export Error Dialog
    if (showExportErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportErrorDialog = false
                viewModel.clearExportState()
            },
            title = {
                Text(
                    text = "Gagal Ekspor",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            },
            text = {
                Text(
                    text = state.exportError ?: "Terjadi kesalahan saat mengekspor laporan.",
                    fontFamily = interfamily,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportErrorDialog = false
                        viewModel.clearExportState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mengerti", fontFamily = interfamily)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Laporan Penjualan",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF0F172A),
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.exportToExcel(context) },
                        enabled = !state.isExporting && state.summary.items.isNotEmpty()
                    ) {
                        if (state.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color(0xFF4F46E5),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Ekspor Excel",
                                tint = if (state.summary.items.isNotEmpty()) Color(0xFF4F46E5) else Color(0xFFCBD5E1)
                            )
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Search Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (state.searchQuery.isEmpty()) {
                                Text(
                                    text = "Cari produk terjual atau kategori...",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            BasicTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontFamily = interfamily
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onSearchQueryChange("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Periode Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(PeriodeLaporanPenjualan.values()) { periode ->
                        val isSelected = state.selectedPeriode == periode
                        val chipText = if (periode == PeriodeLaporanPenjualan.KUSTOM) {
                            state.customDateLabel ?: "Pilih Tanggal"
                        } else {
                            periode.displayName
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (periode == PeriodeLaporanPenjualan.KUSTOM) {
                                    showDateRangePicker = true
                                } else {
                                    viewModel.setPeriode(periode)
                                }
                            },
                            leadingIcon = if (periode == PeriodeLaporanPenjualan.KUSTOM) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
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
                                selectedContainerColor = Color(0xFF4F46E5),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF374151)
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

            // KPI Summary Cards
            item {
                if (state.isLoading && state.summary.totalUnitTerjual == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LaporanKpiCardShimmer(modifier = Modifier.weight(1f))
                            LaporanKpiCardShimmer(modifier = Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LaporanKpiCardShimmer(modifier = Modifier.weight(1f))
                            LaporanKpiCardShimmer(modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LaporanKpiCard(
                                modifier = Modifier.weight(1f),
                                title = "TOTAL TERJUAL",
                                value = "${formatNumber(state.summary.totalUnitTerjual)} Unit",
                                icon = Icons.Default.Inventory2,
                                iconBg = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5)
                            )
                            LaporanKpiCard(
                                modifier = Modifier.weight(1f),
                                title = "TOTAL OMZET",
                                value = formatRupiah(state.summary.totalOmzet),
                                icon = Icons.Default.TrendingUp,
                                iconBg = Color(0xFFECFDF5),
                                iconTint = Color(0xFF10B981)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LaporanKpiCard(
                                modifier = Modifier.weight(1f),
                                title = "VARIASI PRODUK",
                                value = "${state.summary.totalProdukUnik} Produk",
                                icon = Icons.Default.ShoppingBag,
                                iconBg = Color(0xFFFFFBEB),
                                iconTint = Color(0xFFF59E0B)
                            )
                            LaporanKpiCard(
                                modifier = Modifier.weight(1f),
                                title = "TERLARIS (#1)",
                                value = if (state.summary.produkTerlarisQty > 0) "${state.summary.produkTerlarisNama} (${state.summary.produkTerlarisQty})" else "-",
                                icon = Icons.Default.Star,
                                iconBg = Color(0xFFFDF2F8),
                                iconTint = Color(0xFFEC4899)
                            )
                        }
                    }
                }
            }

            // Subheader
            item {
                Text(
                    text = if (state.isLoading) "Memuat Produk Terjual..." else "Rincian Produk Terjual (${state.displayedItems.size})",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Product List
            if (state.isLoading) {
                items(5) {
                    ProdukTerjualShimmerCard()
                }
            } else if (state.displayedItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (state.searchQuery.isNotBlank()) "Tidak ada produk yang cocok dengan pencarian" else "Belum ada produk yang terjual pada periode ini",
                                fontFamily = interfamily,
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(state.displayedItems, key = { "${it.productId}_${it.namaProduk}" }) { item ->
                    ProdukTerjualCard(item = item)
                }
            }
        }
    }
}

@Composable
fun LaporanKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
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

@Composable
fun ProdukTerjualCard(item: ProdukTerjualItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge
                val (rankBg, rankText) = when (item.rank) {
                    1 -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706)) // Gold
                    2 -> Pair(Color(0xFFF1F5F9), Color(0xFF475569)) // Silver
                    3 -> Pair(Color(0xFFFFEDD5), Color(0xFFEA580C)) // Bronze
                    else -> Pair(Color(0xFFF8FAFC), Color(0xFF64748B))
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(rankBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${item.rank}",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = rankText
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Product Image Thumbnail
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.namaProduk,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                // Name & Category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.namaProduk,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = item.kategori,
                                fontFamily = interfamily,
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (item.hargaSatuan > 0) {
                            Text(
                                text = "@ ${formatRupiah(item.hargaSatuan)}",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Unit Sold & Omzet
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = "${item.unitTerjual} Unit",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF4F46E5),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = formatRupiah(item.totalOmzet),
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            // Sales Contribution Progress Bar
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LinearProgressIndicator(
                    progress = { (item.kontribusiPersen / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF4F46E5),
                    trackColor = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(Locale.US, "%.1f%% dari total omzet", item.kontribusiPersen),
                    fontFamily = interfamily,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

private fun formatRupiah(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = if (value % 1.0 == 0.0) 0 else 2
    }
    return "Rp ${formatter.format(value)}"
}

private fun formatNumber(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(value)
}
