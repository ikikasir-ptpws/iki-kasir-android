package com.ptpws.ikikasir.screens.keuangan

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenHutangScreen(
    onBack: () -> Unit = {},
    onTambahHutang: () -> Unit = {}
)
{
    var cariPelangganutang by remember { mutableStateOf("") }
    var filterAktifutang by remember { mutableStateOf("Semua") }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manajemen Hutang",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color.Black,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onTambahHutang,
                containerColor = Color(0xFF4F46E5),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Hutang",
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
                top = 4.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Header Status Piutang
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Status Piutang",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Ringkasan Operasional",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            // Banner Statistik
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Piutang
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            Color(0xFFEEF2FF),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Total Piutang",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            Text(
                                text = "Rp 2.500.000",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF111827)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "+12% Bln ini",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }

                    // Jatuh Tempo
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            Color(0xFFFFFBEB),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Jatuh Tempo",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            Text(
                                text = "5 Pelanggan",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF111827)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Perlu Tindakan",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    BasicTextField(
                        value = cariPelangganutang,
                        onValueChange = { cariPelangganutang = it },
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
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0x80474747),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (cariPelangganutang.isEmpty()) {
                                        Text(
                                            text = "Cari nama pelanggan...",
                                            fontFamily = interfamily,
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

            // Filter Chip
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(listOf("Semua", "Jatuh Tempo", "Tertunda", "Selesai")) { filter ->
                        val isSelected = filterAktifutang == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterAktifutang = filter },
                            label = {
                                Text(
                                    text = filter,
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

            // Hutang Card 1
            item {
                HutangCardItem(
                    inisial = "BS",
                    warnaBg = Color(0xFF4F46E5),
                    namaCustomer = "Budi Santoso",
                    keterangan = "Terakhir bayar: 20 Okt",
                    jumlahHutang = "Rp 150.000",
                    tanggalJatuhTempo = "15 Nov 2023",
                    warnaJatuhTempo = Color(0xFFEF4444),
                    warnaBgJatuhTempo = Color(0xFFFEE2E2),
                    onPengingat = {},
                    onBayarHutang = {}
                )
            }

            // Hutang Card 2
            item {
                HutangCardItem(
                    inisial = "AL",
                    warnaBg = Color(0xFF8B5CF6),
                    namaCustomer = "Ani Lestari",
                    keterangan = "Pelanggan Tetap",
                    jumlahHutang = "Rp 425.000",
                    tanggalJatuhTempo = "22 Des 2023",
                    warnaJatuhTempo = Color(0xFF6B7280),
                    warnaBgJatuhTempo = Color(0xFFF3F4F6),
                    onPengingat = {},
                    onBayarHutang = {}
                )
            }

            // Hutang Card 3
            item {
                HutangCardItem(
                    inisial = "HW",
                    warnaBg = Color(0xFF059669),
                    namaCustomer = "Hendra Wijaya",
                    keterangan = "Pesan Grosir",
                    jumlahHutang = "Rp 1.100.000",
                    tanggalJatuhTempo = "05 Nov 2023",
                    warnaJatuhTempo = Color(0xFFEF4444),
                    warnaBgJatuhTempo = Color(0xFFFEE2E2),
                    onPengingat = {},
                    onBayarHutang = {}
                )
            }

            // Hutang Card 4
            item {
                HutangCardItem(
                    inisial = "HW",
                    warnaBg = Color(0xFF059669),
                    namaCustomer = "Hendra Wijaya",
                    keterangan = "Pesan Grosir",
                    jumlahHutang = "Rp 1.100.000",
                    tanggalJatuhTempo = "05 Nov 2023",
                    warnaJatuhTempo = Color(0xFFEF4444),
                    warnaBgJatuhTempo = Color(0xFFFEE2E2),
                    onPengingat = {},
                    onBayarHutang = {}
                )
            }
        }
    }
}

// Composable: Card item hutang

@Composable
fun HutangCardItem(
    inisial: String,
    warnaBg: Color,
    namaCustomer: String,
    keterangan: String,
    jumlahHutang: String,
    tanggalJatuhTempo: String,
    warnaJatuhTempo: Color,
    warnaBgJatuhTempo: Color,
    onPengingat: () -> Unit,
    onBayarHutang: () -> Unit
)
{
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Baris atas: avatar + nama + jumlah + tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar inisial
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(warnaBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = inisial,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                // Nama + keterangan
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = namaCustomer,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = keterangan,
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // Jumlah + badge tanggal
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = jumlahHutang,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF111827)
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = warnaBgJatuhTempo,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tanggalJatuhTempo,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = warnaJatuhTempo
                        )
                    }
                }
            }

            // Baris bawah: tombol Pengingat + Bayar Hutang
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPengingat,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF374151)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pengingat",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onBayarHutang,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bayar Hutang",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ManajemenHutangPreview() {
    ManajemenHutangScreen()

}