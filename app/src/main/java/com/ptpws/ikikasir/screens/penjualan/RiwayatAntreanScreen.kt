package com.ptpws.ikikasir.screens.penjualan

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.ptpws.ikikasir.commond.CustomDateRangePickerDialog
import com.ptpws.ikikasir.commond.GlobalScrollPaginationFooter
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

import androidx.activity.compose.BackHandler
import com.ptpws.ikikasir.screens.navigation.AppScreen
import com.ptpws.ikikasir.screens.navigation.popBackStackSafely
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatAntreanScreen(
    navController: NavController,
    viewModel: QueueHistoryViewModel = hiltViewModel()
) {
    BackHandler {
        navController.popBackStackSafely(expectedRoute = AppScreen.RiwayatAntrean.route)
    }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val startDateMillis by viewModel.startDateMillis.collectAsState()
    val endDateMillis by viewModel.endDateMillis.collectAsState()
    val customDateLabel by viewModel.customDateLabel.collectAsState()
    val transaksiMap by viewModel.transaksiMap.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportedFileUri by viewModel.exportedFileUri.collectAsState()
    val exportError by viewModel.exportError.collectAsState()

    var showDateRangePickerDialog by remember { mutableStateOf(false) }
    var showExportSuccessDialog by remember { mutableStateOf(false) }
    var showExportErrorDialog by remember { mutableStateOf(false) }
    var exportedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Export state observers ──────────────────────────────────────────────
    LaunchedEffect(exportedFileUri) {
        if (exportedFileUri != null) {
            exportedUri = exportedFileUri
            showExportSuccessDialog = true
        }
    }

    LaunchedEffect(exportError) {
        if (!exportError.isNullOrBlank()) {
            errorMessage = exportError ?: ""
            showExportErrorDialog = true
        }
    }

    // ── Export Success Dialog ─────────────────────────────────────────────
    if (showExportSuccessDialog && exportedUri != null) {
        AlertDialog(
            onDismissRequest = {
                showExportSuccessDialog = false
                viewModel.clearExportState()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(52.dp)
                )
            },
            title = {
                Text(
                    text = "Ekspor Excel Berhasil!",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column {
                    Text(
                        text = "File Excel riwayat antrean berhasil diunduh dan tersimpan di HP Anda.",
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📁 Lokasi Simpan (Berkas HP):",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Memori Internal > Download > IkiKasir",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF4F46E5)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(
                                exportedUri,
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        val chooser = Intent.createChooser(shareIntent, "Buka dengan")
                        context.startActivity(chooser)
                        showExportSuccessDialog = false
                        viewModel.clearExportState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Buka File", fontFamily = interfamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportSuccessDialog = false
                    viewModel.clearExportState()
                }) {
                    Text("Tutup", fontFamily = interfamily, color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Export Error Dialog ───────────────────────────────────────────────────
    if (showExportErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportErrorDialog = false
                viewModel.clearExportState()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(52.dp)
                )
            },
            title = {
                Text(
                    text = "Ekspor Excel Gagal",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF991B1B)
                )
            },
            text = {
                Text(
                    text = if (errorMessage.isNotBlank()) errorMessage else "Gagal mengunduh file Excel. Silakan coba lagi.",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportErrorDialog = false
                        viewModel.clearExportState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mengerti", fontFamily = interfamily, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Launcher for legacy ZXing barcode scanner fallback
    val triggerBarcodeScanner = com.ptpws.ikikasir.core.util.rememberBarcodeScanner { contents ->
        viewModel.onSearchQueryChanged(contents)
    }

    // Filter items based on searchQuery & selectedFilter
    val filteredList = when (val state = uiState) {
        is QueueHistoryUiState.Success -> state.data.filter { item ->
            // Cek apakah transaksi atau antrean sudah di-refund / dibatalkan
            val matchedTx = transaksiMap[item.transactionId]
                ?: transaksiMap[item.transactionId.removePrefix("#")]
                ?: transaksiMap.values.find { tx ->
                    tx.transactionId.equals(item.transactionId, ignoreCase = true) ||
                    tx.transactionNumber.equals(item.transactionId, ignoreCase = true) ||
                    tx.transactionNumber.removePrefix("#").equals(item.transactionId.removePrefix("#"), ignoreCase = true)
                }

            val isRefunded = item.status.equals("REFUND", ignoreCase = true) ||
                item.status.equals("REFUNDED", ignoreCase = true) ||
                item.status.equals("BATAL", ignoreCase = true) ||
                (matchedTx != null && (
                    matchedTx.status.equals("REFUND", ignoreCase = true) ||
                    matchedTx.status.equals("REFUNDED", ignoreCase = true) ||
                    matchedTx.status.equals("BATAL", ignoreCase = true) ||
                    matchedTx.refundReason.isNotBlank()
                ))

            if (isRefunded) {
                return@filter false
            }

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

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.resetToDefault()
        listState.scrollToItem(0, 0)
    }

    // Scroll Pagination State (Tampilkan 10 data awal, muat 10 data berikutnya saat scroll)
    val pageSize = 10
    var displayedCount by remember(searchQuery, selectedFilter, startDateMillis, endDateMillis) {
        mutableIntStateOf(pageSize)
    }
    val displayedHistory = remember(filteredList, displayedCount) {
        filteredList.take(displayedCount)
    }
    val displayedGroupedHistory = remember(displayedHistory) {
        viewModel.groupHistoryByDate(displayedHistory)
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    IconButton(onClick = {
                        navController.popBackStackSafely(expectedRoute = AppScreen.RiwayatAntrean.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.exportToExcel(context, filteredList) },
                        enabled = !isExporting && filteredList.isNotEmpty()
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color(0xFF4F46E5),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Ekspor Excel",
                                tint = if (filteredList.isNotEmpty()) Color(0xFF4F46E5) else Color(0xFFCBD5E1)
                            )
                        }
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

            Spacer(modifier = Modifier.height(4.dp))

            // ── Filter Chips Row (Semua, Hari Ini, 7 Hari Terakhir, Filter Tanggal) ────
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chip 1: Semua
                item {
                    val isSelected = selectedFilter == "SEMUA"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter("SEMUA") },
                        label = {
                            Text(
                                text = "Semua",
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
                            borderColor = Color(0xFFE2E8F0),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 0.dp
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // Chip 2: Hari Ini
                item {
                    val isSelected = selectedFilter == "HARI_INI"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter("HARI_INI") },
                        label = {
                            Text(
                                text = "Hari Ini",
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
                            borderColor = Color(0xFFE2E8F0),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 0.dp
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // Chip 3: 7 Hari Terakhir
                item {
                    val isSelected = selectedFilter == "7_HARI_TERAKHIR"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter("7_HARI_TERAKHIR") },
                        label = {
                            Text(
                                text = "7 Hari Terakhir",
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
                            borderColor = Color(0xFFE2E8F0),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 0.dp
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // Chip 4: Filter Tanggal
                item {
                    val isSelected = selectedFilter == "FILTER_TANGGAL"
                    val filterTanggalLabel = if (isSelected && !customDateLabel.isNullOrBlank()) {
                        customDateLabel!!
                    } else {
                        "Filter Tanggal"
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { showDateRangePickerDialog = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Pilih Tanggal",
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        label = {
                            Text(
                                text = filterTanggalLabel,
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4F46E5),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF374151),
                            iconColor = Color(0xFF374151)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Color.Transparent,
                            borderColor = Color(0xFFE2E8F0),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 0.dp
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Main Grouped List ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (val state = uiState) {
                    is QueueHistoryUiState.Loading -> {
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
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 56.dp)
                            ) {
                                displayedGroupedHistory.forEach { (dateHeader, historyItems) ->
                                    item {
                                        GroupHeaderRow(
                                            dateHeader = dateHeader,
                                            count = historyItems.size
                                        )
                                    }

                                    items(historyItems) { history ->
                                        val matchedTransaksi = transaksiMap[history.transactionId]
                                            ?: transaksiMap[history.transactionId.removePrefix("#")]
                                            ?: transaksiMap.values.find { tx ->
                                                tx.transactionNumber.equals(history.transactionId, ignoreCase = true) ||
                                                tx.transactionId.equals(history.transactionId, ignoreCase = true) ||
                                                tx.transactionNumber.removePrefix("#").equals(history.transactionId.removePrefix("#"), ignoreCase = true)
                                            }

                                        QueueHistoryCard(
                                            history = history,
                                            transaksi = matchedTransaksi
                                        )
                                    }
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
                                            itemName = "riwayat antrean"
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
private fun QueueHistoryCard(
    history: QueueHistory,
    transaksi: PenjualanTransaksi?
) {
    val context = LocalContext.current
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
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontFamily = interfamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!history.isSynced || transaksi?.isSynced == false) {
                        PendingSyncBadge()
                    }

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
            }

            // Subtitle: Customer Name & Table Number
            val subtitleText = buildString {
                if (history.customerName.isNotBlank()) append(history.customerName)
                if (history.tableNumber.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append("Meja: ${history.tableNumber}")
                }
            }

            if (subtitleText.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitleText,
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

                        val isPpnAktif = transaksi.isTaxActive()
                        val isPpnEksklusif = transaksi.checkIsPpnEksklusif()
                        val formattedPercent = transaksi.getFormattedPercentage()
                        val effectivePpn = transaksi.getEffectivePpnAmount()
                        val totalSubtotal = if (transaksi.subtotal > 0) transaksi.subtotal else transaksi.items.sumOf { it.totalPrice }
                        val discountAmount = transaksi.discount
                        val grandTotal = if (transaksi.total > 0) transaksi.total else (totalSubtotal - discountAmount + (if (isPpnEksklusif) effectivePpn else 0.0)).coerceAtLeast(0.0)

                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

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

                        if (discountAmount > 0) {
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
                                    text = "- ${rupiahFormat.format(discountAmount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A),
                                    fontFamily = interfamily
                                )
                            }
                        }

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
                                text = rupiahFormat.format(grandTotal),
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
                    if (history.tableNumber.isNotBlank()) {
                        DetailRow(label = "Nomor Meja", value = history.tableNumber)
                    }
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
