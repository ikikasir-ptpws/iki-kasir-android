package com.example.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.screens.navigation.AppScreen

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.PeriodeLaporanPenjualan
import com.ptpws.ikikasir.commond.CustomDateRangePickerDialog

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.ptpws.ikikasir.commond.DashboardHeaderShimmer
import com.ptpws.ikikasir.commond.DashboardFilterRowShimmer
import com.ptpws.ikikasir.commond.DashboardStatCardsShimmer
import com.ptpws.ikikasir.commond.DashboardMenuSectionShimmer
import com.ptpws.ikikasir.commond.DashboardRingkasanShimmer

import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardUiProvider = HiltDashboardUiProvider(hiltViewModel<DashboardViewModel>())
) {
    val sessionState by viewModel.sessionState.collectAsState()
    val analyticsState by viewModel.analyticsState.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isShimmerActive = isInitialLoading || isRefreshing

    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val customStartDateMillis by viewModel.customStartDateMillis.collectAsState()
    val customEndDateMillis by viewModel.customEndDateMillis.collectAsState()
    val customDateLabel by viewModel.customDateLabel.collectAsState()
    val isBalanceVisible by viewModel.isBalanceVisible.collectAsState()
    var showDateRangePicker by remember { mutableStateOf(false) }
    val userName = sessionState.user?.fullName?.ifBlank { "Pengguna" } ?: "Pengguna"

    val context = androidx.compose.ui.platform.LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }

    androidx.activity.compose.BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000L) {
            (context as? android.app.Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            android.widget.Toast.makeText(context, "Tekan sekali lagi untuk keluar", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (showDateRangePicker) {
        CustomDateRangePickerDialog(
            initialStartDateMillis = customStartDateMillis,
            initialEndDateMillis = customEndDateMillis,
            onDismissRequest = { showDateRangePicker = false },
            onDateRangeSelected = { start, end ->
                viewModel.setCustomDateRange(start, end)
                showDateRangePicker = false
            }
        )
    }

    val showProduk = viewModel.isAllowed("Produk")
    val showKategori = viewModel.isAllowed("Kategori Produk")
    val showStok = viewModel.isAllowed("Manajemen Stok")
    val hasProdukSection = showProduk || showKategori || showStok

    val showKasir = viewModel.isAllowed("Kasir")
    val showTransaksi = viewModel.isAllowed("Transaksi")
    val showAntrean = viewModel.isAllowed("Antrean")
    val showRiwayatAntrean = viewModel.isAllowed("Riwayat Antrean")
    val showPromo = viewModel.isAllowed("Promo")
    val hasPenjualanSection = showKasir || showTransaksi || showAntrean || showRiwayatAntrean || showPromo
    val showLaporanKeuangan = viewModel.isAllowed("Laporan Keuangan")

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val windowSizeInfo = com.ptpws.ikikasir.core.responsive.presentation.util.LocalWindowSizeInfo.current
    val isWideScreen = windowSizeInfo.shouldUseNavigationRail || windowSizeInfo.shouldUsePermanentDrawer

    Scaffold(
        containerColor = Color(0xFFF0F4FF),
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshDashboard() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F4FF)),
                contentPadding = PaddingValues(bottom = if (isWideScreen) 56.dp else 128.dp)
            ) {
                if (isShimmerActive) {
                    item { DashboardHeaderShimmer(isSmallScreen = isSmallScreen) }
                    item { DashboardFilterRowShimmer(isSmallScreen = isSmallScreen) }
                    item { DashboardStatCardsShimmer(isSmallScreen = isSmallScreen) }

                    if (!isWideScreen) {
                        item { SectionHeader(title = "Produk", onLihatSemua = null) }
                        item { DashboardMenuSectionShimmer(itemCount = 3, isSmallScreen = isSmallScreen) }
                        item { SectionHeader(title = "Penjualan", onLihatSemua = null) }
                        item { DashboardMenuSectionShimmer(itemCount = 4, isSmallScreen = isSmallScreen) }
                    }

                    item { SectionHeader(title = "Total Penjualan", onLihatSemua = null) }
                    item { DashboardRingkasanShimmer(isSmallScreen = isSmallScreen) }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                } else {
                    item { HeaderSection(userName = userName) }
                    item {
                        DashboardFilterRow(
                            selectedFilter = selectedFilter,
                            customDateLabel = customDateLabel,
                            onFilterSelected = { viewModel.setFilter(it) },
                            onOpenDatePicker = { showDateRangePicker = true }
                        )
                    }
                    item {
                        StatCardsSection(
                            analytics = analyticsState,
                            selectedFilter = selectedFilter,
                            customDateLabel = customDateLabel,
                            isBalanceVisible = isBalanceVisible,
                            onToggleVisibility = { viewModel.toggleBalanceVisibility() },
                            onPenjualanClick = {
                                val route = if (selectedFilter == PeriodeLaporanPenjualan.KUSTOM &&
                                    customStartDateMillis != null && customEndDateMillis != null) {
                                    AppScreen.LaporanPenjualan.routeWith(
                                        periode = "KUSTOM",
                                        startDate = customStartDateMillis,
                                        endDate = customEndDateMillis
                                    )
                                } else {
                                    AppScreen.LaporanPenjualan.routeWith(selectedFilter.name)
                                }
                                navController.navigate(route)
                            },
                            onTransaksiClick = {
                                val route = if (selectedFilter == PeriodeLaporanPenjualan.KUSTOM &&
                                    customStartDateMillis != null && customEndDateMillis != null) {
                                    AppScreen.Riwayat.routeWith(
                                        initialFilter = "Filter Tanggal",
                                        startDate = customStartDateMillis,
                                        endDate = customEndDateMillis
                                    )
                                } else {
                                    val riwayatFilter = when (selectedFilter) {
                                        PeriodeLaporanPenjualan.HARI_INI -> "Hari Ini"
                                        PeriodeLaporanPenjualan.TUJUH_HARI -> "7 Hari Terakhir"
                                        PeriodeLaporanPenjualan.TIGA_PULUH_HARI -> "30 Hari Terakhir"
                                        PeriodeLaporanPenjualan.BULAN_INI -> "Bulan Ini"
                                        PeriodeLaporanPenjualan.SEMUA -> "Semua"
                                        PeriodeLaporanPenjualan.KUSTOM -> "Filter Tanggal"
                                    }
                                    AppScreen.Riwayat.routeWith(initialFilter = riwayatFilter)
                                }
                                navController.navigate(route)
                            }
                        )
                    }

                    val navigateToSemuaMenu = { navController.navigate(AppScreen.Semuamenu.route) }

                    // Menu Produk dan Penjualan (Gambar 1) tidak ditampilkan pada tablet layar lebar
                    // karena seluruh sub-menunya sudah dipindahkan secara lengkap ke sidebar navigasi.
                    if (!isWideScreen && hasProdukSection) {
                        item { SectionHeader(title = "Produk", onLihatSemua = navigateToSemuaMenu) }
                        item {
                            ProdukMenuSection(
                                navController = navController,
                                showProduk = showProduk,
                                showKategori = showKategori,
                                showStok = showStok
                            )
                        }
                    }

                    if (!isWideScreen && hasPenjualanSection) {
                        val lihatSemuaAction = if (!hasProdukSection) navigateToSemuaMenu else null
                        item { SectionHeader(title = "Penjualan", onLihatSemua = lihatSemuaAction) }
                        item {
                            PenjualanMenuSection(
                                navController = navController,
                                showKasir = showKasir,
                                showTransaksi = showTransaksi,
                                showAntrean = showAntrean,
                                showRiwayatAntrean = showRiwayatAntrean,
                                showPromo = showPromo
                            )
                        }
                    }

                    item {
                        SectionHeader(
                            title = "Total Penjualan",
                            onLihatSemua = {
                                val route = if (selectedFilter == PeriodeLaporanPenjualan.KUSTOM &&
                                    customStartDateMillis != null && customEndDateMillis != null) {
                                    AppScreen.LaporanPenjualan.routeWith(
                                        periode = "KUSTOM",
                                        startDate = customStartDateMillis,
                                        endDate = customEndDateMillis
                                    )
                                } else {
                                    AppScreen.LaporanPenjualan.routeWith(selectedFilter.name)
                                }
                                navController.navigate(route)
                            }
                        )
                    }
                    item {
                        RingkasanSection(
                            analytics = analyticsState,
                            selectedFilter = selectedFilter
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String = "Pengguna") {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val headerHorizontalPadding = if (isSmallScreen) 14.dp else 20.dp
    val headerVerticalPadding = if (isSmallScreen) 14.dp else 20.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = headerHorizontalPadding, vertical = headerVerticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            // Responsif: batasi lebar kolom teks agar nama panjang tidak menabrak ikon notifikasi
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Text(
                text = "Selamat datang,",
                fontSize = if (isSmallScreen) 13.sp else 14.sp,
                fontFamily = interfamily,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = userName,
                fontSize = if (isSmallScreen) 18.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1D2E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Kelola bisnis Anda dengan mudah",
                fontSize = if (isSmallScreen) 12.sp else 13.sp,
                fontFamily = interfamily,
                color = Color(0xFF8A8FA8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .size(if (isSmallScreen) 38.dp else 44.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.notif),
                contentDescription = "Notifikasi",
                modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun DashboardFilterRow(
    selectedFilter: PeriodeLaporanPenjualan,
    customDateLabel: String,
    onFilterSelected: (PeriodeLaporanPenjualan) -> Unit,
    onOpenDatePicker: () -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val sectionHorizontalPadding = if (isSmallScreen) 12.dp else 20.dp
    var expanded by remember { mutableStateOf(false) }

    val filterOptions = listOf(
        PeriodeLaporanPenjualan.HARI_INI,
        PeriodeLaporanPenjualan.TUJUH_HARI,
        PeriodeLaporanPenjualan.TIGA_PULUH_HARI,
        PeriodeLaporanPenjualan.BULAN_INI,
        PeriodeLaporanPenjualan.SEMUA,
        PeriodeLaporanPenjualan.KUSTOM
    )

    val chipText = if (selectedFilter == PeriodeLaporanPenjualan.KUSTOM) {
        if (customDateLabel.isNotBlank()) customDateLabel else "Pilih Tanggal"
    } else {
        selectedFilter.displayName
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sectionHorizontalPadding)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Responsif: batasi lebar chip agar label tanggal panjang tidak keluar dari layar
        Box(modifier = Modifier.weight(1f, fill = false)) {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Filter Periode",
                        tint = Color(0xFF3D5AF1),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = chipText,
                        fontSize = if (isSmallScreen) 11.5.sp else 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Buka Filter",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .widthIn(min = 160.dp, max = 280.dp)
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    val itemText = if (filter == PeriodeLaporanPenjualan.KUSTOM) {
                        "Pilih Tanggal"
                    } else {
                        filter.displayName
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemText,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF3D5AF1) else Color(0xFF1E293B),
                                fontFamily = interfamily
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Dipilih",
                                    tint = Color(0xFF3D5AF1),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            expanded = false
                            if (filter == PeriodeLaporanPenjualan.KUSTOM) {
                                onOpenDatePicker()
                            } else {
                                onFilterSelected(filter)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCardsSection(
    analytics: DashboardAnalytics = DashboardAnalytics(),
    selectedFilter: PeriodeLaporanPenjualan = PeriodeLaporanPenjualan.HARI_INI,
    customDateLabel: String = "",
    isBalanceVisible: Boolean = true,
    onToggleVisibility: (() -> Unit)? = null,
    onPenjualanClick: (() -> Unit)? = null,
    onTransaksiClick: (() -> Unit)? = null,
    // Legacy single-click kept for Preview compatibility
    onCardClick: (() -> Unit)? = null
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val sectionHorizontalPadding = if (isSmallScreen) 12.dp else 20.dp
    val cardPadding = if (isSmallScreen) 12.dp else 16.dp
    val cardSpacing = if (isSmallScreen) 10.dp else 12.dp
    val cardCornerRadius = if (isSmallScreen) 16.dp else 20.dp
    val iconBoxSize = if (isSmallScreen) 32.dp else 36.dp

    val formattedPenjualan = formatDashboardNominal(analytics.penjualanHariIni)
    val nominalDigits = formattedPenjualan.removePrefix("Rp ")

    // Lebar area teks nominal diukur setelah layout (Box.onSizeChanged).
    // BoxWithConstraints tidak bisa dipakai di dalam Row ber-IntrinsicSize.Min
    // (SubcomposeLayout tidak mendukung pengukuran intrinsik).
    var nominalWidthPx by remember { mutableStateOf(0f) }
    val fallbackNominalWidthPx = with(LocalDensity.current) {
        val cardWidthDp = configuration.screenWidthDp -
            sectionHorizontalPadding.value * 2 - cardSpacing.value
        val innerWidthDp = cardWidthDp / 2f - cardPadding.value * 2
        if (innerWidthDp > 0f) innerWidthDp.dp.toPx() else 0f
    }
    val salesGrowthPercent = formatDashboardPercent(analytics.pertumbuhanPenjualanPersen)
    val salesGrowthText = if (analytics.pertumbuhanPenjualanPersen == 0.0) {
        "0%"
    } else if (analytics.isPertumbuhanPenjualanPositif) {
        "+$salesGrowthPercent%"
    } else {
        "-$salesGrowthPercent%"
    }

    val txGrowthPercent = formatDashboardPercent(analytics.pertumbuhanTransaksiPersen)
    val txGrowthText = if (analytics.pertumbuhanTransaksiPersen == 0.0) {
        "0%"
    } else if (analytics.isPertumbuhanTransaksiPositif) {
        "+$txGrowthPercent%"
    } else {
        "-$txGrowthPercent%"
    }

    val salesCardTitle = when (selectedFilter) {
        PeriodeLaporanPenjualan.HARI_INI -> "PENJUALAN HARI INI"
        PeriodeLaporanPenjualan.TUJUH_HARI -> "PENJUALAN 7 HARI"
        PeriodeLaporanPenjualan.TIGA_PULUH_HARI -> "PENJUALAN 30 HARI"
        PeriodeLaporanPenjualan.BULAN_INI -> "PENJUALAN BULAN INI"
        PeriodeLaporanPenjualan.KUSTOM -> if (customDateLabel.isNotBlank()) "PENJUALAN ($customDateLabel)" else "PENJUALAN KUSTOM"
        else -> "TOTAL PENJUALAN"
    }

    val periodVsLabel = when (selectedFilter) {
        PeriodeLaporanPenjualan.HARI_INI -> "dari kemarin"
        PeriodeLaporanPenjualan.TUJUH_HARI -> "vs 7 hr lalu"
        PeriodeLaporanPenjualan.TIGA_PULUH_HARI -> "vs 30 hr lalu"
        PeriodeLaporanPenjualan.BULAN_INI -> "vs bln lalu"
        PeriodeLaporanPenjualan.KUSTOM -> "vs periode lalu"
        else -> "keseluruhan"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = sectionHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing)
    ) {
        // Card Penjualan Hari Ini — gradient biru
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .then(
                    if (onPenjualanClick != null) Modifier.clickable { onPenjualanClick() }
                    else if (onCardClick != null) Modifier.clickable { onCardClick() }
                    else Modifier
                ),
            shape = RoundedCornerShape(cardCornerRadius),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF3D5AF1),
                                Color(0xFF6C8EF5)
                            )
                        )
                    )
                    .padding(cardPadding)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.grafikicon),
                            contentDescription = "Grafik",
                            modifier = Modifier
                                .size(iconBoxSize)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                        if (onToggleVisibility != null) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { onToggleVisibility() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isBalanceVisible) "Sembunyikan saldo" else "Tampilkan saldo",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = salesCardTitle,
                            fontSize = if (isSmallScreen) 9.5.sp else 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f),
                            // Responsif: judul boleh wrap, tidak boleh terpotong
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = "ⓘ",
                            fontSize = if (isSmallScreen) 9.sp else 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isBalanceVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged { nominalWidthPx = it.width.toFloat() }
                        ) {
                            FittedNominalText(
                                value = nominalDigits,
                                availableWidthPx = if (nominalWidthPx > 0) nominalWidthPx else fallbackNominalWidthPx,
                                prefixFontSize = if (isSmallScreen) 12.sp else 14.sp,
                                valueFontSize = if (isSmallScreen) {
                                    if (nominalDigits.length > 8) 16.sp else 20.sp
                                } else {
                                    if (nominalDigits.length > 10) 19.sp else 24.sp
                                },
                                color = Color.White
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged { nominalWidthPx = it.width.toFloat() }
                        ) {
                            FittedNominalText(
                                value = "••••••",
                                availableWidthPx = if (nominalWidthPx > 0) nominalWidthPx else fallbackNominalWidthPx,
                                prefixFontSize = if (isSmallScreen) 12.sp else 14.sp,
                                valueFontSize = if (isSmallScreen) 20.sp else 24.sp,
                                color = Color.White,
                                valueLetterSpacing = 4.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isSmallScreen) 6.dp else 8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(
                                    horizontal = if (isSmallScreen) 6.dp else 8.dp,
                                    vertical = if (isSmallScreen) 2.dp else 3.dp
                                )
                        ) {
                            Text(
                                text = salesGrowthText,
                                fontSize = if (isSmallScreen) 9.5.sp else 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                // Responsif: badge tidak boleh terpotong/berbaris dua
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(3.dp))

                        Text(
                            text = periodVsLabel,
                            fontSize = if (isSmallScreen) 9.5.sp else 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Card Total Transaksi
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .then(
                    if (onTransaksiClick != null) Modifier.clickable { onTransaksiClick() }
                    else if (onCardClick != null) Modifier.clickable { onCardClick() }
                    else Modifier
                ),
            shape = RoundedCornerShape(cardCornerRadius),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(cardPadding)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.penjualan),
                    contentDescription = "Transaksi",
                    modifier = Modifier
                        .size(iconBoxSize)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDBEAFE))
                        .padding(6.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "TOTAL TRANSAKSI",
                        fontSize = if (isSmallScreen) 9.5.sp else 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8A8FA8),
                        // Responsif: judul boleh wrap, tidak boleh terpotong
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "ⓘ",
                        fontSize = if (isSmallScreen) 9.sp else 10.sp,
                        color = Color(0xFF8A8FA8)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${analytics.totalTransaksiHariIni} trx",
                    fontSize = if (isSmallScreen) 18.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D2E)
                )

                Spacer(modifier = Modifier.height(if (isSmallScreen) 6.dp else 8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val badgeColor = if (analytics.isPertumbuhanTransaksiPositif) Color(0xFF22C55E) else Color(0xFFEF4444)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(
                                horizontal = if (isSmallScreen) 6.dp else 8.dp,
                                vertical = if (isSmallScreen) 2.dp else 3.dp
                            )
                    ) {
                        Text(
                            text = txGrowthText,
                            fontSize = if (isSmallScreen) 9.5.sp else 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor,
                            // Responsif: badge tidak boleh terpotong/berbaris dua
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = periodVsLabel,
                        fontSize = if (isSmallScreen) 9.5.sp else 11.sp,
                        color = Color(0xFF8A8FA8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onLihatSemua: (() -> Unit)? = null) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val headerHorizontalPadding = if (isSmallScreen) 14.dp else 20.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = headerHorizontalPadding, vertical = if (isSmallScreen) 12.dp else 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = if (isSmallScreen) 16.sp else 18.sp,
            fontFamily = interfamily,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1D2E),
            // Responsif: judul panjang dipotong agar tidak menabrak "Lihat Semua"
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (onLihatSemua != null) {
            Text(
                text = "Lihat Semua",
                fontSize = if (isSmallScreen) 12.sp else 13.sp,
                color = Color(0xFF3D5AF1),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onLihatSemua() }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProdukMenuSection(
    navController: NavController,
    showProduk: Boolean = true,
    showKategori: Boolean = true,
    showStok: Boolean = true
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val sectionHorizontalPadding = if (isSmallScreen) 14.dp else 20.dp
    val itemSpacing = if (isSmallScreen) 16.dp else 28.dp

    val items = mutableListOf<@Composable () -> Unit>()
    if (showProduk) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.produk,
                label = "Produk",
                bgColor = Color(0xFFF0FDF4),
                onClick = { navController.navigate(AppScreen.Produk.baseRoute) }
            )
        }
    }
    if (showKategori) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.kategori,
                label = "Kategori\nProduk",
                bgColor = Color(0xFFFAF5FF),
                onClick = { navController.navigate(AppScreen.KategoriProduk.route) }
            )
        }
    }
    if (showStok) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.manajemenstok,
                label = "Manajemen\nStok",
                bgColor = Color(0xFFFFF7ED),
                onClick = { navController.navigate(AppScreen.ManajemenStok.route) }
            )
        }
    }

    if (items.isEmpty()) return

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sectionHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { it() }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PenjualanMenuSection(
    navController: NavController,
    showKasir: Boolean = true,
    showTransaksi: Boolean = true,
    showAntrean: Boolean = true,
    showRiwayatAntrean: Boolean = true,
    showPromo: Boolean = true,
    showLaporanPenjualan: Boolean = false
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val sectionHorizontalPadding = if (isSmallScreen) 14.dp else 20.dp
    val itemSpacing = if (isSmallScreen) 16.dp else 28.dp

    val items = mutableListOf<@Composable () -> Unit>()
    if (showKasir) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.kasirmenu,
                label = "Kasir",
                bgColor = Color(0xFFEFF6FF),
                onClick = { navController.navigate(AppScreen.Kasir.route) }
            )
        }
    }
    if (showTransaksi) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.transaksi,
                label = "Transaksi",
                bgColor = Color(0xFFECFEFF),
                onClick = { navController.navigate(AppScreen.Riwayat.baseRoute) }
            )
        }
    }
    if (showAntrean) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.waitinglist,
                label = "Antrean",
                bgColor = Color(0xFFECFEFF),
                onClick = { navController.navigate(AppScreen.WaitingList.route) }
            )
        }
    }
    if (showRiwayatAntrean) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.riwayatantrean,
                label = "Riwayat\nAntrean",
                bgColor = Color(0xFFEFF6FF),
                onClick = { navController.navigate(AppScreen.RiwayatAntrean.route) }
            )
        }
    }
    if (showPromo) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.promo,
                label = "Promo",
                bgColor = Color(0xFFFFFBEB),
                onClick = { navController.navigate(AppScreen.Promo.route) }
            )
        }
    }
    if (showLaporanPenjualan) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.laporanpenjualan,
                label = "Laporan\nPenjualan",
                bgColor = Color(0xFFECFEFF),
                onClick = { navController.navigate(AppScreen.LaporanPenjualan.route) }
            )
        }
    }

    if (items.isEmpty()) return

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sectionHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { it() }
    }
}

