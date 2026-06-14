package com.ptpws.ikikasir.screens.penjualan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTransaksiScreen(
    onBack: () -> Unit = {},
    onCetakStruk: () -> Unit = {},
    onRefund: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Transaksi",
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

            // Card: Nomor Transaksi
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nomor Transaksi",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFD1FAE5),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "LUNAS",
                                    fontSize = 11.sp,
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }
                        }

                        Text(
                            text = "#TRX-20231025-001",
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            fontSize = 22.sp,
                            color = Color(0xFF4F46E5),
                            lineHeight = 28.sp
                        )

                        Divider(
                            color = Color(0xFFF3F4F6),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Tanggal dan Waktu",
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                                Text(
                                    text = "25 Okt 2023, 14:30",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color(0xFF111827)
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Kasir",
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                                Text(
                                    text = "Admin",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color(0xFF111827)
                                )
                            }
                        }
                    }
                }
            }

            // Card: Daftar Produk
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "Daftar Produk",
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            fontSize = 14.sp,
                            color = Color(0xFF111827)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ProdukCardRow(
                            namaProduk = "Kopi Susu Gula Aren",
                            qty = 2,
                            hargaSatuan = "Rp 18.000",
                            totalHarga = "Rp 36.000",
                            imagePainter = painterResource(R.drawable.kopi)
                        )

                        Divider(
                            color = Color(0xFFF3F4F6),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        ProdukCardRow(
                            namaProduk = "Roti Bakar Cokelat",
                            qty = 1,
                            hargaSatuan = "Rp 15.000",
                            totalHarga = "Rp 15.000",
                            imagePainter = painterResource(R.drawable.kopi)
                        )
                    }
                }
            }

            // Card: Ringkasan Pembayaran
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RingkasanBaris(
                            label = "Subtotal",
                            nilai = "Rp 51.000",
                            nilaiColor = Color(0xFF374151)
                        )

                        RingkasanBaris(
                            label = "Promo/Diskon",
                            nilai = "- Rp 5.000",
                            nilaiColor = Color(0xFF059669)
                        )

                        Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Pembayaran",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Rp 46.000",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF4F46E5)
                            )
                        }

                        Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Metode Pembayaran",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallet,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Cash",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF374151)
                                )
                            }
                        }
                    }
                }
            }

            // Tombol Cetak Struk
            item {
                Button(
                    onClick = onCetakStruk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cetak Struk",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            // Tombol Refund
            item {
                OutlinedButton(
                    onClick = onRefund,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Refund",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

// Composable: Baris satu produk

@Composable
fun ProdukCardRow(
    namaProduk: String,
    qty: Int,
    hargaSatuan: String,
    totalHarga: String,
    imagePainter: Painter? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            if (imagePainter != null) {
                Image(
                    painter = imagePainter,
                    contentDescription = namaProduk,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ImageNotSupported,
                    contentDescription = null,
                    tint = Color(0xFFD1D5DB),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = namaProduk,
                fontFamily = interfamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF111827),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "x$qty @ $hargaSatuan",
                fontFamily = interfamily,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )
        }

        Text(
            text = totalHarga,
            fontFamily = interfamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF111827)
        )
    }
}


@Composable
fun RingkasanBaris(
    label: String,
    nilai: String,
    nilaiColor: Color = Color(0xFF374151)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = interfamily,
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
        Text(
            text = nilai,
            fontFamily = interfamily,
            fontSize = 13.sp,
            color = nilaiColor
        )
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetailTransaksiScreenPreview() {
    MaterialTheme {
        DetailTransaksiScreen ()
    }
}