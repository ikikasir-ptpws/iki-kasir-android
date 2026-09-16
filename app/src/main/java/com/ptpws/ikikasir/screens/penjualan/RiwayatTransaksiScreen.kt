package com.ptpws.ikikasir.screens.penjualan

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.RiwayatTransaksiViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatTransaksiScreen(
    navController: NavController,
    viewModel: RiwayatTransaksiViewModel = hiltViewModel(),
    onDetailTransaksi: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Launcher for legacy ZXing barcode scanner fallback
    val barcodeScanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contents = result.data?.getStringExtra("SCAN_RESULT")
            if (!contents.isNullOrBlank()) {
                viewModel.onSearchQueryChange(contents)
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
                        viewModel.onSearchQueryChange(rawValue)
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

    // Function to show native DatePickerDialog for custom date filter
    fun showDatePicker() {
        val cal = Calendar.getInstance()
        if (state.selectedCustomDateMillis != null) {
            cal.timeInMillis = state.selectedCustomDateMillis!!
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                viewModel.onCustomDateSelect(selectedCal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Riwayat Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF0F172A),
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
                    containerColor = Color(0xFFF8FAFC)
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Barcode Scan Icon + Search Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Input Box
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
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
                                        tint = Color(0xFF94A3B8),
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
                                                fontSize = 13.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )
                    }

                    // Barcode Scanner Icon Button
                    Card(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { triggerBarcodeScanner() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Barcode Transaksi",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Filter Chips (Semua, Hari Ini, 7 Hari Terakhir, Filter Tanggal)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    // Chip 1: Semua
                    item {
                        val isSelected = state.selectedFilter == "Semua"
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onFilterSelect("Semua") },
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
                        val isSelected = state.selectedFilter == "Hari Ini"
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onFilterSelect("Hari Ini") },
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
                        val isSelected = state.selectedFilter == "7 Hari Terakhir"
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onFilterSelect("7 Hari Terakhir") },
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

                    // Chip 4: Filter Tanggal (Custom Date Picker)
                    item {
                        val isSelected = state.selectedFilter == "Filter Tanggal"
                        val chipText = if (isSelected && !state.customDateLabel.isNullOrBlank()) {
                            state.customDateLabel!!
                        } else {
                            "Filter Tanggal"
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { showDatePicker() },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Pilih Tanggal",
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = chipText,
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
            } else if (state.groupedTransactions.isEmpty()) {
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
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Render Grouped Transactions by Date Header (Gambar 1 Layout)
                state.groupedTransactions.forEach { group ->
                    item(key = "header_${group.dateHeader}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Vertical Indicator Pill (Gambar 1 style)
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(18.dp)
                                    .background(Color(0xFF4F46E5), RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = group.dateHeader,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interfamily,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.weight(1f))

                            // Total Transactions Badge Pill (Gambar 1 style)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = group.countText,
                                    fontSize = 11.sp,
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }

                    // Render transaction items inside group
                    items(group.transactions, key = { it.transactionId }) { transaksi ->
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val dateObj = transaksi.createdAt.toDate()
                        val jamText = timeFormat.format(dateObj)
                        val formattedPrice = "Rp " + NumberFormat.getInstance(Locale("id", "ID")).format(transaksi.total.toLong())

                        TransaksiCardItem(
                            kodeTransaksi = transaksi.transactionNumber,
                            jam = jamText,
                            metodePembayaran = transaksi.paymentMethod,
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
}

@Composable
fun TransaksiCardItem(
    kodeTransaksi: String,
    jam: String,
    metodePembayaran: String,
    totalHarga: String,
    statusBayar: String,
    isSynced: Boolean = true,
    onDetail: () -> Unit
) {
    val isLunas = statusBayar.equals("COMPLETED", ignoreCase = true) || statusBayar.equals("LUNAS", ignoreCase = true) || statusBayar.equals("Berhasil", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetail() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Line 1: Invoice ID + Status Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = kodeTransaksi,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )

                    // Pending Badge (if offline unsynced)
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
                    }

                    // Berhasil / LUNAS Status Badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isLunas) Color(0xFFE6F4F1) else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isLunas) "Berhasil" else statusBayar,
                            fontSize = 10.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isLunas) Color(0xFF0D9488) else Color(0xFFD97706)
                        )
                    }
                }

                // Line 2: Time WIB • Payment Method
                Text(
                    text = "$jam WIB • $metodePembayaran",
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Line 1 Right: Price + Chevron Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = totalHarga,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(12.dp)
                )
            }
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