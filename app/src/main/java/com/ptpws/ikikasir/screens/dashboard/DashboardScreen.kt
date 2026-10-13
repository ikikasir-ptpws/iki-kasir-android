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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.screens.navigation.AppScreen

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val sessionState by viewModel.sessionState.collectAsState()
    val analyticsState by viewModel.analyticsState.collectAsState()
    val userName = sessionState.user?.fullName?.ifBlank { "Pengguna" } ?: "Pengguna"

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

    val windowSizeInfo = com.ptpws.ikikasir.core.responsive.presentation.util.LocalWindowSizeInfo.current
    val isWideScreen = windowSizeInfo.shouldUseNavigationRail || windowSizeInfo.shouldUsePermanentDrawer

    Scaffold(
        containerColor = Color(0xFFF0F4FF),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4FF))
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = if (isWideScreen) 56.dp else 128.dp)
        ) {
            item { HeaderSection(userName = userName) }
            item {
                StatCardsSection(
                    analytics = analyticsState,
                    onCardClick = {
                        if (showTransaksi) {
                            navController.navigate(AppScreen.Riwayat.route)
                        } else if (showLaporanKeuangan) {
                            navController.navigate(AppScreen.LaporanKeuangan.route)
                        }
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
                    title = "Ringkasan Hari Ini",
                    onLihatSemua = {
                        navController.navigate(AppScreen.LaporanPenjualan.routeWith("HARI_INI"))
                    }
                )
            }
            item { RingkasanSection(analytics = analyticsState) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
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
        Column {
            Text(
                text = "Selamat datang,",
                fontSize = if (isSmallScreen) 13.sp else 14.sp,
                fontFamily = interfamily,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Text(
                text = userName,
                fontSize = if (isSmallScreen) 18.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1D2E)
            )
            Text(
                text = "Kelola bisnis Anda dengan mudah",
                fontSize = if (isSmallScreen) 12.sp else 13.sp,
                fontFamily = interfamily,
                color = Color(0xFF8A8FA8)
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
fun StatCardsSection(
    analytics: DashboardAnalytics = DashboardAnalytics(),
    onCardClick: (() -> Unit)? = null
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val sectionHorizontalPadding = if (isSmallScreen) 12.dp else 20.dp
    val cardPadding = if (isSmallScreen) 12.dp else 16.dp
    val cardCornerRadius = if (isSmallScreen) 16.dp else 20.dp
    val iconBoxSize = if (isSmallScreen) 32.dp else 36.dp

    val formattedPenjualan = formatDashboardNominal(analytics.penjualanHariIni)
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = sectionHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 12.dp)
    ) {
        // Card Penjualan Hari Ini — gradient biru
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .then(
                    if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier
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

                    Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "PENJUALAN HARI INI",
                            fontSize = if (isSmallScreen) 9.5.sp else 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "ⓘ",
                            fontSize = if (isSmallScreen) 9.sp else 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Rp",
                        fontSize = if (isSmallScreen) 12.sp else 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = formattedPenjualan,
                        fontSize = if (isSmallScreen) {
                            if (formattedPenjualan.length > 8) 16.sp else 20.sp
                        } else {
                            if (formattedPenjualan.length > 10) 19.sp else 24.sp
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

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
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(3.dp))

                        Text(
                            text = "dari kemarin",
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
                    if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    color = Color(0xFF1A1D2E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                            color = badgeColor
                        )
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = "dari kemarin",
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
            color = Color(0xFF1A1D2E)
        )
        if (onLihatSemua != null) {
            Text(
                text = "Lihat Semua",
                fontSize = if (isSmallScreen) 12.sp else 13.sp,
                color = Color(0xFF3D5AF1),
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
                onClick = { navController.navigate(AppScreen.Riwayat.route) }
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
            lineHeight = if (isSmallScreen) 14.sp else 16.sp
        )
    }
}


@Composable
fun RingkasanSection(
    analytics: DashboardAnalytics = DashboardAnalytics()
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp.dp <= 340.dp
    val sectionHorizontalPadding = if (isSmallScreen) 12.dp else 20.dp

    val custGrowthPercent = formatDashboardPercent(analytics.pertumbuhanPelangganPersen)
    val custGrowthText = if (analytics.pertumbuhanPelangganPersen == 0.0) {
        "0% dari kemarin"
    } else {
        "${custGrowthPercent}% dari kemarin"
    }

    val soldGrowthPercent = formatDashboardPercent(analytics.pertumbuhanProdukTerjualPersen)
    val soldGrowthText = if (analytics.pertumbuhanProdukTerjualPersen == 0.0) {
        "0% dari kemarin"
    } else {
        "${soldGrowthPercent}% dari kemarin"
    }

    val formattedCust = formatDashboardInteger(analytics.totalPelangganHariIni)
    val formattedSold = formatDashboardInteger(analytics.totalProdukTerjualHariIni)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sectionHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 12.dp)
    ) {
        RingkasanCard(
            modifier = Modifier.weight(1f),
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
            modifier = Modifier.weight(1f),
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
        maximumFractionDigits = if (value % 1.0 == 0.0) 0 else 2
    }
    return formatter.format(value)
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
        modifier = modifier.height(if (isSmallScreen) 105.dp else 115.dp),
        shape = RoundedCornerShape(if (isSmallScreen) 16.dp else 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSmallScreen) 4.dp else 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isSmallScreen) 10.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(if (isSmallScreen) 36.dp else 42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(if (isSmallScreen) 18.dp else 22.dp)
                )
            }

            Spacer(modifier = Modifier.width(if (isSmallScreen) 8.dp else 12.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = value,
                    fontSize = if (isSmallScreen) 18.sp else 22.sp,
                    fontFamily = interfamily,
                    lineHeight = if (isSmallScreen) 18.sp else 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D2E)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = interfamily,
                        fontSize = if (isSmallScreen) 11.sp else 12.sp,
                        color = Color(0xFF8A8FA8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
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
                        modifier = Modifier.size(if (isSmallScreen) 10.dp else 12.dp)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = growth,
                        fontSize = if (isSmallScreen) 9.5.sp else 11.sp,
                        fontFamily = interfamily,
                        lineHeight = if (isSmallScreen) 11.sp else 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isGrowthPositive)
                            Color(0xFF22C55E)
                        else
                            Color(0xFFEF4444),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme {
        DashboardScreen(navController = rememberNavController())
    }
}