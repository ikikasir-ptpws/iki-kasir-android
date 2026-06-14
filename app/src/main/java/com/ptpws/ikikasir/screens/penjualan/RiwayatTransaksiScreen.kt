package com.ptpws.ikikasir.screens.penjualan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
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
fun RiwayatTransaksiScreen(
    onBack: () -> Unit = {},
    onDetailTransaksi: (String) -> Unit = {}
) {
    var cariTransaksi by remember { mutableStateOf("") }
    var filterAktif by remember { mutableStateOf("Hari Ini") }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Riwayat Transaksi",
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

            // Search Bar + Icon QR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search TextField
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        BasicTextField(
                            value = cariTransaksi,
                            onValueChange = { cariTransaksi = it },
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
                                        if (cariTransaksi.isEmpty()) {
                                            Text(
                                                text = "Cari kode transaksi...",
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

                    // Tombol QR Scan
                    Card(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Scan QR",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Filter Chip: Hari Ini
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    // Chip: Hari Ini
                    item {
                        val isSelected = filterAktif == "Hari Ini"
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterAktif = "Hari Ini" },
                            label = {
                                Text(
                                    text = "Hari Ini",
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

                    // Chip: 7 Hari Terakhir
                    item {
                        val isSelected = filterAktif == "7 Hari Terakhir"
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterAktif = "7 Hari Terakhir" },
                            label = {
                                Text(
                                    text = "7 Hari Terakhir",
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

                    // Chip: Pilih Tanggal (dengan icon kalender)
                    item {
                        val isSelected = filterAktif == "Pilih Tanggal"
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterAktif = "Pilih Tanggal" },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Pilih Tanggal",
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
                                labelColor = Color(0xFF374151),
                                iconColor = Color(0xFF374151)
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


            item {
                Text(
                    text = "25 Okt 2026",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                )
            }

            // Transaksi #TRX-2023-042
            item {
                TransaksiCardItem(
                    kodeTransaksi = "#TRX-2023-042",
                    jam = "14:30",
                    metodePembayaran = "Cash",
                    iconMetode = Icons.Default.Wallet,
                    iconTint = Color(0xFF4F46E5),
                    totalHarga = "Rp 150.000",
                    statusBayar = "LUNAS",
                    onDetail = { onDetailTransaksi("#TRX-2023-042") }
                )
            }

            // Transaksi #TRX-2023-041
            item {
                TransaksiCardItem(
                    kodeTransaksi = "#TRX-2023-041",
                    jam = "12:15",
                    metodePembayaran = "QRIS",
                    iconMetode = Icons.Default.QrCode,
                    iconTint = Color(0xFF4F46E5),
                    totalHarga = "Rp 42.500",
                    statusBayar = "LUNAS",
                    onDetail = { onDetailTransaksi("#TRX-2023-041") }
                )
            }


            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "24 Okt 2026",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                )
            }

            // Transaksi #TRX-2023-040
            item {
                TransaksiCardItem(
                    kodeTransaksi = "#TRX-2023-040",
                    jam = "18:45",
                    metodePembayaran = "Debit Card",
                    iconMetode = Icons.Default.CreditCard,
                    iconTint = Color(0xFF4F46E5),
                    totalHarga = "Rp 1.250.000",
                    statusBayar = "HUTANG",
                    onDetail = { onDetailTransaksi("#TRX-2023-040") }
                )
            }

            // Transaksi #TRX-2023-039
            item {
                TransaksiCardItem(
                    kodeTransaksi = "#TRX-2023-039",
                    jam = "09:10",
                    metodePembayaran = "Cash",
                    iconMetode = Icons.Default.Wallet,
                    iconTint = Color(0xFF4F46E5),
                    totalHarga = "Rp 8.000",
                    statusBayar = "LUNAS",
                    onDetail = { onDetailTransaksi("#TRX-2023-039") }
                )
            }
        }
    }
}

@Composable
fun TransaksiCardItem(
    kodeTransaksi: String,
    jam: String,
    metodePembayaran: String,
    iconMetode: ImageVector,
    iconTint: Color,
    totalHarga: String,
    statusBayar: String,
    onDetail: () -> Unit
) {
    val isLunas = statusBayar == "LUNAS"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        onClick = onDetail
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kode transaksi (bold indigo)
                Text(
                    text = kodeTransaksi,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    color = Color(0xFF4F46E5)
                )

                // Jam
                Text(
                    text = "  $jam",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Badge Status
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isLunas) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusBayar,
                        fontSize = 11.sp,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isLunas) Color(0xFF059669) else Color(0xFFD97706)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Arrow
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Detail",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(14.dp)
                )
            }

            // Baris tengah: icon metode + nama metode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = iconMetode,
                    contentDescription = metodePembayaran,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = metodePembayaran,
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    color = Color(0xFF374151)
                )
            }

            // Baris bawah: total harga (bold besar)
            Text(
                text = totalHarga,
                fontWeight = FontWeight.Bold,
                fontFamily = interfamily,
                fontSize = 20.sp,
                color = Color(0xFF111827)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RiwayatTransaksiScreenPreview() {
    MaterialTheme {
        RiwayatTransaksiScreen()
    }
}