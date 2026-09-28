package com.ptpws.ikikasir.screens.promo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily

data class PromoItem(
    val id: String,
    val namaPromo: String,
    val nilaiPromo: String,
    val infoTanggal: String,
    val isBerakhirHariIni: Boolean = false,
    val isMulaiNanti: Boolean = false,
    val isSelesai: Boolean = false,
    val isActive: Boolean = true,
    val statusFilter: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenPromoScreen(
    onBack: () -> Unit = {},
    onTambahPromo: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }

    var promoList by remember {
        mutableStateOf(
            listOf(
                PromoItem(
                    id = "1",
                    namaPromo = "Promo Bundling Hemat",
                    nilaiPromo = "Potongan Rp 25.000",
                    infoTanggal = "Hingga 31 Mei 2024",
                    isActive = true,
                    statusFilter = "Aktif"
                ),
                PromoItem(
                    id = "2",
                    namaPromo = "Diskon Spesial Gajian",
                    nilaiPromo = "Diskon 20% (Maks Rp 50rb)",
                    infoTanggal = "Hingga 31 Mei 2024",
                    isActive = true,
                    statusFilter = "Aktif"
                ),
                PromoItem(
                    id = "3",
                    namaPromo = "Kopi Kenikmatan Sore",
                    nilaiPromo = "Potongan Rp 5.000 / cup",
                    infoTanggal = "Berakhir Hari Ini (23:59 WIB)",
                    isBerakhirHariIni = true,
                    isActive = true,
                    statusFilter = "Aktif"
                ),
                PromoItem(
                    id = "4",
                    namaPromo = "Flash Sale Awal Bulan",
                    nilaiPromo = "Diskon Rp 15.000",
                    infoTanggal = "Mulai 31 Mei 2024",
                    isMulaiNanti = true,
                    isActive = false,
                    statusFilter = "Akan Datang"
                ),
                PromoItem(
                    id = "5",
                    namaPromo = "Diskon Akhir Pekan Ramadan",
                    nilaiPromo = "Potongan Rp 30.000",
                    infoTanggal = "SELESAI",
                    isSelesai = true,
                    isActive = false,
                    statusFilter = "Kedaluwarsa"
                )
            )
        )
    }

    var promoToDelete by remember { mutableStateOf<PromoItem?>(null) }

    val filteredList = remember(searchQuery, selectedFilter, promoList) {
        promoList.filter { item ->
            val matchSearch = searchQuery.isEmpty() ||
                    item.namaPromo.contains(searchQuery, ignoreCase = true) ||
                    item.nilaiPromo.contains(searchQuery, ignoreCase = true)
            val matchFilter = when (selectedFilter) {
                "Aktif" -> item.statusFilter == "Aktif"
                "Akan Datang" -> item.statusFilter == "Akan Datang"
                "Kedaluwarsa" -> item.statusFilter == "Kedaluwarsa"
                else -> true
            }
            matchSearch && matchFilter
        }
    }

    val promoAktifCount = remember(promoList) { promoList.count { it.isActive } }
    val berakhirMingguIniCount = remember(promoList) { promoList.count { it.isBerakhirHariIni || (it.isActive && it.infoTanggal.contains("Mei")) } }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daftar Promo",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF0F172A),
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                onClick = onTambahPromo,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Tambah Promo",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. Search Bar ─────────────────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            ),
                            cursorBrush = SolidColor(Color(0xFF2563EB)),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari promo ..",
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            // ── 2. Stat Cards (Promo Aktif & Berakhir Minggu Ini) ─────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card Promo Aktif
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4F46E5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PROMO AKTIF",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$promoAktifCount",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp,
                                    color = Color.White
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    // Card Berakhir Minggu Ini
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "BERAKHIR\nMINGGU INI",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFFEF4444),
                                    letterSpacing = 0.5.sp,
                                    lineHeight = 13.sp
                                )
                                Text(
                                    text = "$berakhirMingguIniCount",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFEF4444).copy(alpha = 0.35f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }

            // ── 3. Filter Chips ──────────────────────────────────────────
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    val filters = listOf("Semua", "Aktif", "Akan Datang", "Kedaluwarsa")
                    items(filters) { filterText ->
                        val isSelected = selectedFilter == filterText
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedFilter = filterText }
                        ) {
                            Text(
                                text = filterText,
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // ── 4. Promo Items List ──────────────────────────────────────
            items(items = filteredList, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Text Info
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            // Subtitle / Nama Promo
                            Text(
                                text = item.namaPromo,
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.isSelesai) Color(0xFF94A3B8) else Color(0xFF334155)
                            )
                            // Main Value Title
                            Text(
                                text = item.nilaiPromo,
                                fontFamily = interfamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    item.isSelesai -> Color(0xFF94A3B8)
                                    item.isMulaiNanti -> Color(0xFF0F172A)
                                    else -> Color(0xFF2563EB)
                                }
                            )
                            // Info Tanggal / Status
                            if (item.isSelesai) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "SELESAI",
                                        fontFamily = interfamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = item.infoTanggal,
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (item.isBerakhirHariIni) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (item.isBerakhirHariIni) Color(0xFFEF4444) else Color(0xFF64748B)
                                )
                            }
                        }

                        // Right Controls Row (Switch + Edit + Delete + Chevron)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Switch (only if not completed/expired)
                            if (!item.isSelesai) {
                                Switch(
                                    checked = item.isActive,
                                    onCheckedChange = { checked ->
                                        promoList = promoList.map {
                                            if (it.id == item.id) it.copy(isActive = checked) else it
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2563EB),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFCBD5E1),
                                        uncheckedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.scale(0.85f)
                                )
                            }

                            // Edit Icon
                            IconButton(
                                onClick = onTambahPromo,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Promo",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete Icon
                            IconButton(
                                onClick = { promoToDelete = item },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Promo",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Chevron Right Icon
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    promoToDelete?.let { targetPromo ->
        AlertDialog(
            onDismissRequest = { promoToDelete = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Hapus Promo",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus promo \"${targetPromo.namaPromo}\"?",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        promoList = promoList.filter { it.id != targetPromo.id }
                        promoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Hapus",
                        color = Color.White,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { promoToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Batal",
                        fontFamily = interfamily,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ManajemenPromoScreenPreview() {
    ManajemenPromoScreen()
}