package com.ptpws.ikikasir.screens.keuangan.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BayarHutangScreen(
    onBack: () -> Unit = {},
    onKonfirmasiPembayaran: () -> Unit = {}
) {
    var namaPelanggan by remember { mutableStateOf("Budi Setiawan") }
    var totalSisaHutang by remember { mutableStateOf("Rp 1.250.000") }
    var jumlahPembayaran by remember { mutableStateOf("0") }
    var metodePembayaran by remember { mutableStateOf("Cash") }
    var catatan by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bayar Hutang",
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

            // ── Informasi Pelanggan
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "INFORMASI PELANGGAN",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = namaPelanggan,
                                fontFamily = interfamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Total Sisa Hutang",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = totalSisaHutang,
                                fontFamily = interfamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = namaPelanggan,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // ── Jumlah Pembayaran
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Jumlah Pembayaran",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rp",
                                fontFamily = interfamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = jumlahPembayaran,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.all { it.isDigit() }) {
                                        jumlahPembayaran = input
                                    }
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = interfamily
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ── Metode Pembayaran
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Metode Pembayaran",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                        // Cash
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(
                                    width = if (metodePembayaran == "Cash") 1.5.dp else 1.dp,
                                    color = if (metodePembayaran == "Cash") Color(0xFF4F46E5) else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { metodePembayaran = "Cash" }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Cash",
                                tint = if (metodePembayaran == "Cash") Color(0xFF4F46E5) else Color(0xFF6B7280),
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "Cash",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (metodePembayaran == "Cash") Color(0xFF4F46E5) else Color(0xFF374151)
                            )
                        }

                        // QRIS
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(
                                    width = if (metodePembayaran == "QRIS") 1.5.dp else 1.dp,
                                    color = if (metodePembayaran == "QRIS") Color(0xFF4F46E5) else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { metodePembayaran = "QRIS" }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QRIS",
                                tint = if (metodePembayaran == "QRIS") Color(0xFF4F46E5) else Color(0xFF6B7280),
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "QRIS",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (metodePembayaran == "QRIS") Color(0xFF4F46E5) else Color(0xFF374151)
                            )
                        }

                        // Transfer
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(
                                    width = if (metodePembayaran == "Transfer") 1.5.dp else 1.dp,
                                    color = if (metodePembayaran == "Transfer") Color(0xFF4F46E5) else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { metodePembayaran = "Transfer" }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Transfer",
                                tint = if (metodePembayaran == "Transfer") Color(0xFF4F46E5) else Color(0xFF6B7280),
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "Transfer",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (metodePembayaran == "Transfer") Color(0xFF4F46E5) else Color(0xFF374151)
                            )
                        }
                    }
                }
            }

            // ── Catatan (Opsional)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Catatan (Opsional)",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            BasicTextField(
                                value = catatan,
                                onValueChange = { catatan = it },
                                textStyle = TextStyle(
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontFamily = interfamily
                                ),
                                modifier = Modifier.fillMaxSize(),
                                decorationBox = { innerTextField ->
                                    if (catatan.isEmpty()) {
                                        Text(
                                            text = "Tambahkan keterangan pembayaran...",
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

                Spacer(modifier = Modifier.height(190.dp))

                // ── Tombol Konfirmasi Pembayaran
                Button(
                    onClick = onKonfirmasiPembayaran,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Konfirmasi Pembayaran",
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
fun BayarHutangScreenPreview() {
    MaterialTheme {
        BayarHutangScreen()
    }
}