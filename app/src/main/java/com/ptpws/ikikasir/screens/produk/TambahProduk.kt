package com.example.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahProdukScreen(
    onBack: () -> Unit = {},
    onSimpanDraft: () -> Unit = {},
    onSimpanProduk: () -> Unit = {}
) {
    var namaProduk by remember { mutableStateOf("") }
    var hargaJual by remember { mutableStateOf("0") }
    var stok by remember { mutableStateOf("0") }
    var selectedKategori by remember { mutableStateOf("Makanan") }
    var diskonProduk by remember { mutableStateOf("0") }
    var diskonProdukMode by remember { mutableStateOf("%") }
    var diskonMember by remember { mutableStateOf("0") }
    var diskonMemberMode by remember { mutableStateOf("%") }
    var lokasiPenempatan by remember { mutableStateOf("Gudang") }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tambah Produk",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 20.sp, color = Color.Black
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
                    containerColor = Color(0xFFF3F4F6),
                    titleContentColor = Color(0xFF111827),
                    navigationIconContentColor = Color(0xFF4F46E5)
                )
            )
        },

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

            // ── Upload Foto Produk
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(
                            width = 1.5.dp,
                            color = Color(0xFFD1D5DB),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Tambah Foto",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Text(
                            text = "Tambah Foto Produk (opsional)",
                            fontFamily = interfamily,
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // ── Format Info
            item {
                Text(
                    text = "Format JPG, PNG atau HEIC. Maks 5MB.",
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                )
            }

            // ── Nama Produk
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Nama Produk",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = namaProduk,
                                onValueChange = { namaProduk = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontFamily = interfamily
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (namaProduk.isEmpty()) {
                                        Text(
                                            text = "Contoh: Kopi Susu Gula Aren",
                                            fontSize = 14.sp,
                                            fontFamily = interfamily,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }
            }

            // ── Harga Jual & Stok
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Harga Jual",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rp",
                                    fontFamily = interfamily,
                                    fontSize = 15.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = hargaJual,
                                    onValueChange = { hargaJual = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.Black,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = interfamily
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Stok",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = stok,
                                    onValueChange = { stok = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.Black,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = interfamily
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // ── Pilih Kategori Produk
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pilih Kategori Produk",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(4) { index ->
                            val label = listOf("Makanan", "Minuman", "Snack", "Dessert")[index]
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
                        item {
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = "Tambah Kategori",
                                        fontFamily = interfamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFF374151)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tambah Kategori",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF374151)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color.White
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = Color(0xFFE5E7EB)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            // ── Pengaturan Diskon
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sell,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Pengaturan Diskon",
                                fontFamily = interfamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                        }

                        // Diskon Produk
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row {
                                Text(
                                    text = "Diskon Produk ",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    color = Color(0xFF374151)
                                )
                                Text(
                                    text = "*opsional",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        BasicTextField(
                                            value = diskonProduk,
                                            onValueChange = { diskonProduk = it },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                color = Color.Black,
                                                fontSize = 14.sp,
                                                fontFamily = interfamily
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (diskonProdukMode == "%") Color(0xFF4F46E5) else Color.Transparent
                                            )
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "%",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (diskonProdukMode == "%") Color.White else Color(0xFF374151),
                                            modifier = Modifier.clickable { diskonProdukMode = "%" }
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (diskonProdukMode == "Rp") Color(0xFF4F46E5) else Color.Transparent
                                            )
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Rp",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (diskonProdukMode == "Rp") Color.White else Color(0xFF374151),
                                            modifier = Modifier.clickable { diskonProdukMode = "Rp" }
                                        )
                                    }
                                }
                            }
                        }

                        // Diskon Member
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row {
                                Text(
                                    text = "Diskon Member ",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    color = Color(0xFF374151)
                                )
                                Text(
                                    text = "*opsional",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        BasicTextField(
                                            value = diskonMember,
                                            onValueChange = { diskonMember = it },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                color = Color.Black,
                                                fontSize = 14.sp,
                                                fontFamily = interfamily
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (diskonMemberMode == "%") Color(0xFF4F46E5) else Color.Transparent
                                            )
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "%",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (diskonMemberMode == "%") Color.White else Color(0xFF374151),
                                            modifier = Modifier.clickable { diskonMemberMode = "%" }
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (diskonMemberMode == "Rp") Color(0xFF4F46E5) else Color.Transparent
                                            )
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Rp",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (diskonMemberMode == "Rp") Color.White else Color(0xFF374151),
                                            modifier = Modifier.clickable { diskonMemberMode = "Rp" }
                                        )
                                    }
                                }
                            }
                        }

                        // Lokasi Penempatan
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Lokasi Penempatan",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                color = Color(0xFF374151)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (lokasiPenempatan == "Gudang") Color(0xFF4F46E5) else Color(0xffEFF4FF)
                                        )
                                        .clickable { lokasiPenempatan = "Gudang" }
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warehouse,
                                        contentDescription = "Gudang",
                                        tint = if (lokasiPenempatan == "Gudang") Color.White else Color(0xFF374151),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Gudang",
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (lokasiPenempatan == "Gudang") Color.White else Color(0xFF374151)
                                    )
                                }



                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (lokasiPenempatan == "Etalase") Color(0xFF4F46E5) else Color(0xffEFF4FF)
                                        )
                                        .clickable { lokasiPenempatan = "Etalase" }
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = "Etalase",
                                        tint = if (lokasiPenempatan == "Etalase") Color.White else Color(0xFF374151),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Etalase",
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (lokasiPenempatan == "Etalase") Color.White else Color(0xFF374151)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(54.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSimpanDraft,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF374151)
                        )
                    ) {
                        Text(
                            text = "Simpan Draft",
                            fontFamily = interfamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Button(
                        onClick = onSimpanProduk,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simpan Produk",
                            fontFamily = interfamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ── Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TambahProdukScreenPreview() {
    MaterialTheme {
        TambahProdukScreen()
    }
}