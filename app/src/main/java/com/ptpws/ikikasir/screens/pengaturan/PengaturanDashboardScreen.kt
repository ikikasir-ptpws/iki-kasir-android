package com.ptpws.ikikasir.screens.pengaturan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanDashboardScreen(
    onBack: () -> Unit = {},
    onSimpanPengaturan: () -> Unit = {}
) {
    var tampilkanPenjualanHariIni by remember { mutableStateOf(true) }
    var tampilkanTotalTransaksi by remember { mutableStateOf(true) }
    var tampilkanProdukTerlaris by remember { mutableStateOf(true) }
    var tampilkanCabangTeraktif by remember { mutableStateOf(false) }
    var tampilkanGrafikPenjualan by remember { mutableStateOf(true) }

    var aksesKasir by remember { mutableStateOf(true) }
    var aksesProduk by remember { mutableStateOf(true) }
    var aksesTransaksi by remember { mutableStateOf(true) }
    var aksesHutang by remember { mutableStateOf(false) }
    var aksesDatabase by remember { mutableStateOf(true) }
    var aksesManajemenStok by remember { mutableStateOf(true) }
    var aksesSupplier by remember { mutableStateOf(true) }
    var aksesLaporan by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan Dashboard",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 20.sp, color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6),
                    titleContentColor = Color(0xFF111827),
                    navigationIconContentColor = Color(0xFF4F46E5)
                )
            )
        },

        ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Tampilan Utama
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TAMPILAN UTAMA",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            PengaturanToggleItem(
                                icon = Icons.Outlined.Payments,
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Penjualan Hari Ini",
                                checked = tampilkanPenjualanHariIni,
                                onCheckedChange = { tampilkanPenjualanHariIni = it }
                            )
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            PengaturanToggleItem(
                                icon = Icons.Outlined.ReceiptLong,
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Total Transaksi",
                                checked = tampilkanTotalTransaksi,
                                onCheckedChange = { tampilkanTotalTransaksi = it }
                            )
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            PengaturanToggleItem(
                                icon = Icons.Outlined.StarBorder,
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Produk Terlaris",
                                checked = tampilkanProdukTerlaris,
                                onCheckedChange = { tampilkanProdukTerlaris = it }
                            )
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            PengaturanToggleItem(
                                icon = Icons.Outlined.Storefront,
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Cabang Teraktif",
                                checked = tampilkanCabangTeraktif,
                                onCheckedChange = { tampilkanCabangTeraktif = it }
                            )
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            PengaturanToggleItem(
                                icon = Icons.Outlined.ShowChart,
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Grafik Penjualan",
                                checked = tampilkanGrafikPenjualan,
                                onCheckedChange = { tampilkanGrafikPenjualan = it }
                            )
                        }
                    }
                }
            }

            // ── Akses Cepat
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "AKSES CEPAT",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AksesCepatCard(
                                modifier = Modifier.weight(1f),
                                icon = rememberVectorPainter(Icons.Outlined.PointOfSale),
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Kasir",
                                checked = aksesKasir,
                                onCheckedChange = { aksesKasir = it }
                            )
                            AksesCepatCard(
                                modifier = Modifier.weight(1f),
                                icon = rememberVectorPainter(Icons.Outlined.Inventory2),
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Produk",
                                checked = aksesProduk,
                                onCheckedChange = { aksesProduk = it }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AksesCepatCard(
                                modifier = Modifier.weight(1f),
                                icon = rememberVectorPainter(Icons.Outlined.History),
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Transaksi",
                                checked = aksesTransaksi,
                                onCheckedChange = { aksesTransaksi = it }
                            )
                            AksesCepatCard(
                                modifier = Modifier.weight(1f),
                                icon = rememberVectorPainter(Icons.Outlined.AccountBalanceWallet),
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Hutang",
                                checked = aksesHutang,
                                onCheckedChange = { aksesHutang = it }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AksesCepatCard(
                                modifier = Modifier.weight(1f),
                                icon = painterResource(id = R.drawable.database),
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Database",
                                checked = aksesDatabase,
                                onCheckedChange = { aksesDatabase = it }
                            )
                            AksesCepatCard(
                                modifier = Modifier.weight(1f),
                                icon = rememberVectorPainter(Icons.Outlined.Warehouse),
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Manajemen Stok",
                                checked = aksesManajemenStok,
                                onCheckedChange = { aksesManajemenStok = it }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AksesCepatCard(
                                modifier = Modifier.weight(1f),
                                icon = rememberVectorPainter(Icons.Outlined.LocalShipping),
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Supplier",
                                checked = aksesSupplier,
                                onCheckedChange = { aksesSupplier = it }
                            )
                            AksesCepatCard(
                                modifier = Modifier.weight(1f),
                                icon = rememberVectorPainter(Icons.Outlined.Analytics),
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Laporan",
                                checked = aksesLaporan,
                                onCheckedChange = { aksesLaporan = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Tombol Simpan Pengaturan
                Button(
                    onClick = onSimpanPengaturan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan Pengaturan",
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ── Item Toggle untuk Tampilan Utama

@Composable
fun PengaturanToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    iconTint: Color,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontFamily = interfamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111827),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color(0xFF4F46E5),
                checkedThumbColor = Color.White
            )
        )
    }
}

// ── Card Akses Cepat

@Composable
fun AksesCepatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.painter.Painter,
    iconBackground: Color,
    iconTint: Color,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = label,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Color(0xFF4F46E5),
                        checkedThumbColor = Color.White
                    )
                )
            }
            Text(
                text = label,
                fontFamily = interfamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111827)
            )
        }
    }
}

// ── Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PengaturanDashboardScreenPreview() {
    MaterialTheme {
        PengaturanDashboardScreen()
    }
}