@Composable
fun MenuIconItem(
    iconRes: Int,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val itemWidth = if (isSmallScreen) 74.dp else 80.dp
    val cardSize = if (isSmallScreen) 54.dp else 60.dp
    val iconSize = if (isSmallScreen) 22.dp else 25.dp
    val fontSize = if (isSmallScreen) 11.sp else 12.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(itemWidth)
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .size(cardSize)
                .shadow(
                    elevation = if (isSmallScreen) 4.dp else 5.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = bgColor
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(iconSize),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isSmallScreen) 6.dp else 8.dp))

        Text(
            text = label,
            fontSize = fontSize,
            fontFamily = interfamily,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = if (isSmallScreen) 14.sp else 16.sp,
            // Responsif: label menu maksimal 2 baris, sisa dipotong dengan ellipsis
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MenuIconItem(
    iconVector: ImageVector,
    iconTint: Color,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val itemWidth = if (isSmallScreen) 74.dp else 80.dp
    val cardSize = if (isSmallScreen) 54.dp else 60.dp
    val iconSize = if (isSmallScreen) 22.dp else 25.dp
    val fontSize = if (isSmallScreen) 11.sp else 12.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(itemWidth)
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .size(cardSize)
                .shadow(
                    elevation = if (isSmallScreen) 4.dp else 5.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = bgColor
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isSmallScreen) 6.dp else 8.dp))

        Text(
            text = label,
            fontSize = fontSize,
            fontFamily = interfamily,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = if (isSmallScreen) 14.sp else 16.sp,
            // Responsif: label menu maksimal 2 baris, sisa dipotong dengan ellipsis
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun RingkasanSection(
    analytics: DashboardAnalytics = DashboardAnalytics(),
    selectedFilter: PeriodeLaporanPenjualan = PeriodeLaporanPenjualan.HARI_INI
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val sectionHorizontalPadding = if (isSmallScreen) 12.dp else 20.dp

    val vsLabel = when (selectedFilter) {
        PeriodeLaporanPenjualan.HARI_INI -> "vs kmrn"
        PeriodeLaporanPenjualan.TUJUH_HARI -> "vs 7 hr"
        PeriodeLaporanPenjualan.TIGA_PULUH_HARI -> "vs 30 hr"
        PeriodeLaporanPenjualan.BULAN_INI -> "vs bln lalu"
        PeriodeLaporanPenjualan.KUSTOM -> "vs lalu"
        else -> "total"
    }

    val custGrowthPercent = formatDashboardPercent(analytics.pertumbuhanPelangganPersen)
    val custGrowthText = if (analytics.pertumbuhanPelangganPersen == 0.0) {
        "0% $vsLabel"
    } else {
        "${custGrowthPercent}% $vsLabel"
    }

    val soldGrowthPercent = formatDashboardPercent(analytics.pertumbuhanProdukTerjualPersen)
    val soldGrowthText = if (analytics.pertumbuhanProdukTerjualPersen == 0.0) {
        "0% $vsLabel"
    } else {
        "${soldGrowthPercent}% $vsLabel"
    }

    val formattedCust = formatDashboardInteger(analytics.totalPelangganHariIni)
    val formattedSold = formatDashboardInteger(analytics.totalProdukTerjualHariIni)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = sectionHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 12.dp)
    ) {
        RingkasanCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            value = formattedCust,
            title = "Pelanggan",
            growth = custGrowthText,
            isGrowthPositive = analytics.isPertumbuhanPelangganPositif,
            icon = Icons.Outlined.Person,
            iconColor = Color(0xFF22C55E),
            iconBg = Color(0xFFDCFCE7),
            isSmallScreen = isSmallScreen
        )

        RingkasanCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            value = formattedSold,
            title = "Terjual",
            growth = soldGrowthText,
            isGrowthPositive = analytics.isPertumbuhanProdukTerjualPositif,
            icon = Icons.Outlined.Inventory2,
            iconColor = Color(0xFFF97316),
            iconBg = Color(0xFFFFEDD5),
            isSmallScreen = isSmallScreen
        )
    }
}

