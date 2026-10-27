package com.ptpws.ikikasir.screens.penjualan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ptpws.ikikasir.commond.GlobalScrollPaginationFooter
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import com.ptpws.ikikasir.feature.antrean.presentation.state.AntreanUiState
import com.ptpws.ikikasir.feature.antrean.presentation.viewmodel.AntreanViewModel
import androidx.activity.compose.BackHandler
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.screens.navigation.AppScreen
import com.ptpws.ikikasir.screens.navigation.popBackStackSafely
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitingListScreen(
    navController: NavController,
    viewModel: AntreanViewModel = hiltViewModel()
) {
    BackHandler {
        navController.popBackStackSafely(expectedRoute = AppScreen.WaitingList.route)
    }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val transaksiMap by viewModel.transaksiMap.collectAsState()

    val filteredList = when (val state = uiState) {
        is AntreanUiState.Success -> state.data.filter { antrean ->
            // Cek apakah transaksi atau antrean sudah di-refund / dibatalkan
            val matchedTransaksi = transaksiMap[antrean.transactionId]
                ?: transaksiMap[antrean.transactionId.removePrefix("#")]
                ?: transaksiMap.values.find { tx ->
                    tx.transactionId.equals(antrean.transactionId, ignoreCase = true) ||
                    tx.transactionNumber.equals(antrean.transactionId, ignoreCase = true) ||
                    tx.transactionNumber.removePrefix("#").equals(antrean.transactionId.removePrefix("#"), ignoreCase = true)
                }

            val isRefunded = antrean.status.equals("REFUND", ignoreCase = true) ||
                antrean.status.equals("REFUNDED", ignoreCase = true) ||
                antrean.status.equals("BATAL", ignoreCase = true) ||
                (matchedTransaksi != null && (
                    matchedTransaksi.status.equals("REFUND", ignoreCase = true) ||
                    matchedTransaksi.status.equals("REFUNDED", ignoreCase = true) ||
                    matchedTransaksi.status.equals("BATAL", ignoreCase = true) ||
                    matchedTransaksi.refundReason.isNotBlank()
                ))

            if (isRefunded) {
                false
            } else if (searchQuery.isBlank()) {
                true
            } else {
                antrean.transactionId.contains(searchQuery, ignoreCase = true)
                    || antrean.customerName.contains(searchQuery, ignoreCase = true)
            }
        }
        else -> emptyList()
    }

    // Scroll Pagination State (Tampilkan 10 data awal, muat 10 data berikutnya saat scroll)
    val pageSize = 10
    var displayedCount by remember(searchQuery) {
        mutableIntStateOf(pageSize)
    }
    val displayedWaitingList = remember(filteredList, displayedCount) {
        filteredList.take(displayedCount)
    }

    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var showBatchCancelConfirmDialog by remember { mutableStateOf(false) }

    // Dialog Konfirmasi Batal Batch
    if (showBatchCancelConfirmDialog && selectedIds.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showBatchCancelConfirmDialog = false },
            title = {
                Text(
                    text = "Batalkan ${selectedIds.size} Antrean?",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin membatalkan ${selectedIds.size} antrean yang dipilih sekaligus?",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idsToProcess = selectedIds
                        selectedIds = emptySet()
                        showBatchCancelConfirmDialog = false
                        viewModel.batalkanMultiple(idsToProcess)
                    }
                ) {
                    Text(
                        text = "Ya, Batalkan Semua",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBatchCancelConfirmDialog = false }
                ) {
                    Text(
                        text = "Batal",
                        color = Color(0xFF6B7280),
                        fontFamily = interfamily
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
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
                    IconButton(onClick = {
                        navController.popBackStackSafely(expectedRoute = AppScreen.WaitingList.route)
                    }) {
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

            // ── Batch Action / Select All Header Bar ──────────────────
            if (filteredList.isNotEmpty()) {
                val isAllSelected = selectedIds.size == filteredList.size && filteredList.isNotEmpty()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Checkbox Select All / Deselect All
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isAllSelected) {
                                        selectedIds = emptySet()
                                    } else {
                                        selectedIds = filteredList.map { it.id }.toSet()
                                    }
                                }
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            Checkbox(
                                checked = isAllSelected,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) {
                                        filteredList.map { it.id }.toSet()
                                    } else {
                                        emptySet()
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF4F46E5),
                                    uncheckedColor = Color(0xFF9CA3AF)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedIds.isEmpty()) "Pilih Semua (${filteredList.size})" else "${selectedIds.size} Terpilih",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedIds.isNotEmpty()) Color(0xFF4F46E5) else Color(0xFF334155)
                            )
                        }

                        // Action Buttons when 1 or more selected
                        if (selectedIds.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Tombol Centang Semua / Selesaikan (✓)
                                Surface(
                                    onClick = {
                                        val idsToProcess = selectedIds
                                        selectedIds = emptySet()
                                        viewModel.selesaikanMultiple(idsToProcess)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFDCFCE7),
                                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selesaikan",
                                            tint = Color(0xFF15803D),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Selesai (${selectedIds.size})",
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }

                                // Tombol Silang Semua / Batalkan (✗)
                                Surface(
                                    onClick = { showBatchCancelConfirmDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFEE2E2),
                                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Batalkan",
                                            tint = Color(0xFFB91C1C),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Batal (${selectedIds.size})",
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFB91C1C)
                                        )
                                    }
                                }
                            }
                        }
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            repeat(5) {
                                com.ptpws.ikikasir.commond.AntreanCardShimmer()
                            }
                        }
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
                                contentPadding = PaddingValues(bottom = 56.dp)
                            ) {
                                itemsIndexed(displayedWaitingList) { index, antrean ->
                                    val globalNumber = index + 1
                                    val matchedTransaksi = transaksiMap[antrean.transactionId]
                                        ?: transaksiMap[antrean.transactionId.removePrefix("#")]
                                        ?: transaksiMap.values.find { tx ->
                                            tx.transactionNumber.equals(antrean.transactionId, ignoreCase = true) ||
                                            tx.transactionId.equals(antrean.transactionId, ignoreCase = true) ||
                                            tx.transactionNumber.removePrefix("#").equals(antrean.transactionId.removePrefix("#"), ignoreCase = true)
                                        }

                                    AntreanCard(
                                        nomor = globalNumber,
                                        antrean = antrean,
                                        transaksi = matchedTransaksi,
                                        isSelected = selectedIds.contains(antrean.id),
                                        onSelectToggle = {
                                            selectedIds = if (selectedIds.contains(antrean.id)) {
                                                selectedIds - antrean.id
                                            } else {
                                                selectedIds + antrean.id
                                            }
                                        },
                                        onSelesai = { viewModel.selesaikan(antrean.id) },
                                        onBatal = { viewModel.batalkan(antrean.id) }
                                    )
                                }

                                // Scroll Pagination Footer (CircularProgressIndicator saat discroll)
                                if (filteredList.isNotEmpty()) {
                                    item(key = "scroll_pagination_footer") {
                                        GlobalScrollPaginationFooter(
                                            displayedCount = displayedCount,
                                            totalItems = filteredList.size,
                                            onLoadMore = {
                                                displayedCount = minOf(displayedCount + pageSize, filteredList.size)
                                            },
                                            itemName = "antrean"
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
}

@Composable
private fun PendingSyncBadge() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFEF3C7)
    ) {
        Text(
            text = "Pending",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontFamily = interfamily,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD97706)
        )
    }
}

