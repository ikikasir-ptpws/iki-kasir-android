package com.example.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarProdukScreen(
    navController: NavController,
    onBack: () -> Unit = {},
    onTambah: () -> Unit = {}
) {
    var cariproduk by remember { mutableStateOf("") }
    var selectedKategori by remember { mutableStateOf("Semua") }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daftar Produk",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 20.sp, color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {if (navController.currentDestination?.route == "produk") {
                        navController.popBackStack()
                    }}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
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
                onClick = onTambah,
                modifier = Modifier.padding(bottom = 86.dp),
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Produk"
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
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Search Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF2F3F5)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    BasicTextField(
                        value = cariproduk,
                        onValueChange = { cariproduk = it },
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
                                    if (cariproduk.isEmpty()) {
                                        Text(
                                            text = "Cari Produk",
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

            //  Filter Kategori
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(4) { index ->
                        val label = listOf("Semua", "Kopi", "Non-Coffee", "Makanan")[index]
                        val isSelected = label == selectedKategori
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedKategori = label },
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

            // Card 1: Nasi Goreng Jumbo
            item {
                ProdukCardItem(
                    nama = "Nasi Goreng Jumbo level 1",
                    stok = 45,
                    harga = "Rp 18.000",
                    painter = painterResource(R.drawable.kopi),
                    isStokRendah = false,
                    onEdit = {},
                    onDelete = {}
                )
            }

            //  Card 2: Matcha Latte Cream
            item {
                ProdukCardItem(
                    nama = "Matcha Latte Cream",
                    stok = 2,
                    harga = "Rp 12.000",
                    painter = painterResource(R.drawable.kopi),
                    isStokRendah = true,
                    onEdit = {},
                    onDelete = {}
                )
            }

            //  Card 3: Roti Bakar Cokelat
            item {
                ProdukCardItem(
                    nama = "Roti Bakar Cokelat",
                    stok = 12,
                    harga = "Rp 15.000",
                    painter = painterResource(R.drawable.kopi),
                    isStokRendah = false,
                    onEdit = {},
                    onDelete = {}
                )
            }

            // Card 4: Kopi Susu Gula Aren
            item {
                ProdukCardItem(
                    nama = "Kopi Susu Gula Aren",
                    stok = 30,
                    harga = "Rp 20.000",
                    painter = painterResource(R.drawable.kopi),
                    isStokRendah = false,
                    onEdit = {},
                    onDelete = {}
                )
            }

            // Card 5: Es Teh Manis
            item {
                ProdukCardItem(
                    nama = "Es Teh Manis",
                    stok = 50,
                    harga = "Rp 8.000",
                    painter = painterResource(R.drawable.kopi),
                    isStokRendah = false,
                    onEdit = {},
                    onDelete = {}
                )
            }
        }
    }
}

// Produk Card Item

@Composable
fun ProdukCardItem(
    nama: String,
    stok: Int,
    harga: String,
    painter: Painter,
    isStokRendah: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // Badge Stok Rendah di pojok kanan atas
            if (isStokRendah) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 12.dp, topEnd = 16.dp))
                        .background(Color(0xFFEF4444))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "STOK RENDAH",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Gambar Produk
                Image(
                    painter = painter,
                    contentDescription = nama,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE5E7EB))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Info Produk
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = nama,
                        fontWeight = FontWeight.Medium,
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        color = Color(0xFF111827),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Stok: $stok Unit",
                        fontSize = 13.sp,
                        fontFamily = interfamily,
                        color = if (isStokRendah) Color(0xFFDC2626) else Color(0xFF6B7280),
                        fontWeight = if (isStokRendah) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        text = harga,
                        fontSize = 14.sp,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                }

                // Tombol Edit & Hapus — Row horizontal sesuai gambar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.iconedit) ,
                            contentDescription = "Edit",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.iconhapus),
                            contentDescription = "Hapus",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

//  Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DaftarProdukScreenPreview() {
    MaterialTheme {
        DaftarProdukScreen(navController = rememberNavController())
    }
}