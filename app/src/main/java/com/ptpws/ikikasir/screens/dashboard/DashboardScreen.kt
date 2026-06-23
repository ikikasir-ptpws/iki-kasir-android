package com.example.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

@Composable
fun DashboardScreen() {
    Scaffold(
        containerColor = Color(0xFFF0F4FF),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4FF))
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item { HeaderSection() }
            item { StatCardsSection() }
            item { SectionHeader(title = "Produk", onLihatSemua = {}) }
            item { ProdukMenuSection() }
            item {
                Text(
                    text = "Penjualan",
                    fontSize = 18.sp,
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D2E),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
            item { PenjualanMenuSection() }
            item { SectionHeader(title = "Ringkasan Hari Ini", onLihatSemua = {}) }
            item { RingkasanSection() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Selamat datang,",
                fontSize = 14.sp,
                fontFamily = interfamily,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Text(
                text = "Pro Player Alfanshter",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1D2E)
            )
            Text(
                text = "Kelola bisnis Anda dengan mudah",
                fontSize = 13.sp,
                fontFamily = interfamily,
                color = Color(0xFF8A8FA8)
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.notif),
                contentDescription = "Notifikasi",
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun StatCardsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card Penjualan Hari Ini — gradient biru
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF3D5AF1), Color(0xFF6C8EF5))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.grafikicon),
                    contentDescription = "Grafik",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(6.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "PENJUALAN HARI INI ⓘ",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rp",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "10.000.000",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "+5%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "dari kemarin",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Card Total Transaksi — putih
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFFFFFF))
                .padding(16.dp)
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.penjualan),
                    contentDescription = "Transaksi",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDBEAFE))
                        .padding(6.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "TOTAL TRANSAKSI ⓘ",
                    fontSize = 10.sp,
                    color = Color(0xFF8A8FA8)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "50 trx",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D2E)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "+5%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF22C55E)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "dari kemarin",
                        fontSize = 11.sp,
                        color = Color(0xFF8A8FA8)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onLihatSemua: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontFamily = interfamily,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1D2E)
        )
        Text(
            text = "Lihat Semua",
            fontSize = 13.sp,
            color = Color(0xFF3D5AF1),
            modifier = Modifier.clickable { onLihatSemua() }
        )
    }
}

@Composable
fun ProdukMenuSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MenuIconItem(
            iconRes = R.drawable.produk,
            label = "Produk",
            bgColor = Color(0xFFF0FDF4),
            onClick = { /* TODO: Navigasi ke Produk */ }
        )
        MenuIconItem(
            iconRes = R.drawable.kategori,
            label = "Kategori\nProduk",
            bgColor = Color(0xFFFAF5FF),
            onClick = { /* TODO: Navigasi ke Kategori Produk */ }
        )
        MenuIconItem(
            iconRes = R.drawable.manajemenstok,
            label = "Manajemen\nStok",
            bgColor = Color(0xFFFFF7ED),
            onClick = { /* TODO: Navigasi ke Manajemen Stok */ }
        )
        MenuIconItem(
            iconRes = R.drawable.barangrusak,
            label = "Barang Rusak\nExp",
            bgColor = Color(0xFFFDF2F8),
            onClick = { /* TODO: Navigasi ke Barang Rusak/Exp */ }
        )
    }
}

@Composable
fun PenjualanMenuSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        MenuIconItem(
            iconRes = R.drawable.kasirmenu,
            label = "Kasir",
            bgColor = Color(0xFFEFF6FF),
            onClick = { /* TODO: Navigasi ke Kasir */ }
        )
        Spacer(modifier = Modifier.width(28.dp))
        MenuIconItem(
            iconRes = R.drawable.transaksi,
            label = "Transaksi",
            bgColor = Color(0xFFECFEFF),
            onClick = { /* TODO: Navigasi ke Transaksi */ }
        )
    }
}

@Composable
fun MenuIconItem(
    iconRes: Int,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {

        Card(
            onClick = onClick,
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = RoundedCornerShape(18.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = bgColor
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(25.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = interfamily,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}


@Composable
fun RingkasanSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        RingkasanCard(
            modifier = Modifier.weight(1f),
            value = "142",
            title = "Pelanggan",
            growth = "8% dari kemarin",
            isGrowthPositive = true,
            icon = Icons.Outlined.Person,
            iconColor = Color(0xFF22C55E),
            iconBg = Color(0xFFDCFCE7)
        )

        RingkasanCard(
            modifier = Modifier.weight(1f),
            value = "1.320",
            title = "Terjual",
            growth = "15% dari kemarin",
            isGrowthPositive = true,
            icon = Icons.Outlined.Inventory2,
            iconColor = Color(0xFFF97316),
            iconBg = Color(0xFFFFEDD5)
        )
    }
}

@Composable
fun RingkasanCard(
    modifier: Modifier = Modifier,
    value: String,
    title: String,
    growth: String,
    isGrowthPositive: Boolean,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color
) {
    Card(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontFamily = interfamily,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D2E)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF8A8FA8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = if (isGrowthPositive)
                            Icons.Filled.ArrowUpward
                        else
                            Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = if (isGrowthPositive)
                            Color(0xFF22C55E)
                        else
                            Color(0xFFEF4444),
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = growth,
                        fontSize = 11.sp,
                        fontFamily = interfamily,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isGrowthPositive)
                            Color(0xFF22C55E)
                        else
                            Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme {
        DashboardScreen()
    }
}