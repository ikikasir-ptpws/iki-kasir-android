package com.ptpws.ikikasir.screens.promo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

data class ProdukTerpilih(
    val nama: String,
    val sku: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahPromoScreen(
    onBack: () -> Unit = {},
    onSimpanPromo: () -> Unit = {}
) {
    var namaPromo by remember { mutableStateOf("") }
    var tipePromo by remember { mutableStateOf("Bundling") }
    var dropdownTipeExpanded by remember { mutableStateOf(false) }
    var cariProduk by remember { mutableStateOf("") }
    var nilaiDiskon by remember { mutableStateOf("0") }
    var tanggalMulai by remember { mutableStateOf("") }
    var tanggalBerakhir by remember { mutableStateOf("") }
    var deskripsiPromo by remember { mutableStateOf("") }
    var produkTerpilih by remember {
        mutableStateOf(
            listOf(
                ProdukTerpilih("Nike Air Max Red", "NK-AM-001"),
                ProdukTerpilih("Smartwatch Series X", "SW-SX-2024")
            )
        )
    }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tambah Promo",
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
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                // ── Card: Nama Promo & Tipe Promo
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

                        // Nama Promo
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Nama Promo",
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
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = namaPromo,
                                        onValueChange = { namaPromo = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontFamily = interfamily
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (namaPromo.isEmpty()) {
                                                Text(
                                                    text = "Contoh: Promo Gajian Akhir Bulan",
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

                        // Tipe Promo
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Tipe Promo",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151)
                            )
                            Box {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clickable { dropdownTipeExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = tipePromo,
                                            fontFamily = interfamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF111827)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Pilih Tipe Promo",
                                            tint = Color(0xFF6B7280),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = dropdownTipeExpanded,
                                    onDismissRequest = { dropdownTipeExpanded = false }
                                ) {
                                    listOf("Bundling", "Diskon Persentase", "Diskon Nominal", "Beli 1 Gratis 1").forEach { opsi ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = opsi,
                                                    fontFamily = interfamily,
                                                    fontSize = 14.sp
                                                )
                                            },
                                            onClick = {
                                                tipePromo = opsi
                                                dropdownTipeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                // ── Card: Pilih Produk
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Pilih Produk",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )

                        // Search Bar
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F3F5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            BasicTextField(
                                value = cariProduk,
                                onValueChange = { cariProduk = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontFamily = interfamily
                                ),
                                modifier = Modifier.fillMaxSize(),
                                decorationBox = { innerTextField ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = "Cari",
                                            tint = Color(0x80474747),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (cariProduk.isEmpty()) {
                                                Text(
                                                    text = "Cari produk",
                                                    fontSize = 13.sp,
                                                    fontFamily = interfamily,
                                                    color = Color(0x80474747)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                }
                            )
                        }

                        // Selected Products
                        Text(
                            text = "Selected Products",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            produkTerpilih.forEach { produk ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.kopi), // Use actual resource in your project
                                            contentDescription = produk.nama,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE5E7EB))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = produk.nama,
                                                fontFamily = interfamily,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF111827)
                                            )
                                            Text(
                                                text = "SKU: ${produk.sku}",
                                                fontFamily = interfamily,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                produkTerpilih = produkTerpilih.filter { it != produk }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.iconhapus),
                                                contentDescription = "Hapus ${produk.nama}",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                // ── Card: Nilai Diskon
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
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Nilai Diskon",
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .background(Color(0xFFEDE9FE)) // Light purple as seen in design
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Rp",
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4F46E5)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                BasicTextField(
                                    value = nilaiDiskon,
                                    onValueChange = { input ->
                                        if (input.isEmpty() || input.all { it.isDigit() }) {
                                            nilaiDiskon = input
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontFamily = interfamily
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // ── Card: Periode Promo
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Periode Promo",
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                BasicTextField(
                                    value = tanggalMulai,
                                    onValueChange = { tanggalMulai = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontFamily = interfamily
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (tanggalMulai.isEmpty()) {
                                            Text(
                                                text = "Mulai (DD/MM/YYYY)",
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

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                BasicTextField(
                                    value = tanggalBerakhir,
                                    onValueChange = { tanggalBerakhir = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontFamily = interfamily
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (tanggalBerakhir.isEmpty()) {
                                            Text(
                                                text = "Berakhir (DD/MM/YYYY)",
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
            }

            item {
                // ── Card: Deskripsi Promo
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
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Deskripsi Promo",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                BasicTextField(
                                    value = deskripsiPromo,
                                    onValueChange = { deskripsiPromo = it },
                                    textStyle = TextStyle(
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontFamily = interfamily
                                    ),
                                    modifier = Modifier.fillMaxSize(),
                                    decorationBox = { innerTextField ->
                                        if (deskripsiPromo.isEmpty()) {
                                            Text(
                                                text = "Tuliskan detail promo, syarat dan ketentuan di sini...",
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
            }

            item {
                Spacer(modifier = Modifier.height(31.dp))
                // ── Tombol Simpan Promo
                Button(
                    onClick = onSimpanPromo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    )
                ) {
                    Text(
                        text = "Simpan Promo",
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ── Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TambahPromoScreenPreview() {
    MaterialTheme {
        TambahPromoScreen()
    }
}