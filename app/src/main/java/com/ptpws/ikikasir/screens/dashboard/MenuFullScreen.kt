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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.screens.navigation.AppScreen

@Composable
fun MenuFullScreen(
    navController: NavController
) {
    val queryState = remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF0F4FF)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4FF))
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                TopBar(
                    title = "Semua Menu",
                    navController = navController
                )
                Spacer(modifier = Modifier.height(12.dp))

                SearchField(
                    query = queryState.value,
                    onQueryChange = { queryState.value = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { SectionTitle(title = "Produk") }
            item { ProdukMenuSection(navController) }
            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionTitle(title = "Penjualan") }
            item { PenjualanMenuSection(navController) }
            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionTitle(title = "Keuangan") }
            item { KeuanganMenuSection(navController) }
            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionTitle(title = "Pengguna") }
            item { PenggunaMenuSection(navController) }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
fun KeuanganMenuSection(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MenuIconItem(
            iconRes = R.drawable.laporankeuangan,
            label = "Laporan\nKeuangan",
            bgColor = Color(0xFFFFF0F6),
            onClick = { navController.navigate(AppScreen.LaporanKeuangan.route) }
        )
        MenuIconItem(
            iconRes = R.drawable.hutang,
            label = "Hutang",
            bgColor = Color(0xFFFFF7ED),
            onClick = { navController.navigate(AppScreen.Hutang.route) }
        )
        MenuIconItem(
            iconRes = R.drawable.auditlog,
            label = "Auditlog",
            bgColor = Color(0xFFFFF6F0),
            onClick = { navController.navigate(AppScreen.AuditLog.route) }
        )
        MenuIconItem(
            iconRes = R.drawable.laporanpenjualan,
            label = "Laporan\nPenjualan",
            bgColor = Color(0xFFEFFCF8),
            onClick = { navController.navigate(AppScreen.LaporanPenjualan.route) }
        )
    }
}

@Composable
fun PenggunaMenuSection(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        MenuIconItem(
            iconRes = R.drawable.pengguna,
            label = "Pengguna",
            bgColor = Color(0xFFEFF1FF),
            onClick = { navController.navigate(AppScreen.Pengguna.route) }
        )
        Spacer(modifier = Modifier.width(28.dp))
        MenuIconItem(
            iconRes = R.drawable.pengaturanmenu,
            label = "Pengaturan\nMenu",
            bgColor = Color(0xFFF4F7FF),
            onClick = { navController.navigate(AppScreen.PengaturanMenu.route) }
        )
    }
}

@Composable
fun TopBar(title: String, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            if (navController.currentDestination?.route == AppScreen.Semuamenu.route || navController.currentDestination?.route == "semuamenu") {
                navController.popBackStack()
            }
        }) {
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
        fontWeight = FontWeight.SemiBold ,
        color = Color(0xFF1A1D2E),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit) {
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuFullScreenPreview() {
    MaterialTheme {
        MenuFullScreen(navController = rememberNavController())
    }
}
