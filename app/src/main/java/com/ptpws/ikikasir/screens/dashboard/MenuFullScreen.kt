package com.example.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.screens.navigation.AppScreen

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun MenuFullScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val sessionState by viewModel.sessionState.collectAsState()
    val queryState = remember { mutableStateOf("") }
    val query = queryState.value.trim()

    val showProduk = viewModel.isAllowed("Produk") && (query.isBlank() || "produk".contains(query, true))
    val showKategori = viewModel.isAllowed("Kategori Produk") && (query.isBlank() || "kategori".contains(query, true))
    val showStok = viewModel.isAllowed("Manajemen Stok") && (query.isBlank() || "stok".contains(query, true))
    val hasProdukSection = showProduk || showKategori || showStok

    val showKasir = viewModel.isAllowed("Kasir") && (query.isBlank() || "kasir".contains(query, true))
    val showTransaksi = viewModel.isAllowed("Transaksi") && (query.isBlank() || "transaksi".contains(query, true))
    val showAntrean = viewModel.isAllowed("Antrean") && (query.isBlank() || "antrean".contains(query, true))
    val showRiwayatAntrean = viewModel.isAllowed("Riwayat Antrean") && (query.isBlank() || "riwayat antrean".contains(query, true))
    val showPromo = viewModel.isAllowed("Promo") && (query.isBlank() || "promo".contains(query, true))
    val hasPenjualanSection = showKasir || showTransaksi || showAntrean || showRiwayatAntrean || showPromo

    val showLaporanKeuangan = viewModel.isAllowed("Laporan Keuangan") && (query.isBlank() || "laporan".contains(query, true) || "keuangan".contains(query, true))
    val hasKeuanganSection = showLaporanKeuangan

    val showManajemenPengguna = viewModel.isAllowed("Manajemen Pengguna") && (query.isBlank() || "pengguna".contains(query, true))
    val showPengaturanMenu = viewModel.isAllowed("Pengaturan Menu") && (query.isBlank() || "pengaturan".contains(query, true))
    val showAuditLog = viewModel.isAllowed("Audit Log") && (query.isBlank() || "auditlog".contains(query, true) || "audit".contains(query, true))
    val hasPenggunaSection = showManajemenPengguna || showPengaturanMenu || showAuditLog

    Scaffold(
        containerColor = Color(0xFFF0F4FF)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4FF))
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                TopBar(
                    title = "Semua Menu",
                    navController = navController
                )
                Spacer(modifier = Modifier.height(12.dp))

                SearchField(
                    query = queryState.value,
                    onQueryChange = { queryState.value = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (sessionState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (!hasProdukSection && !hasPenjualanSection && !hasKeuanganSection && !hasPenggunaSection) {
                item {
                    Text(
                        text = "Tidak ada menu yang tersedia untuk akun ini.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                }
            }

            if (!sessionState.isLoading && hasProdukSection) {
                item { SectionTitle(title = "Produk") }
                item {
                    ProdukMenuSection(
                        navController = navController,
                        showProduk = showProduk,
                        showKategori = showKategori,
                        showStok = showStok
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            if (!sessionState.isLoading && hasPenjualanSection) {
                item { SectionTitle(title = "Penjualan") }
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
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            if (!sessionState.isLoading && hasKeuanganSection) {
                item { SectionTitle(title = "Keuangan") }
                item { KeuanganMenuSection(navController = navController, showLaporan = showLaporanKeuangan) }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            if (!sessionState.isLoading && hasPenggunaSection) {
                item { SectionTitle(title = "Pengguna") }
                item {
                    PenggunaMenuSection(
                        navController = navController,
                        showPengguna = showManajemenPengguna,
                        showPengaturan = showPengaturanMenu,
                        showAuditlog = showAuditLog
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
fun KeuanganMenuSection(
    navController: NavController,
    showLaporan: Boolean = true
) {
    if (!showLaporan) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        MenuIconItem(
            iconRes = R.drawable.laporankeuangan,
            label = "Laporan\nKeuangan",
            bgColor = Color(0xFFFFF0F6),
            onClick = { navController.navigate(AppScreen.LaporanKeuangan.route) }
        )
    }
}

@Composable
fun PenggunaMenuSection(
    navController: NavController,
    showPengguna: Boolean = true,
    showPengaturan: Boolean = true,
    showAuditlog: Boolean = true
) {
    val items = mutableListOf<@Composable () -> Unit>()
    if (showPengguna) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.pengguna,
                label = "Pengguna",
                bgColor = Color(0xFFEFF1FF),
                onClick = { navController.navigate(AppScreen.Pengguna.route) }
            )
        }
    }
    if (showPengaturan) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.pengaturanmenu,
                label = "Pengaturan\nMenu",
                bgColor = Color(0xFFF4F7FF),
                onClick = { navController.navigate(AppScreen.PengaturanMenu.route) }
            )
        }
    }
    if (showAuditlog) {
        items.add {
            MenuIconItem(
                iconRes = R.drawable.auditlog,
                label = "Auditlog",
                bgColor = Color(0xFFFFF6F0),
                onClick = { navController.navigate(AppScreen.AuditLog.route) }
            )
        }
    }

    if (items.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        items.forEachIndexed { idx, item ->
            if (idx > 0) Spacer(modifier = Modifier.width(28.dp))
            item()
        }
    }
}

@Composable
fun TopBar(title: String, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            if (navController.currentDestination?.route == AppScreen.Semuamenu.route || navController.currentDestination?.route == "semuamenu") {
                navController.popBackStack()
            }
        }) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = Color(0xFF3D5AF1)
            )
        }
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = interfamily,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontFamily = interfamily,
        fontWeight = FontWeight.SemiBold ,
        color = Color(0xFF1A1D2E),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2F3F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 12.sp,
                fontFamily = interfamily
            ),
            modifier = Modifier.fillMaxSize(),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Cari",
                        tint = Color(0x80474747)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Cari Menu",
                                fontSize = 12.sp,
                                color = Color(0x80474747)
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuFullScreenPreview() {
    MaterialTheme {
        MenuFullScreen(navController = rememberNavController())
    }
}
