package com.example.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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

    val showProduk = viewModel.isAllowed("Produk") && matchSearch(query, "produk", "barang", "item")
    val showKategori = viewModel.isAllowed("Kategori Produk") && matchSearch(query, "kategori produk", "kategori")
    val showStok = viewModel.isAllowed("Manajemen Stok") && matchSearch(query, "manajemen stok", "stok", "stock")
    val hasProdukSection = showProduk || showKategori || showStok

    val showKasir = viewModel.isAllowed("Kasir") && matchSearch(query, "kasir", "pos")
    val showTransaksi = viewModel.isAllowed("Transaksi") && matchSearch(query, "transaksi", "riwayat")
    val showAntrean = viewModel.isAllowed("Antrean") && matchSearch(query, "antrean", "antrian")
    val showRiwayatAntrean = viewModel.isAllowed("Riwayat Antrean") && matchSearch(query, "riwayat antrean", "riwayat antrian")
    val showPromo = viewModel.isAllowed("Promo") && matchSearch(query, "promo", "diskon", "voucher")
    val showLaporanPenjualan = viewModel.isAllowed("Laporan Penjualan") && matchSearch(query, "laporan penjualan", "penjualan", "produk terjual", "terjual")
    val hasPenjualanSection = showKasir || showTransaksi || showAntrean || showRiwayatAntrean || showPromo || showLaporanPenjualan

    val showLaporanKeuangan = viewModel.isAllowed("Laporan Keuangan") && matchSearch(query, "laporan keuangan", "laporan", "keuangan")
    val hasKeuanganSection = showLaporanKeuangan

    val showManajemenPengguna = viewModel.isAllowed("Manajemen Pengguna") && matchSearch(query, "manajemen pengguna", "pengguna", "user")
    val showAuditLog = viewModel.isAllowed("Audit Log") && matchSearch(query, "audit log", "auditlog", "audit", "log")
    val hasPenggunaSection = showManajemenPengguna || showAuditLog

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
                        text = if (query.isNotBlank()) "Tidak ada menu yang sesuai dengan \"$query\"." else "Tidak ada menu yang tersedia untuk akun ini.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        fontFamily = interfamily
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
                        showPromo = showPromo,
                        showLaporanPenjualan = showLaporanPenjualan
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
            .height(48.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Cari",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color(0xFF1E293B),
                    fontSize = 14.sp,
                    fontFamily = interfamily
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Cari Menu...",
                            fontSize = 14.sp,
                            fontFamily = interfamily,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus pencarian",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun matchSearch(query: String, vararg keywords: String): Boolean {
    if (query.isBlank()) return true
    val cleanQuery = query.trim().lowercase()
    return keywords.any { keyword ->
        val cleanKeyword = keyword.trim().lowercase()
        cleanKeyword.contains(cleanQuery) || cleanQuery.contains(cleanKeyword)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuFullScreenPreview() {
    MaterialTheme {
        MenuFullScreen(navController = rememberNavController())
    }
}
