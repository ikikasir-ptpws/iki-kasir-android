package com.ptpws.ikikasir.screens.manajemenstok


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.ptpws.ikikasir.screens.manajemenstok.component.DialogPindahStok
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
fun StokEtalaseScreen(
    onBack: () -> Unit = {},
    onTambah: () -> Unit = {}
) {
    var cariproduketalase by remember { mutableStateOf("") }
    var showDialogPindah by remember { mutableStateOf(false) }
    var selectedProductName by remember { mutableStateOf("") }
    var selectedProductStock by remember { mutableIntStateOf(0) }
    var selectedProductImage by remember { mutableIntStateOf(R.drawable.kopi) }

    if (showDialogPindah) {
        DialogPindahStok(
            title = "Pindah ke Gudang",
            productName = selectedProductName,
            stockLabel = "Stok Etalase",
            maxStock = selectedProductStock,
            imagePainter = painterResource(id = selectedProductImage),
            onDismiss = { showDialogPindah = false },
            onConfirm = { amount -> 
                // Handle transfer logic here
                showDialogPindah = false 
            }
        )
    }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            contentPadding = PaddingValues(
                top = 16.dp, bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stok Menipis
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "STOK MENIPIS",
                                fontSize = 10.sp,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9CA3AF),
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "12",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Total Unit
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4F46E5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "TOTAL UNIT",
                                fontSize = 10.sp,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBFBFFF),
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "1,402",
                                    fontSize = 30.sp,
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    painter = painterResource(R.drawable.stokk),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
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
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF2F3F5)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    BasicTextField(
                        value = cariproduketalase,
                        onValueChange = { cariproduketalase = it },
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
                                    if (cariproduketalase.isEmpty()) {
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

            // Header Daftar Produk
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Produk",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        color = Color(0xFF111827)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FILTER KATEGORI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }
            }

            // Card 1: Kopi Arabika Toraja
            item {
                EtalaseCardItem(
                    nama = "Kopi Arabika Toraja 250g Special Roast",
                    sku = "SKU: ARB-TRJ-250",
                    sisaStok = "42",
                    isKritis = false,
                    painter = painterResource(R.drawable.kopi),
                    onRestock = {},
                    onPindahKeGudang = {
                        selectedProductName = "Kopi Arabika Toraja 250g Special Roast"
                        selectedProductStock = 42
                        selectedProductImage = R.drawable.kopi
                        showDialogPindah = true
                    },
                    onLihat = {}
                )
            }

            // Card 2: Gula Semut Organik
            item {
                EtalaseCardItem(
                    nama = "Gula Semut Organik 500g",
                    sku = "SKU: ORG-GUL-500",
                    sisaStok = "5",
                    isKritis = true,
                    painter = painterResource(R.drawable.kopi),
                    onRestock = {},
                    onPindahKeGudang = {
                        selectedProductName = "Gula Semut Organik 500g"
                        selectedProductStock = 5
                        selectedProductImage = R.drawable.kopi
                        showDialogPindah = true
                    },
                    onLihat = {}
                )
            }

            // Card 3: Teh Melati Premium
            item {
                EtalaseCardItem(
                    nama = "Teh Melati Premium Pack",
                    sku = "SKU: JSM-TEH-PRE",
                    sisaStok = "128",
                    isKritis = false,
                    painter = painterResource(R.drawable.kopi),
                    onRestock = {},
                    onPindahKeGudang = {
                        selectedProductName = "Teh Melati Premium Pack"
                        selectedProductStock = 128
                        selectedProductImage = R.drawable.kopi
                        showDialogPindah = true
                    },
                    onLihat = {}
                )
            }

            // Card 3: Teh Melati Premium
            item {
                EtalaseCardItem(
                    nama = "Teh Melati Premium Pack",
                    sku = "SKU: JSM-TEH-PRE",
                    sisaStok = "128",
                    isKritis = false,
                    painter = painterResource(R.drawable.kopi),
                    onRestock = {},
                    onPindahKeGudang = {
                        selectedProductName = "Teh Melati Premium Pack"
                        selectedProductStock = 128
                        selectedProductImage = R.drawable.kopi
                        showDialogPindah = true
                    },
                    onLihat = {}
                )
            }
        }
    }

// Etalase Card Item

@Composable
fun EtalaseCardItem(
    nama: String,
    sku: String,
    sisaStok: String,
    isKritis: Boolean,
    painter: Painter,
    onRestock: () -> Unit,
    onPindahKeGudang: () -> Unit,
    onLihat: () -> Unit
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
        Row(modifier = Modifier.fillMaxWidth()) {

            // Garis merah kritis di sisi kiri
            if (isKritis) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(160.dp)
                        .background(
                            color = Color(0xFFEF4444),
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // Baris atas: gambar + info + stok
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = nama,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE5E7EB))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nama,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            fontFamily = interfamily,
                            color = Color(0xFF111827),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sku,
                            fontSize = 12.sp,
                            fontFamily = interfamily,
                            color = Color(0xFF9CA3AF)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sisa Stok
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isKritis) "KRITIS" else "SISA\nSTOK",
                            fontSize = 10.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isKritis) Color(0xFFEF4444) else Color(0xFF9CA3AF),
                            letterSpacing = 0.3.sp,
                            lineHeight = 12.sp
                        )
                        Text(
                            text = sisaStok,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isKritis) Color(0xFFEF4444) else Color(0xFF111827)
                        )
                    }
                }

                // Baris bawah: tombol aksi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onRestock,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "RESTOCK",
                            fontSize = 12.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onPindahKeGudang,
                        modifier = Modifier.weight(1.4f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "PINDAHKAN KE GUDANG",
                            fontSize = 11.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedIconButton(
                        onClick = onLihat,
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Lihat",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StokEtalaseScreenPreview() {
    MaterialTheme {
        StokEtalaseScreen()
    }
}