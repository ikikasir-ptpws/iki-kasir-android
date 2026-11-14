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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ptpws.ikikasir.commond.GlobalScrollPaginationFooter
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import com.ptpws.ikikasir.feature.antrean.presentation.state.AntreanUiState
import com.ptpws.ikikasir.feature.antrean.presentation.viewmodel.AntreanUiProvider
import com.ptpws.ikikasir.feature.antrean.presentation.viewmodel.HiltAntreanUiProvider
import com.ptpws.ikikasir.feature.antrean.presentation.viewmodel.AntreanViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.ui.tooling.preview.Preview
import com.ptpws.ikikasir.core.responsive.presentation.components.PhoneDevicePreviews
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.screens.navigation.AppScreen
import com.ptpws.ikikasir.screens.navigation.popBackStackSafely
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitingListScreen(
    navController: NavController,
    viewModel: AntreanUiProvider = HiltAntreanUiProvider(hiltViewModel<AntreanViewModel>())
) {
    BackHandler {
        navController.popBackStackSafely(expectedRoute = AppScreen.WaitingList.route)
    }

    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val transaksiMap by viewModel.transaksiMap.collectAsState()
    val isBulkProcessing by viewModel.isBulkProcessing.collectAsState()

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
    var selectedIds by remember(searchQuery) { mutableStateOf(emptySet<String>()) }
    val selectedAntreanIds = filteredList.filter { it.id in selectedIds }.map { it.id }
    val selectedCount = selectedAntreanIds.size
    val allFilteredSelected = filteredList.isNotEmpty() && selectedCount == filteredList.size

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
                        color = Color(0xFF111827),
                        // Responsif: judul top bar dipotong agar tidak menabrak ikon navigasi di HP 320dp
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStackSafely(expectedRoute = AppScreen.WaitingList.route)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari antrean, nama, atau invoice...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF9CA3AF),
                                        fontFamily = interfamily,
                                        modifier = Modifier.align(Alignment.CenterStart),
                                        // Responsif: placeholder dipotong agar tidak menabrak ikon di HP 320dp
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }

            if (uiState is AntreanUiState.Success && filteredList.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = allFilteredSelected,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) {
                                        selectedIds + filteredList.map { it.id }
                                    } else {
                                        selectedIds - filteredList.map { it.id }.toSet()
                                    }
                                },
                                enabled = !isBulkProcessing,
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3D5AF1))
                            )
                            Text(
                                text = "Pilih semua antrean",
                                modifier = Modifier.weight(1f),
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = if (isBulkProcessing) "Memproses..." else "$selectedCount dipilih",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (selectedCount > 0) {
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.selesaikanBanyak(selectedAntreanIds)
                                        selectedIds = emptySet()
                                    },
                                    enabled = !isBulkProcessing,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "Selesaikan ($selectedCount)",
                                        fontSize = 12.sp,
                                        fontFamily = interfamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Button(
                                    onClick = {
                                        viewModel.batalkanBanyak(selectedAntreanIds)
                                        selectedIds = emptySet()
                                    },
                                    enabled = !isBulkProcessing,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "Batalkan ($selectedCount)",
                                        fontSize = 12.sp,
                                        fontFamily = interfamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

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
                                        isSelected = antrean.id in selectedIds,
                                        selectionEnabled = !isBulkProcessing,
                                        onSelectionChanged = { selected ->
                                            selectedIds = if (selected) {
                                                selectedIds + antrean.id
                                            } else {
                                                selectedIds - antrean.id
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
    isSelected: Boolean,
    selectionEnabled: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
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
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectionChanged,
                    enabled = selectionEnabled,
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3D5AF1))
                )

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

                Spacer(modifier = Modifier.width(8.dp))

                // Invoice ID & Customer Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = antrean.transactionId,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val subtitleText = buildString {
                        if (antrean.customerName.isNotBlank()) append(antrean.customerName)
                        if (antrean.tableNumber.isNotBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append("Meja: ${antrean.tableNumber}")
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (!antrean.isSynced || transaksi?.isSynced == false) {
                            PendingSyncBadge()
                        }
                        if (subtitleText.isNotBlank()) {
                            Text(
                                text = subtitleText,
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Action Buttons: Selesai (✓) & Batal (✗) neatly styled side-by-side
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Centang (✓) - Selesai
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7))
                            .border(1.dp, Color(0xFF86EFAC), CircleShape)
                            .clickable(enabled = selectionEnabled) { onSelesai() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selesai",
                            tint = Color(0xFF166534),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Tombol Silang (✗) - Batal
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2))
                            .border(1.dp, Color(0xFFFCA5A5), CircleShape)
                            .clickable(enabled = selectionEnabled) { onBatal() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Batal",
                            tint = Color(0xFF991B1B),
                            modifier = Modifier.size(16.dp)
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
                    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
                        maximumFractionDigits = 0
                        minimumFractionDigits = 0
                    }

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
                                        fontFamily = interfamily,
                                        // Responsif: nama produk panjang dipotong agar harga tidak terdorong keluar card
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${cartItem.quantity}x @ ${rupiahFormat.format(cartItem.produk.price)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        fontFamily = interfamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (cartItem.produk.discount > 0) {
                                        val discLabel = if (cartItem.produk.discountType == "PERCENT") "${cartItem.produk.discount.toInt()}%" else rupiahFormat.format(cartItem.produk.discount)
                                        Text(
                                            text = "Promo Diskon: $discLabel",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF16A34A),
                                            fontFamily = interfamily,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (cartItem.note.isNotBlank()) {
                                        Text(
                                            text = "Catatan: ${cartItem.note}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFE11D48),
                                            fontFamily = interfamily,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Text(
                                    text = rupiahFormat.format(cartItem.totalPrice),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    fontFamily = interfamily,
                                    // Responsif: harga dipotong bila layar sempit agar tidak keluar card
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                                    fontFamily = interfamily,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isPpnEksklusif) "+ ${rupiahFormat.format(effectivePpn)}" else rupiahFormat.format(effectivePpn),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    fontFamily = interfamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                                    fontFamily = interfamily,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "- ${rupiahFormat.format(transaksi.discount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A),
                                    fontFamily = interfamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                                fontFamily = interfamily,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = rupiahFormat.format(totalTagihan),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3D5AF1),
                                fontFamily = interfamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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

// ---- PREVIEW (aktifkan satu per satu untuk tes) ----
// import androidx.compose.ui.tooling.preview.Preview
// import androidx.navigation.compose.rememberNavController
//
// @Preview(name = "HP kecil 320x568", device = "spec:width=320dp,height=568dp", showBackground = true)
// @Preview(name = "HP standar 360x800", device = "spec:width=360dp,height=800dp", showBackground = true)
// @Preview(name = "HP besar 411x891", device = "spec:width=411dp,height=891dp", showBackground = true)
// @Preview(name = "Font besar", device = "spec:width=360dp,height=800dp", fontScale = 1.3f, showBackground = true)
// @Composable
// private fun WaitingListScreenResponsivePreview() {
//     // Preview butuh ViewModel/Firebase, perlu composable stateless
//     MaterialTheme {
//         WaitingListScreen(
//             navController = rememberNavController()
//         )
//     }
// }

@Preview(showBackground = true)
@PhoneDevicePreviews
@Composable
private fun WaitingListScreenPreview() {
    MaterialTheme {
        WaitingListScreen(
            navController = androidx.navigation.compose.rememberNavController(),
            viewModel = com.ptpws.ikikasir.commond.PreviewAntreanProvider()
        )
    }
}
