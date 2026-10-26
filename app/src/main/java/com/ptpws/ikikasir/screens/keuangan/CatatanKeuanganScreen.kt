package com.ptpws.ikikasir.screens.keuangan

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptpws.ikikasir.commond.GlobalScrollPaginationFooter
import com.ptpws.ikikasir.commond.GlobalTextField
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.catatankeuangan.presentation.viewmodel.CatatanKeuanganViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatRupiah(amount: Double): String {
    val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp " + numberFormat.format(amount.toLong())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatanKeuanganScreen(
    navController: NavController? = null,
    onBack: () -> Unit = {},
    onTambahCatatan: () -> Unit = {},
    onDetailCatatan: (String) -> Unit = {},
    onEditCatatan: (String) -> Unit = {},
    viewModel: CatatanKeuanganViewModel = hiltViewModel()
) {
    var isBackProcessing by remember { mutableStateOf(false) }
    val handleBack = {
        if (!isBackProcessing) {
            isBackProcessing = true
            onBack()
        }
    }
    BackHandler(enabled = !isBackProcessing, onBack = handleBack)

    val state by viewModel.state.collectAsState()
    var catatanToDelete by remember { mutableStateOf<CatatanKeuangan?>(null) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshCatatanList()
    }

    // Scroll Pagination State (Tampilkan 10 data awal)
    val pageSize = 10
    var displayedCount by remember(
        state.searchQuery,
        state.selectedFilterTab,
        state.startDateMillis,
        state.endDateMillis
    ) {
        mutableIntStateOf(pageSize)
    }
    val displayedCatatanList = remember(state.filteredList, displayedCount) {
        state.filteredList.take(displayedCount)
    }
    val groupedCatatan = remember(displayedCatatanList) {
        displayedCatatanList.groupBy { it.date.ifBlank { "Tanggal tidak diketahui" } }
    }

    if (showDateRangePicker) {
        com.ptpws.ikikasir.commond.CustomDateRangePickerDialog(
            initialStartDateMillis = state.startDateMillis,
            initialEndDateMillis = state.endDateMillis,
            onDismissRequest = { showDateRangePicker = false },
            onDateRangeSelected = { start, end ->
                viewModel.onDateRangeSelected(start, end)
                showDateRangePicker = false
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Catatan Keuangan",
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            color = Color(0xFF0F172A),
                            fontSize = 19.sp
                        )
                        Text(
                            text = "Pemasukan & Pengeluaran Usaha",
                            fontFamily = interfamily,
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onTambahCatatan() },
                modifier = Modifier.padding(bottom = 16.dp),
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Catatan Keuangan",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── 1. KPI SUMMARY CARDS (Startup Style) ─────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Pemasukan Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(96.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.TrendingUp,
                                            contentDescription = null,
                                            tint = Color(0xFF059669),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "PEMASUKAN",
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color(0xFF047857),
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Text(
                                        text = formatRupiah(state.totalPemasukan),
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF065F46),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Total Pengeluaran Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(96.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.TrendingDown,
                                            contentDescription = null,
                                            tint = Color(0xFFE11D48),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "PENGELUARAN",
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color(0xFFBE123C),
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Text(
                                        text = formatRupiah(state.totalPengeluaran),
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF9F1239),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Saldo Bersih Card (Hero Banner)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "SALDO BERSIH",
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = formatRupiah(state.saldoBersih),
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Text(
                                text = "${state.catatanList.size} Transaksi",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // ── 2. SEARCH BAR ─────────────────────────────────────────────
            item {
                GlobalTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = "Cari judul atau catatan ..",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── 3. FILTER CHIPS ──────────────────────────────────────────
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    val filters = listOf("Semua", "Pemasukan", "Pengeluaran", "Filter Tanggal")
                    items(filters) { filterText ->
                        val isSelected = state.selectedFilterTab == filterText
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (filterText == "Filter Tanggal") {
                                    if (state.selectedFilterTab != filterText) {
                                        viewModel.onFilterTabSelected(filterText)
                                    }
                                    showDateRangePicker = true
                                } else {
                                    viewModel.onFilterTabSelected(filterText)
                                }
                            },
                            leadingIcon = if (filterText == "Filter Tanggal") {
                                {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            } else {
                                null
                            },
                            label = {
                                Text(
                                    text = if (filterText == "Filter Tanggal" && isSelected) {
                                        state.customDateLabel ?: filterText
                                    } else {
                                        filterText
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF475569),
                                iconColor = Color(0xFF475569)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = Color.Transparent,
                                borderWidth = 0.dp,
                                selectedBorderWidth = 0.dp
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // ── 4. CATATAN ITEMS LIST ─────────────────────────────────────
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2563EB))
                    }
                }
            } else if (state.filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.catatanList.isEmpty()) "Belum ada catatan keuangan" else "Tidak ada catatan yang sesuai",
                            fontFamily = interfamily,
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                groupedCatatan.forEach { (date, records) ->
                    item(key = "catatan_header_$date") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = date.uppercase(Locale("id", "ID")),
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = "${records.size} Transaksi",
                                    fontFamily = interfamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    items(items = records, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDetailCatatan(item.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Icon Badge
                            Surface(
                                shape = CircleShape,
                                color = if (item.isPemasukan) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (item.isPemasukan) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (item.isPemasukan) Color(0xFF16A34A) else Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Main Info Column
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontFamily = interfamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (!item.isSynced) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                text = "Pending",
                                                fontFamily = interfamily,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD97706),
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = (if (item.isPemasukan) "+ " else "- ") + formatRupiah(item.amount),
                                    fontFamily = interfamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isPemasukan) Color(0xFF059669) else Color(0xFFEF4444)
                                )

                                Text(
                                    text = buildString {
                                        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(item.createdAt.toDate())
                                        append("$time WIB")
                                        if (item.paymentMethod.isNotBlank()) append(" • ${item.paymentMethod}")
                                    },
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Right Amount & Actions
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEEF2FF)) {
                                        IconButton(
                                            onClick = {
                                                onEditCatatan(item.id)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = "Edit",
                                                tint = Color(0xFF4F46E5),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF1F2)) {
                                        IconButton(
                                            onClick = { catatanToDelete = item },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Hapus",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
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

            // Scroll Pagination Footer
            if (state.filteredList.isNotEmpty()) {
                item(key = "scroll_pagination_footer") {
                    GlobalScrollPaginationFooter(
                        displayedCount = displayedCount,
                        totalItems = state.filteredList.size,
                        onLoadMore = {
                            displayedCount = minOf(displayedCount + pageSize, state.filteredList.size)
                        },
                        itemName = "catatan"
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    catatanToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { catatanToDelete = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Hapus Catatan",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus catatan \"${target.title}\"?",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCatatan(target.id, target.title)
                        catatanToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Hapus",
                        color = Color.White,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { catatanToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Batal",
                        fontFamily = interfamily,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        )
    }
}
