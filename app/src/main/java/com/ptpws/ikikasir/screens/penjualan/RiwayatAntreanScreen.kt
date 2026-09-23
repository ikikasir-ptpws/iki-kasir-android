package com.ptpws.ikikasir.screens.penjualan

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.ptpws.ikikasir.commond.CustomDateRangePickerDialog
import com.ptpws.ikikasir.commond.getEndOfDayLocalSeconds
import com.ptpws.ikikasir.commond.getStartOfDayLocalSeconds
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.antrean.presentation.state.QueueHistoryUiState
import com.ptpws.ikikasir.feature.antrean.presentation.viewmodel.QueueHistoryViewModel
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatAntreanScreen(
    navController: NavController,
    viewModel: QueueHistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val startDateMillis by viewModel.startDateMillis.collectAsState()
    val endDateMillis by viewModel.endDateMillis.collectAsState()
    val customDateLabel by viewModel.customDateLabel.collectAsState()
    val transaksiMap by viewModel.transaksiMap.collectAsState()

    var showDateRangePickerDialog by remember { mutableStateOf(false) }

    // Launcher for legacy ZXing barcode scanner fallback
    val barcodeScanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contents = result.data?.getStringExtra("SCAN_RESULT")
            if (!contents.isNullOrBlank()) {
                viewModel.onSearchQueryChanged(contents)
            }
        }
    }

    // Trigger ML Kit GMS Code Scanner
    fun triggerBarcodeScanner() {
        try {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .enableAutoZoom()
                .build()

            val scanner = GmsBarcodeScanning.getClient(context, options)
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrBlank()) {
                        viewModel.onSearchQueryChanged(rawValue)
                    }
                }
                .addOnFailureListener {
                    val scanIntent = Intent("com.google.zxing.client.android.SCAN").apply {
                        putExtra("SCAN_MODE", "PRODUCT_MODE")
                    }
                    if (scanIntent.resolveActivity(context.packageManager) != null) {
                        barcodeScanLauncher.launch(scanIntent)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Filter items based on searchQuery & selectedFilter
    val filteredList = when (val state = uiState) {
        is QueueHistoryUiState.Success -> state.data.filter { item ->
            // Search filter
            val matchesSearch = if (searchQuery.isBlank()) true
            else item.transactionId.contains(searchQuery, ignoreCase = true)
                    || item.customerName.contains(searchQuery, ignoreCase = true)

            // Date filter
            val matchesDate = when (selectedFilter) {
                "HARI_INI" -> {
                    val itemCal = Calendar.getInstance().apply { time = item.completedAt.toDate() }
                    val todayCal = Calendar.getInstance()
                    itemCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                            itemCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
                }
                "7_HARI_TERAKHIR" -> {
                    val startOf7Days = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -7)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis / 1000
                    item.completedAt.seconds >= startOf7Days
                }
                "FILTER_TANGGAL" -> {
                    if (startDateMillis != null && endDateMillis != null) {
                        val startSec = getStartOfDayLocalSeconds(startDateMillis!!)
                        val endSec = getEndOfDayLocalSeconds(endDateMillis!!)
                        item.completedAt.seconds in startSec..endSec
                    } else true
                }
                else -> true // "SEMUA"
            }

            matchesSearch && matchesDate
        }
        else -> emptyList()
    }

    val groupedHistory = remember(filteredList) {
        viewModel.groupHistoryByDate(filteredList)
    }

    if (showDateRangePickerDialog) {
        CustomDateRangePickerDialog(
            initialStartDateMillis = startDateMillis,
            initialEndDateMillis = endDateMillis,
            onDismissRequest = { showDateRangePickerDialog = false },
            onDateRangeSelected = { start, end ->
                viewModel.setCustomDateRange(start, end)
                showDateRangePickerDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF0F4FF),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Riwayat Antrean",
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
            // ── Search & Grid Action Bar ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
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
                        .clickable { triggerBarcodeScanner() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode Antrean",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Filter Chips Row (Semua, Hari Ini, 7 Hari Terakhir, Filter Tanggal) ────
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterPill(
                        label = "Semua",
                        isSelected = selectedFilter == "SEMUA",
                        onClick = { viewModel.setFilter("SEMUA") }
                    )
                }
                item {
                    FilterPill(
                        label = "Hari Ini",
                        isSelected = selectedFilter == "HARI_INI",
                        onClick = { viewModel.setFilter("HARI_INI") }
                    )
                }
                item {
                    FilterPill(
                        label = "7 Hari Terakhir",
                        isSelected = selectedFilter == "7_HARI_TERAKHIR",
                        onClick = { viewModel.setFilter("7_HARI_TERAKHIR") }
                    )
                }
                item {
                    val filterTanggalLabel = if (selectedFilter == "FILTER_TANGGAL" && !customDateLabel.isNullOrBlank()) {
                        customDateLabel!!
                    } else {
                        "Filter Tanggal"
                    }

                    FilterPill(
                        label = filterTanggalLabel,
                        isSelected = selectedFilter == "FILTER_TANGGAL",
                        onClick = {
                            showDateRangePickerDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Main Grouped List ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (val state = uiState) {
                    is QueueHistoryUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF3D5AF1)
                        )
                    }

                    is QueueHistoryUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Gagal memuat riwayat antrean",
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

                    is QueueHistoryUiState.Success -> {
                        if (filteredList.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada riwayat antrean.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280),
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                groupedHistory.forEach { (dateHeader, historyItems) ->
                                    item {
                                        GroupHeaderRow(
                                            dateHeader = dateHeader,
                                            count = historyItems.size
                                        )
                                    }

                                    items(historyItems) { history ->
                                        val matchedTransaksi = transaksiMap[history.transactionId]
                                            ?: transaksiMap.values.find { it.transactionNumber == history.transactionId }

                                        QueueHistoryCard(
                                            history = history,
                                            transaksi = matchedTransaksi
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
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF0066FF) else Color.White,
        shadowElevation = if (isSelected) 2.dp else 1.dp,
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF4B5563),
                fontFamily = interfamily
            )
        }
    }
}

@Composable
private fun GroupHeaderRow(
    dateHeader: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF0066FF))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateHeader,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                fontFamily = interfamily
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE2E8F0)
        ) {
            Text(
                text = "$count Antrean",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                fontFamily = interfamily,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun QueueHistoryCard(
    history: QueueHistory,
    transaksi: PenjualanTransaksi?
) {
    var expanded by remember { mutableStateOf(false) }

    val isDone = history.status.equals("DONE", ignoreCase = true) || history.status.equals("SELESAI", ignoreCase = true)

    val statusBgColor = if (isDone) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
    val statusTextColor = if (isDone) Color(0xFF15803D) else Color(0xFFB91C1C)
    val statusDotColor = if (isDone) Color(0xFF16A34A) else Color(0xFFDC2626)
    val statusLabel = if (isDone) "Selesai" else "Dibatalkan"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Transaction ID & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = history.transactionId,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontFamily = interfamily
                )

                // Status Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusBgColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor,
                            fontFamily = interfamily
                        )
                    }
                }
            }

            // Subtitle: Customer Name
            if (history.customerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = history.customerName,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    fontFamily = interfamily
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trigger Line: "Detail Pesanan" with dropdown chevron
            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detail Pesanan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0066FF),
                    fontFamily = interfamily
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF0066FF),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Collapsible Details (NO ID Antrean, showing Purchased Products)
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

                    // Display product list if available
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
                                color = Color(0xFF0066FF),
                                fontFamily = interfamily
                            )
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    }

                    // Metadata rows (ID Antrean is removed!)
                    DetailRow(label = "Nomor Urut", value = "#${history.queueSequence}")
                    DetailRow(label = "Status", value = history.status)
                    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                    DetailRow(label = "Waktu Selesai", value = sdf.format(history.completedAt.toDate()))
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
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            fontFamily = interfamily
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF0F172A),
            fontFamily = interfamily
        )
    }
}
