package com.ptpws.ikikasir.screens.penjualan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import com.ptpws.ikikasir.feature.antrean.presentation.state.AntreanUiState
import com.ptpws.ikikasir.feature.antrean.presentation.viewmodel.AntreanViewModel
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitingListScreen(
    navController: NavController,
    viewModel: AntreanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val transaksiMap by viewModel.transaksiMap.collectAsState()

    val filteredList = when (val state = uiState) {
        is AntreanUiState.Success -> state.data.filter { antrean ->
            if (searchQuery.isBlank()) true
            else antrean.transactionId.contains(searchQuery, ignoreCase = true)
                || antrean.customerName.contains(searchQuery, ignoreCase = true)
        }
        else -> emptyList()
    }

    Scaffold(
        containerColor = Color(0xFFF0F4FF),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daftar Antrean",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 20.sp,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF0F4FF)
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Search bar + QR button ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
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
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Cari",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Cari antrean, nama, atau invoice...",
                                            fontSize = 13.sp,
                                            color = Color(0xFF9CA3AF),
                                            fontFamily = interfamily
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )
                }

                Card(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { /* QR Scan action */ },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = Color(0xFF3D5AF1),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Main Content ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (val state = uiState) {
                    is AntreanUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF3D5AF1)
                        )
                    }

                    is AntreanUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Gagal memuat antrean",
                                fontSize = 14.sp,
                                color = Color.Red,
                                fontFamily = interfamily
                            )
                            Text(
                                text = state.message,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontFamily = interfamily
                            )
                        }
                    }

                    is AntreanUiState.Success -> {
                        if (filteredList.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada pesanan dalam daftar antrean.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280),
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                itemsIndexed(filteredList) { index, antrean ->
                                    val matchedTransaksi = transaksiMap[antrean.transactionId]
                                        ?: transaksiMap.values.find { it.transactionNumber == antrean.transactionId }

                                    AntreanCard(
                                        nomor = index + 1,
                                        antrean = antrean,
                                        transaksi = matchedTransaksi,
                                        onSelesai = { viewModel.selesaikan(antrean.id) },
                                        onBatal = { viewModel.batalkan(antrean.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AntreanCard(
    nomor: Int,
    antrean: Antrean,
    transaksi: PenjualanTransaksi?,
    onSelesai: () -> Unit,
    onBatal: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // ── Header Row: Sequence Number, Invoice ID, Action Buttons (✓ and ✗) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Queue Sequence Number
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nomor.toString().padStart(2, '0'),
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        color = Color(0xFF3D5AF1)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Invoice ID & Customer Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = antrean.transactionId,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        color = Color(0xFF111827)
                    )
                    val subtitleText = buildString {
                        if (antrean.customerName.isNotBlank()) append(antrean.customerName)
                        if (antrean.tableNumber.isNotBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append("Meja: ${antrean.tableNumber}")
                        }
                    }
                    if (subtitleText.isNotBlank()) {
                        Text(
                            text = subtitleText,
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                // Action Buttons: Selesai (✓) & Batal (✗) neatly styled side-by-side
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Centang (✓) - Selesai
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7))
                            .border(1.dp, Color(0xFF86EFAC), CircleShape)
                            .clickable { onSelesai() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selesai",
                            tint = Color(0xFF166534),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Tombol Silang (✗) - Batal
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2))
                            .border(1.dp, Color(0xFFFCA5A5), CircleShape)
                            .clickable { onBatal() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Batal",
                            tint = Color(0xFF991B1B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // ── Detail Pesanan Trigger Row ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Detail Pesanan",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF3D5AF1)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF3D5AF1),
                    modifier = Modifier.size(20.dp)
                )
            }

            // ── Collapsible Product List Details ───────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

                    if (transaksi != null && transaksi.items.isNotEmpty()) {
                        Text(
                            text = "Item Pesanan:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            fontFamily = interfamily
                        )

                        transaksi.items.forEach { cartItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cartItem.produk.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A),
                                        fontFamily = interfamily
                                    )
                                    Text(
                                        text = "${cartItem.quantity}x @ ${rupiahFormat.format(cartItem.produk.price)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        fontFamily = interfamily
                                    )
                                    if (cartItem.note.isNotBlank()) {
                                        Text(
                                            text = "Catatan: ${cartItem.note}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFE11D48),
                                            fontFamily = interfamily
                                        )
                                    }
                                }
                                Text(
                                    text = rupiahFormat.format(cartItem.totalPrice),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    fontFamily = interfamily
                                )
                            }
                        }

                        if (transaksi.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Catatan Pesanan: ${transaksi.notes}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF92400E),
                                    fontFamily = interfamily
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Tagihan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                fontFamily = interfamily
                            )
                            Text(
                                text = rupiahFormat.format(transaksi.total),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3D5AF1),
                                fontFamily = interfamily
                            )
                        }
                    } else {
                        Text(
                            text = "Nomor Urut: #${antrean.queueSequence}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontFamily = interfamily
                        )
                        Text(
                            text = "Status: ${antrean.status}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontFamily = interfamily
                        )
                    }
                }
            }
        }
    }
}