@Composable
private fun AntreanCard(
    nomor: Int,
    antrean: Antrean,
    transaksi: PenjualanTransaksi?,
    isSelected: Boolean = false,
    onSelectToggle: (() -> Unit)? = null,
    onSelesai: () -> Unit,
    onBatal: () -> Unit
) {
    val context = LocalContext.current
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
            // ── Header Row: Selection Checkbox, Sequence Number, Invoice ID, Action Buttons ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection Checkbox for Batch Action
                if (onSelectToggle != null) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4F46E5),
                            uncheckedColor = Color(0xFF9CA3AF)
                        ),
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = antrean.transactionId,
                            modifier = Modifier.weight(1f, fill = false),
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            fontSize = 15.sp,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!antrean.isSynced || transaksi?.isSynced == false) {
                            PendingSyncBadge()
                        }
                    }
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
                                    if (cartItem.produk.discount > 0) {
                                        val discLabel = if (cartItem.produk.discountType == "PERCENT") "${cartItem.produk.discount.toInt()}%" else rupiahFormat.format(cartItem.produk.discount)
                                        Text(
                                            text = "Promo Diskon: $discLabel",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF16A34A),
                                            fontFamily = interfamily
                                        )
                                    }
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

                        val isPpnAktif = transaksi.isTaxActive()
                        val isPpnEksklusif = transaksi.checkIsPpnEksklusif()
                        val formattedPercent = transaksi.getFormattedPercentage()
                        val effectivePpn = transaksi.getEffectivePpnAmount()

                        if (isPpnAktif && effectivePpn > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PPN ($formattedPercent)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    fontFamily = interfamily
                                )
                                Text(
                                    text = if (isPpnEksklusif) "+ ${rupiahFormat.format(effectivePpn)}" else rupiahFormat.format(effectivePpn),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    fontFamily = interfamily
                                )
                            }
                        }

                        if (transaksi.discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Diskon Promo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A),
                                    fontFamily = interfamily
                                )
                                Text(
                                    text = "- ${rupiahFormat.format(transaksi.discount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A),
                                    fontFamily = interfamily
                                )
                            }
                        }

                        val totalSubtotal = if (transaksi.subtotal > 0) transaksi.subtotal else transaksi.items.sumOf { it.totalPrice }
                        val totalTagihan = if (transaksi.total > 0) transaksi.total else (totalSubtotal - transaksi.discount + (if (isPpnEksklusif) effectivePpn else 0.0)).coerceAtLeast(0.0)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Tagihan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                fontFamily = interfamily
                            )
                            Text(
                                text = rupiahFormat.format(totalTagihan),
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
