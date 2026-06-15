package com.ptpws.ikikasir.screens.promo

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenPromoScreen(
    onBack: () -> Unit = {},
    onTambahPromo: () -> Unit = {}
) {
    var filterpromo by remember { mutableStateOf("Semua") }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manajemen Promo",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color.Black,
                        fontSize = 20.sp
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
                    containerColor = Color(0xFFF3F4F6)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onTambahPromo,
                containerColor = Color(0xFF4F46E5),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Promo",
                    tint = Color.White
                )
            }
        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Banner Statistik ──────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Promo Aktif
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4F46E5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PROMO AKTIF",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "12",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    color = Color.White
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .size(56.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                            )
                        }
                    }

                    // Berakhir Minggu Ini
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "BERAKHIR\nMINGGU INI",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = Color(0xFFEF4444),
                                    letterSpacing = 0.5.sp,
                                    lineHeight = 14.sp
                                )
                                Text(
                                    text = "4",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFEF4444).copy(alpha = 0.2f),
                                modifier = Modifier
                                    .size(56.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Filter Chip
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        val isSelected = filterpromo == "Semua"
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterpromo = "Semua" },
                            label = {
                                Text(
                                    text = "Semua",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4F46E5),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF374151)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = Color(0xFFE5E7EB),
                                borderWidth = 1.dp,
                                selectedBorderWidth = 0.dp
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    item {
                        val isSelected = filterpromo == "Bundling"
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterpromo = "Bundling" },
                            label = {
                                Text(
                                    text = "Bundling",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4F46E5),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF374151)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = Color(0xFFE5E7EB),
                                borderWidth = 1.dp,
                                selectedBorderWidth = 0.dp
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    item {
                        val isSelected = filterpromo == "Voucher"
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterpromo = "Voucher" },
                            label = {
                                Text(
                                    text = "Voucher",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4F46E5),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF374151)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = Color(0xFFE5E7EB),
                                borderWidth = 1.dp,
                                selectedBorderWidth = 0.dp
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    item {
                        val isSelected = filterpromo == "Tanggal Tertentu"
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterpromo = "Tanggal Tertentu" },
                            label = {
                                Text(
                                    text = "Tanggal Tertentu",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4F46E5),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF374151)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = Color(0xFFE5E7EB),
                                borderWidth = 1.dp,
                                selectedBorderWidth = 0.dp
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Promo Card 1: Bundling Aktif
            item {
                PromoCardItem(
                    tipePromo = "BUNDLING",
                    tipeWarna = Color(0xFFEEF2FF),
                    tipeTeksWarna = Color(0xFF4F46E5),
                    statusTeks = "AKTIF",
                    statusWarna = Color(0xFF059669),
                    judulPromo = "Bundling Kopi + Roti Srikaya",
                    deskripsi = "Potongan harga untuk pembelian Paket Sarapan pagi setiap jam 07:00 - 10:00.",
                    info1Label = "POTONGAN",
                    info1Nilai = "Rp 5.000",
                    info2Label = "PRODUK TERKAIT",
                    info2Nilai = "2 Item",
                    tombolUtama = "Edit",
                    tombolKedua = "Nonaktifkan",
                    tombolKeduaMerah = false,
                    onTombolUtama = {},
                    onTombolKedua = {}
                )
            }

            // Promo Card 2: Voucher Nonaktif
            item {
                PromoCardItem(
                    tipePromo = "VOUCHER",
                    tipeWarna = Color(0xFFF0FDF4),
                    tipeTeksWarna = Color(0xFF059669),
                    statusTeks = "NONAKTIF",
                    statusWarna = Color(0xFF9CA3AF),
                    judulPromo = "Diskon Gajian 20%",
                    deskripsi = "Voucher belanja minimum Rp 100.000 khusus untuk pelanggan setia.",
                    info1Label = "PERSENTASE",
                    info1Nilai = "20%",
                    info2Label = "KODE",
                    info2Nilai = "GAJIAN20",
                    tombolUtama = "Aktifkan",
                    tombolKedua = null,
                    tombolKeduaMerah = false,
                    onTombolUtama = {},
                    onTombolKedua = {}
                )
            }

            // ── Promo Card 3: Promo Tanggal Tertentu ─────────────────────
            item {
                PromoCardItem(
                    tipePromo = "PROMO TANGGAL TERTENTU",
                    tipeWarna = Color(0xFFFFFBEB),
                    tipeTeksWarna = Color(0xFFD97706),
                    statusTeks = "DIJADWALKAN",
                    statusWarna = Color(0xFFD97706),
                    judulPromo = "Promo Akhir Pekan (Weekend)",
                    deskripsi = "Beli 2 Gratis 1 untuk semua varian Croissant setiap hari Sabtu dan Minggu.",
                    info1Label = "JADWAL",
                    info1Nilai = "Setiap Sabtu & Minggu",
                    info2Label = null,
                    info2Nilai = null,
                    tombolUtama = "Edit",
                    tombolKedua = null,
                    tombolKeduaMerah = false,
                    onTombolUtama = {},
                    onTombolKedua = {}
                )
            }
        }
    }
}

// Composable: Card item promo

@Composable
fun PromoCardItem(
    tipePromo: String,
    tipeWarna: Color,
    tipeTeksWarna: Color,
    statusTeks: String,
    statusWarna: Color,
    judulPromo: String,
    deskripsi: String,
    info1Label: String,
    info1Nilai: String,
    info2Label: String?,
    info2Nilai: String?,
    tombolUtama: String,
    tombolKedua: String?,
    tombolKeduaMerah: Boolean,
    onTombolUtama: () -> Unit,
    onTombolKedua: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Baris tipe + status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = tipeWarna,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tipePromo,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = tipeTeksWarna,
                        letterSpacing = 0.3.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = statusWarna,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = statusTeks,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = statusWarna
                    )
                }
            }

            // Judul
            Text(
                text = judulPromo,
                fontFamily = interfamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF111827)
            )

            // Deskripsi
            Text(
                text = deskripsi,
                fontFamily = interfamily,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                lineHeight = 18.sp
            )

            // Info baris (label + nilai)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = info1Label,
                        fontFamily = interfamily,
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = info1Nilai,
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Bold
                    )
                }
                if (info2Label != null && info2Nilai != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = info2Label,
                            fontFamily = interfamily,
                            fontSize = 10.sp,
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            text = info2Nilai,
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tombol aksi
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTombolUtama,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tombolUtama,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
                if (tombolKedua != null) {
                    OutlinedButton(
                        onClick = onTombolKedua,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.5.dp,
                            if (tombolKeduaMerah) Color(0xFFEF4444) else Color(0xFFD1D5DB)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (tombolKeduaMerah) Color(0xFFEF4444) else Color(0xFF374151)
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tombolKedua,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ManajemenPromoSCreenPreview() {
ManajemenPromoScreen ()
}