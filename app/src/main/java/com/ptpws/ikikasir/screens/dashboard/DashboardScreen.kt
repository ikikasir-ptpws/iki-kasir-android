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

    Scaffold(
        containerColor = Color(0xFFF0F4FF),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4FF))
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 120.dp)
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

            if (hasProdukSection) {
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

            if (hasPenjualanSection) {
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
                        if (showLaporanKeuangan) {
                            navController.navigate(AppScreen.LaporanKeuangan.route)
                        } else if (showTransaksi) {
                            navController.navigate(AppScreen.Riwayat.route)
                        }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Selamat datang,",
                fontSize = 14.sp,
                fontFamily = interfamily,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1D2E)
            )
            Text(
                text = "Kelola bisnis Anda dengan mudah",
                fontSize = 13.sp,
                fontFamily = interfamily,
                color = Color(0xFF8A8FA8)
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.notif),
                contentDescription = "Notifikasi",
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
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card Penjualan Hari Ini — gradient biru
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .then(
                    if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier
                ),
            shape = RoundedCornerShape(20.dp),
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
                    .padding(16.dp)
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.grafikicon),
                        contentDescription = "Grafik",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(6.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PENJUALAN HARI INI ⓘ",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Rp",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = formattedPenjualan,
                        fontSize = if (formattedPenjualan.length > 10) 19.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = salesGrowthText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "dari kemarin",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
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
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.penjualan),
                    contentDescription = "Transaksi",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDBEAFE))
                        .padding(6.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TOTAL TRANSAKSI ⓘ",
                    fontSize = 10.sp,
                    color = Color(0xFF8A8FA8)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${analytics.totalTransaksiHariIni} trx",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D2E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val badgeColor = if (analytics.isPertumbuhanTransaksiPositif) Color(0xFF22C55E) else Color(0xFFEF4444)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = txGrowthText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "dari kemarin",
                        fontSize = 11.sp,
                        color = Color(0xFF8A8FA8)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onLihatSemua: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontFamily = interfamily,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1D2E)
        )
        if (onLihatSemua != null) {
            Text(
                text = "Lihat Semua",
                fontSize = 13.sp,
                color = Color(0xFF3D5AF1),
                modifier = Modifier.clickable { onLihatSemua() }
            )
        }
    }
}

@Composable
fun ProdukMenuSection(
    navController: NavController,
    showProduk: Boolean = true,
    showKategori: Boolean = true,
    showStok: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        if (showProduk) {
            MenuIconItem(
                iconRes = R.drawable.produk,
                label = "Produk",
                bgColor = Color(0xFFF0FDF4),
                onClick = { navController.navigate(AppScreen.Produk.baseRoute) }
            )
        }
        if (showKategori) {
            if (showProduk) Spacer(modifier = Modifier.width(28.dp))
            MenuIconItem(
                iconRes = R.drawable.kategori,
                label = "Kategori\nProduk",
                bgColor = Color(0xFFFAF5FF),
                onClick = { navController.navigate(AppScreen.KategoriProduk.route) }
            )
        }
        if (showStok) {
            if (showProduk || showKategori) Spacer(modifier = Modifier.width(28.dp))
            MenuIconItem(
                iconRes = R.drawable.manajemenstok,
                label = "Manajemen\nStok",
                bgColor = Color(0xFFFFF7ED),
                onClick = { navController.navigate(AppScreen.ManajemenStok.route) }
            )
        }
    }
}

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
    val firstRowItems = mutableListOf<@Composable () -> Unit>()
    if (showKasir) {
        firstRowItems.add {
            MenuIconItem(
                iconRes = R.drawable.kasirmenu,
                label = "Kasir",
                bgColor = Color(0xFFEFF6FF),
                onClick = { navController.navigate(AppScreen.Kasir.route) }
            )
        }
    }
    if (showTransaksi) {
        firstRowItems.add {
            MenuIconItem(
                iconRes = R.drawable.transaksi,
                label = "Transaksi",
                bgColor = Color(0xFFECFEFF),
                onClick = { navController.navigate(AppScreen.Riwayat.route) }
            )
        }
    }
    if (showAntrean) {
        firstRowItems.add {
            MenuIconItem(
                iconRes = R.drawable.waitinglist,
                label = "Antrean",
                bgColor = Color(0xFFECFEFF),
                onClick = { navController.navigate(AppScreen.WaitingList.route) }
            )
        }
    }
    if (showRiwayatAntrean) {
        firstRowItems.add {
            MenuIconItem(
                iconRes = R.drawable.riwayatantrean,
                label = "Riwayat\nAntrean",
                bgColor = Color(0xFFEFF6FF),
                onClick = { navController.navigate(AppScreen.RiwayatAntrean.route) }
            )
        }
    }

    val secondRowItems = mutableListOf<@Composable () -> Unit>()
    if (showPromo) {
        secondRowItems.add {
            MenuIconItem(
                iconRes = R.drawable.promo,
                label = "Promo",
                bgColor = Color(0xFFFFFBEB),
                onClick = { navController.navigate(AppScreen.Promo.route) }
            )
        }
    }
    if (showLaporanPenjualan) {
        secondRowItems.add {
            MenuIconItem(
                iconRes = R.drawable.laporanpenjualan,
                label = "Laporan\nPenjualan",
                bgColor = Color(0xFFECFEFF),
                onClick = { navController.navigate(AppScreen.LaporanPenjualan.route) }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (firstRowItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                firstRowItems.forEachIndexed { idx, item ->
                    if (idx > 0) Spacer(modifier = Modifier.width(28.dp))
                    item()
                }
            }
        }
        if (secondRowItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                secondRowItems.forEachIndexed { idx, item ->
                    if (idx > 0) Spacer(modifier = Modifier.width(28.dp))
                    item()
                }
            }
        }
    }
}

@Composable
fun MenuIconItem(
    iconRes: Int,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {

        Card(
            onClick = onClick,
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = RoundedCornerShape(18.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(18.dp),
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
                    modifier = Modifier.size(25.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = interfamily,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}


@Composable
fun RingkasanSection(
    analytics: DashboardAnalytics = DashboardAnalytics()
) {
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
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RingkasanCard(
            modifier = Modifier.weight(1f),
            value = formattedCust,
            title = "Pelanggan",
            growth = custGrowthText,
            isGrowthPositive = analytics.isPertumbuhanPelangganPositif,
            icon = Icons.Outlined.Person,
            iconColor = Color(0xFF22C55E),
            iconBg = Color(0xFFDCFCE7)
        )

        RingkasanCard(
            modifier = Modifier.weight(1f),
            value = formattedSold,
            title = "Terjual",
            growth = soldGrowthText,
            isGrowthPositive = analytics.isPertumbuhanProdukTerjualPositif,
            icon = Icons.Outlined.Inventory2,
            iconColor = Color(0xFFF97316),
            iconBg = Color(0xFFFFEDD5)
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
    iconBg: Color
) {
    Card(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontFamily = interfamily,
                    lineHeight = 10.sp,
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
                        fontSize = 12.sp,
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
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = growth,
                        fontSize = 11.sp,
                        fontFamily = interfamily,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isGrowthPositive)
                            Color(0xFF22C55E)
                        else
                            Color(0xFFEF4444)
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