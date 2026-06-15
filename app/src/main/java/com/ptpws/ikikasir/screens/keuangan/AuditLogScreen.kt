package com.ptpws.ikikasir.screens.keuangan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(
    onBack: () -> Unit = {}
) {
    var cariAktivitas by remember { mutableStateOf("") }
    var filterKategori by remember { mutableStateOf("Semua") }
    var filterTanggal by remember { mutableStateOf("Hari Ini") }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Auditlog",
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
                        value = cariAktivitas,
                        onValueChange = { cariAktivitas = it },
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
                                    if (cariAktivitas.isEmpty()) {
                                        Text(
                                            text = "Cari aktivitas atau staf...",
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

            //  Filter Kategori Chip Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(4) { index ->
                        val label = listOf("Semua", "Transaksi", "Stok", "Harga")[index]
                        val isSelected = filterKategori == label
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterKategori = label },
                            label = {
                                Text(
                                    text = label,
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

            // Filter Tanggal
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FILTER TANGGAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(3) { index ->
                            val label = listOf("Hari Ini", "7 Hari Terakhir", "Pilih Tanggal")[index]
                            val isSelected = filterTanggal == label
                            FilterChip(
                                selected = isSelected,
                                onClick = { filterTanggal = label },
                                label = {
                                    Text(
                                        text = label,
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
            }


            // Log 1: Transaksi Baru
            item {
                AuditlogItem(
                    icon = Icons.Default.Description,
                    iconBgColor = Color(0xFF4F46E5),
                    iconTint = Color.White,
                    judul = "Transaksi Baru: #TRX-9021",
                    jam = "14:20",
                    deskripsi = "Penjualan retail 3 item selesai.",
                    avatarInisial = "KS",
                    avatarBgColor = Color(0xFF6B7280),
                    namaStaf = "Kasir Siti",
                    isBahaya = false
                )
            }

            // Log 2: Penambahan Stok
            item {
                AuditlogItem(
                    icon = Icons.Default.Inventory,
                    iconBgColor = Color(0xFFE5E7EB),
                    iconTint = Color(0xFF374151),
                    judul = "Penambahan Stok: Minyak Gore",
                    jam = "11:05",
                    deskripsi = "Stok ditambahkan sebanyak 24 unit ke Gudang A.",
                    avatarInisial = "AA",
                    avatarBgColor = Color(0xFF7C3AED),
                    namaStaf = "Admin Ahmad",
                    isBahaya = false
                )
            }

            // Log 3: Perubahan Harga
            item {
                AuditlogItem(
                    icon = Icons.Default.LocalOffer,
                    iconBgColor = Color(0xFFE5E7EB),
                    iconTint = Color(0xFF374151),
                    judul = "Perubahan Harga: Beras Premiu...",
                    jam = "09:15",
                    deskripsi = "Harga diubah dari Rp 65.000 ke Rp 68.500.",
                    avatarInisial = "MB",
                    avatarBgColor = Color(0xFFD97706),
                    namaStaf = "Manager Budi",
                    isBahaya = false
                )
            }

            //  Separator Tanggal
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFE5E7EB)
                    )
                    Text(
                        text = "KEMARIN — 23 OKT 2023",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFE5E7EB)
                    )
                }
            }


            // Log 4: Sesi Berakhir
            item {
                AuditlogItem(
                    icon = Icons.Default.Logout,
                    iconBgColor = Color(0xFFE5E7EB),
                    iconTint = Color(0xFF374151),
                    judul = "Sesi Berakhir: Kasir Siti",
                    jam = "21:00",
                    deskripsi = "Logout sistem otomatis - Tutup Shift.",
                    avatarInisial = "KS",
                    avatarBgColor = Color(0xFF059669),
                    namaStaf = "Kasir Siti",
                    isBahaya = false
                )
            }

            // Log 5: Pembatalan Transaksi (bahaya - merah)
            item {
                AuditlogItem(
                    icon = Icons.Default.Cancel,
                    iconBgColor = Color(0xFFFEE2E2),
                    iconTint = Color(0xFFEF4444),
                    judul = "Pembatalan Transaksi: #TRX-8...",
                    jam = "18:45",
                    deskripsi = "Alasan: Kesalahan input jumlah item.",
                    avatarInisial = "SL",
                    avatarBgColor = Color(0xFFEF4444),
                    namaStaf = "Supervisor Linda",
                    isBahaya = true
                )
            }
        }
    }
}

// Auditlog Item

@Composable
fun AuditlogItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    judul: String,
    jam: String,
    deskripsi: String,
    avatarInisial: String,
    avatarBgColor: Color,
    namaStaf: String,
    isBahaya: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBahaya) Color(0xFFFFF5F5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Icon kotak kiri
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Konten kanan
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                // Baris judul + jam
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = judul,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        color = if (isBahaya) Color(0xFFEF4444) else Color(0xFF111827),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = jam,
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // Deskripsi
                Text(
                    text = deskripsi,
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Baris avatar + nama staf
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Avatar inisial
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(avatarBgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarInisial,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Oleh: $namaStaf",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuditlogScreenPreview() {
    MaterialTheme {
        AuditLogScreen()
    }
}