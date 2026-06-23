package com.ptpws.ikikasir.screens.barangrusak

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

// ── Data Model

enum class StatusBarang {
    RUSAK, EXPIRED
}

data class BarangRusakExp(
    val id: String,
    val nama: String,
    val status: StatusBarang,
    val tanggal: String,
    val jumlah: Int,
    val lokasi: String
)

// ── Dummy Data

private fun dummyBarangRusakExp(): List<BarangRusakExp> = listOf(
    BarangRusakExp(
        id = "1",
        nama = "Kopi Arabika 250g",
        status = StatusBarang.RUSAK,
        tanggal = "12 Okt 2023",
        jumlah = 3,
        lokasi = "Gudang"
    ),
    BarangRusakExp(
        id = "2",
        nama = "Gelas Keramik Artisan",
        status = StatusBarang.RUSAK,
        tanggal = "08 Okt 2023",
        jumlah = 1,
        lokasi = "Gudang"
    ),
    BarangRusakExp(
        id = "3",
        nama = "Roti Tawar Gandum",
        status = StatusBarang.EXPIRED,
        tanggal = "05 Okt 2023",
        jumlah = 5,
        lokasi = "Gudang"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarangRusakExpScreen(
    navController: NavController,
    onTambahBarang: () -> Unit = {},
    onEditBarang: (BarangRusakExp) -> Unit = {},
    onHapusBarang: (BarangRusakExp) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val daftarBarang = remember { dummyBarangRusakExp() }

    val filteredBarang = remember(selectedFilter, daftarBarang) {
        when (selectedFilter) {
            "Rusak" -> daftarBarang.filter { it.status == StatusBarang.RUSAK }
            "Expired" -> daftarBarang.filter { it.status == StatusBarang.EXPIRED }
            else -> daftarBarang
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Barang Rusak/exp",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 20.sp, color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (navController.currentDestination?.route == "barang_rusak_exp") {
                        navController.popBackStack()
                    } }) {
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onTambahBarang,
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Barang"
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Judul & Subjudul
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "BARANG RUSAK/EXP",
                        fontFamily = interfamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Manajemen barang tidak layak jual",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // ── Filter Status
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(listOf("Semua", "Rusak", "Expired")) { label ->
                        val isSelected = label == selectedFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = label },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontFamily = interfamily,
                                    fontSize = 14.sp
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

            // ── Daftar Barang
            if (filteredBarang.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Belum ada barang",
                                fontFamily = interfamily,
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            } else {
                items(filteredBarang, key = { it.id }) { barang ->
                    BarangRusakExpCard(
                        barang = barang,
                        onEdit = { onEditBarang(barang) },
                        onHapus = { onHapusBarang(barang) }
                    )
                }
            }
        }
    }
}

// ── Card Item Barang

@Composable
private fun BarangRusakExpCard(
    barang: BarangRusakExp,
    onEdit: () -> Unit,
    onHapus: () -> Unit
) {
    val statusColor = when (barang.status) {
        StatusBarang.RUSAK -> Color(0xFFEF4444)
        StatusBarang.EXPIRED -> Color(0xFF111827)
    }
    val statusLabel = when (barang.status) {
        StatusBarang.RUSAK -> "RUSAK"
        StatusBarang.EXPIRED -> "EXPIRED"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = statusLabel,
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = barang.nama,
                    fontFamily = interfamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = barang.tanggal,
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${barang.jumlah} pcs",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warehouse,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = barang.lokasi,
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEFF4FF))
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEE2E2))
                        .clickable { onHapus() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.iconhapus),
                        contentDescription = "Hapus ${barang.nama}",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BarangRusakExpScreenPreview() {
    MaterialTheme {
        BarangRusakExpScreen(navController = rememberNavController())
    }
}