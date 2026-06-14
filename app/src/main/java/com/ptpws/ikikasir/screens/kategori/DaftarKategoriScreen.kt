package com.ptpws.ikikasir.screens.kategori

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarKategoriScreen(
    onBack: () -> Unit = {},
    onTambah: () -> Unit = {}
) {
    var carikategori by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kategori Produk",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Kategori"
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
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Search Bar
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
                        value = carikategori,
                        onValueChange = { carikategori = it },
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
                                    if (carikategori.isEmpty()) {
                                        Text(
                                            text = "Cari Produk",
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

            // Card 1: Kopi
            item {
                KategoriCardItem(
                    namaKategori = "Kopi",
                    jumlahProduk = "15 Produk",
                    icon = Icons.Default.LocalCafe,
                    iconBgColor = Color(0xFFEDE9FE),
                    iconTintColor = Color(0xFF4F46E5),
                    onEdit = {},
                    onDelete = {}
                )
            }

            // Card 2: Makanan
            item {
                KategoriCardItem(
                    namaKategori = "Makanan",
                    jumlahProduk = "24 Produk",
                    icon = Icons.Default.Restaurant,
                    iconBgColor = Color(0xFFEDE9FE),
                    iconTintColor = Color(0xFF4F46E5),
                    onEdit = {},
                    onDelete = {}
                )
            }

            // Card 3: Non-Coffee
            item {
                KategoriCardItem(
                    namaKategori = "Non-Coffee",
                    jumlahProduk = "12 Produk",
                    icon = Icons.Default.LocalBar,
                    iconBgColor = Color(0xFFFFF0EB),
                    iconTintColor = Color(0xFFEA580C),
                    onEdit = {},
                    onDelete = {}
                )
            }

            // Card 4: Snack
            item {
                KategoriCardItem(
                    namaKategori = "Snack",
                    jumlahProduk = "8 Produk",
                    icon = Icons.Default.Cookie,
                    iconBgColor = Color(0xFFEDE9FE),
                    iconTintColor = Color(0xFF4F46E5),
                    onEdit = {},
                    onDelete = {}
                )
            }

            //  Card 5: Dessert
            item {
                KategoriCardItem(
                    namaKategori = "Dessert",
                    jumlahProduk = "5 Produk",
                    icon = Icons.Default.Cake,
                    iconBgColor = Color(0xFFEDE9FE),
                    iconTintColor = Color(0xFF4F46E5),
                    onEdit = {},
                    onDelete = {}
                )
            }
        }
    }
}

// Kategori Card Item

@Composable
fun KategoriCardItem(
    namaKategori: String,
    jumlahProduk: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icon Kategori dengan background bulat
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconBgColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = namaKategori,
                    tint = iconTintColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Nama & Jumlah Produk
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = namaKategori,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = interfamily,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
                Text(
                    text = jumlahProduk,
                    fontSize = 13.sp,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280)
                )
            }

            // Tombol Edit & Hapus — Row horizontal
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.iconedit),
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
                       painter = painterResource(R.drawable.iconhapus),
                        contentDescription = "Hapus",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DaftarKategoriScreenPreview() {
    MaterialTheme {
        DaftarKategoriScreen()
    }
}