private fun formatDashboardNominal(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    return "Rp ${formatter.format(value)}"
}

private fun formatDashboardInteger(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(value)
}

private fun formatDashboardPercent(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 1
    }
    return formatter.format(value)
}

/**
 * Menampilkan nominal ("Rp 1.250.000") sebagai satu Text satu baris.
 * "Rp" digabung dengan angkanya lewat span agar hierarki tipografi desain tetap terjaga,
 * dan font diperkecil otomatis bila lebar card tidak cukup (tidak pernah dipotong).
 */
@Composable
private fun FittedNominalText(
    value: String,
    availableWidthPx: Float,
    prefixFontSize: TextUnit,
    valueFontSize: TextUnit,
    color: Color,
    valueLetterSpacing: TextUnit? = null
) {
    val density = LocalDensity.current
    val scale = remember(value, availableWidthPx, prefixFontSize, valueFontSize, valueLetterSpacing, density.fontScale) {
        if (availableWidthPx <= 0f) {
            1f
        } else {
            val prefixPx = with(density) { prefixFontSize.toPx() }
            val valuePx = with(density) { valueFontSize.toPx() }
            val letterSpacingPx = valueLetterSpacing?.let { with(density) { it.toPx() } } ?: 0f
            val prefixWidthPx = "Rp ".fold(0f) { acc, c -> acc + estimateCharWidthEm(c) } * prefixPx
            val valueWidthPx = value.fold(0f) { acc, c -> acc + estimateCharWidthEm(c) } * valuePx +
                letterSpacingPx * value.length
            val estimatedPx = prefixWidthPx + valueWidthPx
            // margin 2% supaya teks tidak pernah menyentuh tepi card
            val usablePx = availableWidthPx * 0.98f
            if (estimatedPx <= 0f) 1f else (usablePx / estimatedPx).coerceIn(0.45f, 1f)
        }
    }

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontSize = prefixFontSize * scale, fontWeight = FontWeight.Medium)) {
                append("Rp ")
            }
            withStyle(
                SpanStyle(
                    fontSize = valueFontSize * scale,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = valueLetterSpacing ?: TextUnit.Unspecified
                )
            ) {
                append(value)
            }
        },
        color = color,
        maxLines = 1,
        softWrap = false
    )
}

