package com.ptpws.ikikasir.screens.penjualan
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun LaporanPenjualanScreen(
    navController: NavController,
    onBack: () -> Unit = {},
    onPilihTanggal: () -> Unit = {},
    onLihatSemua: () -> Unit = {}
) {

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Laporan Penjualan",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (navController.currentDestination?.route == "laporan_penjualan") {
                        navController.popBackStack()
                    } }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Header Hari Ini
            item {
                Card(
                    onClick = onPilihTanggal,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 14.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "7 Hari Terakhir",
                            modifier = Modifier.weight(1f),
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )

                        Text(
                            text = "Pilih Tanggal",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF4F46E5)
                        )

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5)
                        )
                    }
                }
            }

            // Statistik
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    RevenueCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Revenue",
                        value = "Rp 5.250.000",
                        isPrimary = true
                    )

                    RevenueCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Penjualan",
                        value = "120 Transaksi",
                        isPrimary = false
                    )
                }
            }

            // Produk Terlaris
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Produk Terlaris",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily
                    )

                    TextButton(onClick = onLihatSemua) {
                        Text(
                            text = "Lihat Semua",
                            fontFamily = interfamily,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    BestSellerCard(
                        modifier = Modifier.weight(1f),
                        nama = "UltraBoost X2",
                        jumlah = "42 Terjual",
                        ranking = "#1",
                        imageRes = R.drawable.kopi
                    )

                    BestSellerCard(
                        modifier = Modifier.weight(1f),
                        nama = "Classic Tee",
                        jumlah = "28 Terjual",
                        ranking = "#2",
                        imageRes = R.drawable.kopi
                    )
                }
            }

            // Barang Terjual
            item {
                Text(
                    text = "Barang Terjual Hari Ini",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily
                )
            }

            items(
                listOf(
                    Triple("Smartwatch Gen 5", "12 Pcs", "+15%"),
                    Triple("Pro Audio Headphones", "8 Pcs", "Steady"),
                    Triple("Wireless Speaker V3", "24 Pcs", "+8%"),
                    Triple("Aviator Classic", "5 Pcs", "-2%")
                )
            ) { item ->

                BarangTerjualItem(
                    namaProduk = item.first,
                    jumlah = item.second,
                    perubahan = item.third,
                    imageRes = R.drawable.kopi
                )
            }
        }
    }
}


@Composable
fun RevenueCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    isPrimary: Boolean
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (isPrimary) Color(0xFF4F46E5)
                else Color(0xFFE8EEF9)
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                fontSize = 12.sp,
                fontFamily = interfamily,
                color = if (isPrimary) Color.White else Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                fontFamily = interfamily,
                color = if (isPrimary) Color.White else Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun BestSellerCard(
    modifier: Modifier = Modifier,
    nama: String,
    jumlah: String,
    ranking: String,
    imageRes: Int
) {

    Card(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ){


        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = nama,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "$ranking Terlaris",
                    fontSize = 12.sp,
                    color = Color.Gray, fontFamily = interfamily
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = nama,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = interfamily,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = jumlah,
                    color = Color(0xFF4F46E5),
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = interfamily
                )
            }
        }
    }
}


@Composable
fun BarangTerjualItem(
    namaProduk: String,
    jumlah: String,
    perubahan: String,
    imageRes: Int
) {

    Card(
        modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = namaProduk,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = namaProduk,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily
                )

                Text(
                    text = "SKU: XXXXX",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = interfamily
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = jumlah,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily
                )

                Text(
                    text = perubahan,
                    fontFamily = interfamily,
                    color = when {
                        perubahan.startsWith("+") -> Color(0xFF16A34A)
                        perubahan.startsWith("-") -> Color.Red
                        else -> Color.Gray
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LaporanPenjualanScreenPreview() {
    LaporanPenjualanScreen(navController = rememberNavController())

}