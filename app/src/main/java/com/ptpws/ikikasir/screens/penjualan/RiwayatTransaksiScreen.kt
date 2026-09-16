package com.ptpws.ikikasir.screens.penjualan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.RiwayatTransaksiViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatTransaksiScreen(
    navController: NavController,
    viewModel: RiwayatTransaksiViewModel = hiltViewModel(),
    onDetailTransaksi: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Riwayat Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.currentDestination?.route == "riwayat") {
                            navController.popBackStack()
                        }
                    }) {
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
                bottom = 86.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Search Bar + Icon QR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search TextField
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
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
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color(0x80474747),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (state.searchQuery.isEmpty()) {
                                            Text(
                                                text = "Cari kode transaksi...",
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

                    // Tombol QR Scan
                    Card(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Scan QR",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    val filterOptions = listOf("Hari Ini", "7 Hari Terakhir", "Semua")
                    items(filterOptions) { filter ->
                        val isSelected = state.selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onFilterSelect(filter) },
                            label = {
                                Text(
                                    text = filter,
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
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
                }
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4F46E5))
                    }
                }
            } else if (state.filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat transaksi",
                            fontFamily = interfamily,
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(state.filteredList, key = { it.transactionId }) { transaksi ->
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val dateObj = transaksi.createdAt.toDate()
                    val jamText = timeFormat.format(dateObj)
                    val formattedPrice = "Rp " + NumberFormat.getInstance(Locale("id", "ID")).format(transaksi.total.toLong())

                    val iconMetode = when (transaksi.paymentMethod.lowercase()) {
                        "qris" -> Icons.Default.QrCode
                        "debit card", "debit" -> Icons.Default.CreditCard
                        else -> Icons.Default.Wallet
                    }

                    TransaksiCardItem(
                        kodeTransaksi = transaksi.transactionNumber,
                        jam = "$jamText - ${dateFormat.format(dateObj)}",
                        metodePembayaran = transaksi.paymentMethod,
                        iconMetode = iconMetode,
                        iconTint = Color(0xFF4F46E5),
                        totalHarga = formattedPrice,
                        statusBayar = transaksi.status,
                        isSynced = transaksi.isSynced,
                        onDetail = { onDetailTransaksi(transaksi.transactionId) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransaksiCardItem(
    kodeTransaksi: String,
    jam: String,
    metodePembayaran: String,
    iconMetode: ImageVector,
    iconTint: Color,
    totalHarga: String,
    statusBayar: String,
    isSynced: Boolean = true,
    onDetail: () -> Unit
) {
    val isLunas = statusBayar.equals("COMPLETED", ignoreCase = true) || statusBayar.equals("LUNAS", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        onClick = onDetail
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kode transaksi (bold indigo)
                Text(
                    text = kodeTransaksi,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    color = Color(0xFF4F46E5)
                )

                // Jam / Tanggal
                Text(
                    text = "  $jam",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Pending Badge jika belum tersinkron ke Firestore
                if (!isSynced) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Pending",
                            fontSize = 10.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Badge Status LUNAS / COMPLETED
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isLunas) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isLunas) "LUNAS" else statusBayar,
                        fontSize = 10.sp,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isLunas) Color(0xFF059669) else Color(0xFFD97706)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Arrow
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Detail",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(14.dp)
                )
            }

            // Baris tengah: icon metode + nama metode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = iconMetode,
                    contentDescription = metodePembayaran,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = metodePembayaran,
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    color = Color(0xFF374151)
                )
            }

            // Baris bawah: total harga (bold besar)
            Text(
                text = totalHarga,
                fontWeight = FontWeight.Bold,
                fontFamily = interfamily,
                fontSize = 20.sp,
                color = Color(0xFF111827)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RiwayatTransaksiScreenPreview() {
    MaterialTheme {
        RiwayatTransaksiScreen(navController = rememberNavController())
    }
}