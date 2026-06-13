package com.example.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

@Composable
fun MenuFullScreen(
    onBack: () -> Unit = {},
    onItemClick: (String) -> Unit = {}
) {
    val queryState = remember { mutableStateOf("") }

    val sections = linkedMapOf(
        "Produk" to listOf(
            Triple(R.drawable.produk, "Produk", Color(0xFFF0FDF4)),
            Triple(R.drawable.kategori, "Kategori Produk", Color(0xFFFAF5FF)),
            Triple(R.drawable.manajemenstok, "Manajemen Stok", Color(0xFFFFF7ED)),
            Triple(R.drawable.barangrusak, "Barang Rusak Exp", Color(0xFFFDF2F8))
        ),
        "Penjualan" to listOf(
            Triple(R.drawable.kasirmenu, "Kasir", Color(0xFFEFF6FF)),
            Triple(R.drawable.transaksi, "Transaksi", Color(0xFFECFEFF))
        ),
        "Keuangan" to listOf(
            Triple(R.drawable.laporankeuangan, "Laporan Keuangan", Color(0xFFFFF0F6)),
            Triple(R.drawable.hutang, "Hutang", Color(0xFFFFF7ED)),
            Triple(R.drawable.auditlog, "Auditlog", Color(0xFFFFF6F0)),
            Triple(R.drawable.laporanpenjualan, "Laporan Penjualan", Color(0xFFEFFCF8))
        ),
        "Pengguna" to listOf(
            Triple(R.drawable.pengguna, "Pengguna", Color(0xFFEFF1FF)),
            Triple(R.drawable.pengaturanmenu, "Pengaturan Menu", Color(0xFFF4F7FF))
        )
    )

    Scaffold(
        containerColor = Color(0xFFF0F4FF)
    ) { paddingValues ->

        val query = queryState.value.trim()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4FF))
                .padding(paddingValues)
        ) {

            item {
                TopBar(
                    title = "Semua Menu",
                    onBack = onBack
                )

                Spacer(modifier = Modifier.height(12.dp))

                SearchField(
                    query = queryState.value,
                    onQueryChange = { queryState.value = it }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (query.isEmpty()) {

                sections.forEach { entry ->

                    item {
                        SectionTitle(title = entry.key)

                        MenuRowsTriple(
                            items = entry.value,
                            onItemClick = onItemClick
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

            } else {

                val filtered = sections
                    .flatMap { it.value }
                    .filter {
                        it.second.contains(query, ignoreCase = true)
                    }

                item {
                    SectionTitle(title = "Hasil")
                }

                if (filtered.isEmpty()) {

                    item {
                        Text(
                            text = "Tidak ada hasil untuk \"$query\"",
                            fontSize = 14.sp,
                            fontFamily = interfamily,
                            color = Color(0xFF8A8FA8),
                            modifier = Modifier.padding(24.dp)
                        )
                    }

                } else {

                    item {
                        MenuRowsTriple(
                            items = filtered,
                            onItemClick = onItemClick
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = Color(0xFF3D5AF1)
            )
        }
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = interfamily,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontFamily = interfamily,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1D2E),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    // Replicate LoginScreen textfield style: Card with light gray background and inner BasicTextField
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2F3F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
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
                        if (query.isEmpty()) {
                            Text(
                                text = "Cari Menu",
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

@Composable
fun MenuRowsTriple(items: List<Triple<Int, String, Color>>, onItemClick: (String) -> Unit) {
    val chunked = items.chunked(4)
    Column(modifier = Modifier.fillMaxWidth()) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { (iconRes, label, bgColor) ->
                    MenuIconItem(
                        iconRes = iconRes,
                        label = label,
                        bgColor = bgColor,
                        onClick = { onItemClick(label) }
                    )
                }
                // Fill remaining space in row with empty Boxes to keep spacing consistent
                if (rowItems.size < 4) {
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.width(80.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuFullScreenPreview() {
    MenuFullScreen()
}
