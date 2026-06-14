package com.ptpws.ikikasir.ui.screens.kasir

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
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
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirScreen(
    onBack: () -> Unit = {},
    onScanProduk: () -> Unit = {},
    onTambahProdukBaru: () -> Unit = {},
    onBayar: () -> Unit = {}
) {
    var cariprodukkasir by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kasir Pintar",
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

            // ── Search Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F3F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    BasicTextField(
                        value = cariprodukkasir,
                        onValueChange = { cariprodukkasir = it },
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
                                    if (cariprodukkasir.isEmpty()) {
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

            //  Header Daftar Produk
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DAFTAR PRODUK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "12 Produk Tersedia",
                            fontSize = 12.sp,
                            fontFamily = interfamily,
                            color = Color(0xFF6B7280)
                        )
                    }
                    Text(
                        text = "Filter",
                        fontSize = 13.sp,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4F46E5)
                    )
                }
            }

            // Card 1: Nasi Goreng Mawut
            item {
                KasirProdukCard(
                    nama = "Nasi Goreng Mawut ....",
                    stok = 45,
                    isStokRendah = false,
                    harga = "Rp 18.000",
                    painter = painterResource(R.drawable.kopi)
                )
            }

            // Card 2: Roti Bakar Cokelat
            item {
                KasirProdukCard(
                    nama = "Roti Bakar Cokelat",
                    stok = 12,
                    isStokRendah = true,
                    harga = "Rp 15.000",
                    painter = painterResource(R.drawable.kopi)
                )
            }

            // Card 3: Matcha Latte Cream
            item {
                KasirProdukCard(
                    nama = "Matcha Latte Cream",
                    stok = 2,
                    isStokRendah = true,
                    harga = "Rp 12.000",
                    painter = painterResource(R.drawable.kopi)
                )
            }

            //  Card 4: Kopi Susu Gula Aren
            item {
                KasirProdukCard(
                    nama = "Kopi Susu Gula Aren",
                    stok = 30,
                    isStokRendah = false,
                    harga = "Rp 20.000",
                    painter = painterResource(R.drawable.kopi)
                )
            }

            // Card 5: Es Teh Manis
            item {
                KasirProdukCard(
                    nama = "Es Teh Manis",
                    stok = 50,
                    isStokRendah = false,
                    harga = "Rp 8.000",
                    painter = painterResource(R.drawable.kopi)
                )
            }

            //  Tombol SCAN PRODUK
            item {
                Button(
                    onClick = onScanProduk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text(
                        text = "SCAN PRODUK",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Tombol TAMBAH PRODUK BARU
            item {
                OutlinedButton(
                    onClick = onTambahProdukBaru,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFD1D5DB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "TAMBAH PRODUK BARU",
                            fontSize = 12.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onBayar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text(
                        text = "BAYAR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun KasirProdukCard(
    nama: String,
    stok: Int,
    isStokRendah: Boolean,
    harga: String,
    painter: Painter
) {
    var qty by remember { mutableStateOf(1) }

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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Gambar Produk
            Image(
                painter = painter,
                contentDescription = nama,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE5E7EB))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Info Produk
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nama,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = interfamily,
                    fontSize = 15.sp,
                    color = Color(0xFF111827),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Badge Stok
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Stok: ",
                        fontSize = 12.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF6B7280)
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isStokRendah) Color(0xFFFEE2E2) else Color(0xFFEEF2FF),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$stok UNIT",
                            fontSize = 11.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isStokRendah) Color(0xFFEF4444) else Color(0xFF4F46E5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = harga,
                    fontSize = 14.sp,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quantity Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFE5E7EB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { if (qty > 1) qty-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Kurang",
                            tint = Color(0xFF374151),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = qty.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    color = Color(0xFF111827)
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF4F46E5), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { if (qty < stok) qty++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KasirScreenPreview() {
    MaterialTheme {
        KasirScreen()
    }
}