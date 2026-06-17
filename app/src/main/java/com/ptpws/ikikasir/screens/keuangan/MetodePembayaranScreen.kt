package com.ptpws.ikikasir.screens.keuangan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetodePembayaranScreen(
    onBack: () -> Unit = {}
) {
    // State toggle masing-masing metode
    var cashAktif by remember { mutableStateOf(true) }
    var qrisAktif by remember { mutableStateOf(true) }
    var transferAktif by remember { mutableStateOf(false) }
    var hutangAktif by remember { mutableStateOf(false) }

    // Hitung berapa yang aktif
    val totalAktif = listOf(cashAktif, qrisAktif, transferAktif, hutangAktif).count { it }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Metode Pembayaran",
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
                            contentDescription = "Kembali",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Card Total Metode Aktif
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF4F46E5),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Total Metode Aktif",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            color = Color(0xFFBFBFFF)
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$totalAktif",
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = interfamily,
                                fontSize = 40.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "dari 4 tersedia",
                                fontFamily = interfamily,
                                fontSize = 14.sp,
                                color = Color(0xFFBFBFFF),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        // Badge metode yang aktif
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (cashAktif) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color(0x33FFFFFF),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "CASH",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            if (qrisAktif) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color(0x33FFFFFF),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "QRIS",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            if (transferAktif) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color(0x33FFFFFF),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "TRANSFER",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            if (hutangAktif) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color(0x33FFFFFF),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "HUTANG",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Header Section
            item {
                Text(
                    text = "ATUR METODE PEMBAYARAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    color = Color(0xFF464555),
                    letterSpacing = 0.5.sp
                )
            }

            // Toggle Card: Cash
            item {
                MetodeToggleCard(
                    icon = Icons.Default.Wallet,
                    iconBgColor = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF4F46E5),
                    namaMetode = "Cash",
                    subLabel = "TUNAI",
                    isAktif = cashAktif,
                    onToggle = { cashAktif = it }
                )
            }

            // Toggle Card: QRIS
            item {
                MetodeToggleCard(
                    icon = Icons.Default.QrCode,
                    iconBgColor = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF4F46E5),
                    namaMetode = "QRIS",
                    subLabel = "DIGITAL PAYMENT",
                    isAktif = qrisAktif,
                    onToggle = { qrisAktif = it }
                )
            }

            //  Toggle Card: Transfer
            item {
                MetodeToggleCard(
                    icon = Icons.Default.AccountBalance,
                    iconBgColor = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF4F46E5),
                    namaMetode = "Transfer",
                    subLabel = "BANK MANUAL",
                    isAktif = transferAktif,
                    onToggle = { transferAktif = it }
                )
            }

            // Toggle Card: Hutang
            item {
                MetodeToggleCard(
                    icon = Icons.Default.CreditCard,
                    iconBgColor = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF4F46E5),
                    namaMetode = "Hutang",
                    subLabel = "KREDIT",
                    isAktif = hutangAktif,
                    onToggle = { hutangAktif = it }
                )
            }

            // Info Box Pemberitahuan
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color(0xffEFF4FF), RoundedCornerShape(16.dp))
                ) {
                    // Garis aksen kiri indigo
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(100.dp)
                                .background(
                                    color = Color(0xFF4F46E5),
                                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Pemberitahuan",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interfamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFF111827)
                                )
                            }
                            Text(
                                text = "Metode yang diaktifkan akan muncul di halaman transaksi. Pastikan rekening Anda sudah terhubung sebelum mengaktifkan Transfer Bank.",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Metode Toggle Card

@Composable
fun MetodeToggleCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    namaMetode: String,
    subLabel: String,
    isAktif: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon metode
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = namaMetode,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Nama & sub label
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = namaMetode,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = interfamily,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
                Text(
                    text = subLabel,
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9CA3AF),
                    letterSpacing = 0.3.sp
                )
            }

            // Toggle Switch
            Switch(
                checked = isAktif,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4F46E5),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD1D5DB),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MetodePembayaranScreenPreview() {
    MaterialTheme {
        MetodePembayaranScreen()
    }
}