/** Perkiraan lebar karakter dalam satuan em, dipakai untuk menghitung skala font nominal. */
private fun estimateCharWidthEm(char: Char): Float = when {
    char.isDigit() -> 0.62f
    char == '.' || char == ',' -> 0.30f
    char == ' ' -> 0.26f
    char == '•' -> 0.45f
    else -> 0.60f
}

@Composable
fun RingkasanCard(
    modifier: Modifier = Modifier,
    value: String,
    title: String,
    growth: String,
    isGrowthPositive: Boolean,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    isSmallScreen: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(if (isSmallScreen) 16.dp else 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSmallScreen) 3.dp else 5.dp
        )
    ) {




        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isSmallScreen) 10.dp else 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(if (isSmallScreen) 34.dp else 38.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
                )
            }

            Spacer(modifier = Modifier.width(if (isSmallScreen) 8.dp else 10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = value,
                    fontSize = if (isSmallScreen) 18.sp else 22.sp,
                    fontFamily = interfamily,
                    lineHeight = if (isSmallScreen) 20.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D2E)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = title,
                    fontFamily = interfamily,
                    fontSize = if (isSmallScreen) 11.sp else 12.sp,
                    lineHeight = 14.sp,
                    color = Color(0xFF8A8FA8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Icon(
                        imageVector = if (isGrowthPositive)
                            Icons.Filled.ArrowUpward
                        else
                            Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = if (isGrowthPositive)
                            Color(0xFF22C55E)
                        else
                            Color(0xFFEF4444),
                        modifier = Modifier.size(if (isSmallScreen) 10.dp else 11.dp)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = growth,
                        fontSize = if (isSmallScreen) 9.5.sp else 10.5.sp,
                        fontFamily = interfamily,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isGrowthPositive)
                            Color(0xFF22C55E)
                        else
                            Color(0xFFEF4444),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


// ---- PREVIEW (untuk tes responsif) ----
// Preview ini memakai section stateless (HeaderSection, DashboardFilterRow, StatCardsSection,
// SectionHeader, RingkasanSection) BUKAN DashboardScreen(), karena DashboardScreen() memanggil
// hiltViewModel() yang tidak tersedia di Compose Preview (menyVMs causing "Show Exception").
@Preview(name = "HP kecil 320x568", device = "spec:width=320dp,height=568dp", showBackground = true)
@Preview(name = "HP standar 360x800", device = "spec:width=360dp,height=800dp", showBackground = true)
@Preview(name = "HP besar 411x891", device = "spec:width=411dp,height=891dp", showBackground = true)
@Preview(name = "Font besar", device = "spec:width=360dp,height=800dp", fontScale = 1.3f, showBackground = true)
@Composable
private fun DashboardScreenPreviewResponsif() {
    val dummyAnalytics = DashboardAnalytics(
        penjualanHariIni = 1250000.0,
        pertumbuhanPenjualanPersen = 12.5,
        isPertumbuhanPenjualanPositif = true,
        totalTransaksiHariIni = 24,
        pertumbuhanTransaksiPersen = 8.0,
        isPertumbuhanTransaksiPositif = true,
        totalPelangganHariIni = 17,
        pertumbuhanPelangganPersen = 3.5,
        isPertumbuhanPelangganPositif = true,
        totalProdukTerjualHariIni = 40,
        pertumbuhanProdukTerjualPersen = 5.0,
        isPertumbuhanProdukTerjualPositif = true,
        isLoading = false
    )
    MaterialTheme {
        Surface(color = Color(0xFFF0F4FF)) {
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderSection(userName = "Budi Santoso")
                DashboardFilterRow(
                    selectedFilter = PeriodeLaporanPenjualan.HARI_INI,
                    customDateLabel = "",
                    onFilterSelected = {},
                    onOpenDatePicker = {}
                )
                StatCardsSection(
                    analytics = dummyAnalytics,
                    selectedFilter = PeriodeLaporanPenjualan.HARI_INI,
                    customDateLabel = "",
                    isBalanceVisible = true
                )
                SectionHeader(title = "Produk")
                SectionHeader(title = "Total Penjualan")
                RingkasanSection(
                    analytics = dummyAnalytics,
                    selectedFilter = PeriodeLaporanPenjualan.HARI_INI
                )
            }
        }

    }
}

@com.ptpws.ikikasir.core.responsive.presentation.components.PhoneDevicePreviews
@Composable
private fun DashboardFullScreenPreview() {
    MaterialTheme {
        DashboardScreen(
            navController = androidx.navigation.compose.rememberNavController(),
            viewModel = com.ptpws.ikikasir.commond.PreviewDashboardProvider()
        )
    }
}
