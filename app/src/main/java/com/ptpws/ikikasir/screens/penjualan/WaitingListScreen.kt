package com.ptpws.ikikasir.screens.penjualan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.ptpws.ikikasir.feature.antrean.domain.model.AntreanStatus
import com.ptpws.ikikasir.feature.antrean.presentation.state.AntreanUiState
import com.ptpws.ikikasir.feature.antrean.presentation.viewmodel.AntreanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitingListScreen(
    navController: NavController,
    viewModel: AntreanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

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
                // Search field
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color(0xFF111827),
                            fontSize = 13.sp,
                            fontFamily = interfamily
                        ),
                        modifier = Modifier.fillMaxSize(),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFFB0B8C1),
                                    modifier = Modifier.size(18.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Cari antrean, nama, atau invoice...",
                                            fontSize = 12.sp,
                                            fontFamily = interfamily,
                                            color = Color(0xFFB0B8C1)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )
                }

                // QR scan button
                Card(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    onClick = { /* TODO: QR scanner */ }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Content ────────────────────────────────────────────
            when (val state = uiState) {
                is AntreanUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4F46E5))
                    }
                }

                is AntreanUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            fontFamily = interfamily,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                is AntreanUiState.Success -> {
                    if (filteredList.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = Color(0xFFD1D5DB),
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "Antrean Kosong",
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = interfamily,
                                    fontSize = 18.sp,
                                    color = Color(0xFF6B7280)
                                )
                                Text(
                                    text = "Belum ada pesanan dalam daftar antrean.",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                items = filteredList,
                                key = { _, item -> item.id }
                            ) { index, antrean ->
                                AntreanCard(
                                    nomor = index + 1,
                                    antrean = antrean,
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

@Composable
private fun AntreanCard(
    nomor: Int,
    antrean: Antrean,
    onSelesai: () -> Unit,
    onBatal: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isDone = antrean.status == AntreanStatus.DONE
    val isCancelled = antrean.status == AntreanStatus.CANCELLED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // ── Row utama ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nomor urut
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
                        color = Color(0xFF4F46E5)
                    )
                }

                // Transaction ID + Customer Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = antrean.transactionId,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        color = Color(0xFF111827)
                    )
                    if (antrean.customerName.isNotBlank()) {
                        Text(
                            text = antrean.customerName,
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                // Action buttons (only for WAITING status)
                if (!isDone && !isCancelled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Selesai (✓)
                        IconButton(
                            onClick = onSelesai,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFF22C55E), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selesai",
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        // Batal (✗)
                        IconButton(
                            onClick = onBatal,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFFEF4444), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Batal",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    // Status badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isDone) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isDone) "Selesai" else "Batal",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = if (isDone) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // ── Detail Pesanan toggle row ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Detail Pesanan",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF4F46E5)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF4F46E5),
                    modifier = Modifier.size(20.dp)
                )
            }

            // ── Expanded content ───────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailRow(label = "ID Antrean", value = antrean.id)
                    DetailRow(label = "Nomor Urut", value = "#${antrean.queueSequence}")
                    DetailRow(label = "Status", value = antrean.status)
                    DetailRow(
                        label = "Waktu",
                        value = try {
                            val sdf = java.text.SimpleDateFormat(
                                "dd MMM yyyy HH:mm",
                                java.util.Locale("id", "ID")
                            )
                            sdf.format(antrean.createdAt.toDate())
                        } catch (e: Exception) { "-" }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = interfamily,
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = value,
            fontFamily = interfamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )
    }
}
