package com.ptpws.ikikasir.screens.keuangan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ptpws.ikikasir.commond.CustomDateRangePickerDialog
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.auditlog.domain.model.AuditLog
import com.ptpws.ikikasir.feature.auditlog.presentation.viewmodel.AuditLogViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(
    navController: NavController,
    onBack: () -> Unit = {},
    viewModel: AuditLogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDateRangePicker by remember { mutableStateOf(false) }

    if (showDateRangePicker) {
        CustomDateRangePickerDialog(
            initialStartDateMillis = state.startDateMillis,
            initialEndDateMillis = state.endDateMillis,
            onDismissRequest = { showDateRangePicker = false },
            onDateRangeSelected = { start, end ->
                viewModel.onCustomDateRangeSelected(start, end)
                showDateRangePicker = false
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Auditlog",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.currentDestination?.route == "audit_log") {
                            navController.popBackStack()
                        } else {
                            onBack()
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
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Search Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    BasicTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
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
                                            text = "Cari aktivitas atau staf...",
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
            }

            // Filter Kategori Chips
            item {
                val categories = listOf("Semua", "Transaksi", "Stok", "Harga", "Autentikasi", "Promo", "Sistem")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(categories.size) { index ->
                        val label = categories[index]
                        val isSelected = state.selectedCategory == label
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategorySelected(label) },
                            label = {
                                Text(
                                    text = label,
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

            // Filter Tanggal
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FILTER TANGGAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 0.5.sp
                        )
                        IconButton(
                            onClick = { showDateRangePicker = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Pilih rentang tanggal",
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    val dateFilters = listOf("Semua", "Hari Ini", "7 Hari Terakhir")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dateFilters.size) { index ->
                            val label = dateFilters[index]
                            val isSelected = state.selectedDateFilter == label
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onDateFilterSelected(label) },
                                label = {
                                    Text(
                                        text = label,
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

                        item {
                            val isSelected = state.selectedDateFilter == "Filter Tanggal"
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (state.startDateMillis != null && state.endDateMillis != null) {
                                        viewModel.onDateFilterSelected("Filter Tanggal")
                                    } else {
                                        showDateRangePicker = true
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (isSelected) state.customDateLabel ?: "Filter Tanggal" else "Pilih Tanggal",
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
            }

            // Loading state
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
            } else if (state.filteredLogs.isEmpty()) {
                // Empty state
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum Ada Aktivitas Auditlog",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF374151)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Setiap transaksi, perubahan stok, harga, dan aktivitas sistem akan tercatat di sini.",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Render Logs with Date Headers
                val groupedLogs = state.filteredLogs.groupBy { formatTimestampToDateHeader(it.timestamp) }

                groupedLogs.forEach { (dateHeader, logsInGroup) ->
                    item(key = "header_$dateHeader") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFFE5E7EB)
                            )
                            Text(
                                text = dateHeader,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interfamily,
                                color = Color(0xFF9CA3AF),
                                letterSpacing = 0.5.sp
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFFE5E7EB)
                            )
                        }
                    }

                    items(logsInGroup, key = { it.id }) { log ->
                        val iconInfo = getAuditLogIconAndColors(log)
                        AuditlogItem(
                            icon = iconInfo.icon,
                            iconBgColor = iconInfo.bgColor,
                            iconTint = iconInfo.tint,
                            judul = log.title,
                            jam = formatTimestampToTime(log.timestamp),
                            deskripsi = log.description,
                            avatarInisial = getInitials(log.actorName),
                            avatarBgColor = getAvatarBgColor(log.actorName),
                            namaStaf = log.actorName.ifBlank { "System" },
                            actorRole = log.actorRole,
                            isPending = !log.isSynced,
                            isBahaya = log.isWarning
                        )
                    }
                }
            }
        }
    }
}

private data class IconInfo(
    val icon: ImageVector,
    val bgColor: Color,
    val tint: Color
)

private fun getAuditLogIconAndColors(log: AuditLog): IconInfo {
    if (log.isWarning) {
        return IconInfo(
            icon = Icons.Default.Cancel,
            bgColor = Color(0xFFFEE2E2),
            tint = Color(0xFFEF4444)
        )
    }

    return when (log.category.uppercase()) {
        "TRANSACTION" -> IconInfo(
            icon = Icons.Default.Description,
            bgColor = Color(0xFF4F46E5),
            tint = Color.White
        )
        "STOCK" -> IconInfo(
            icon = Icons.Default.Inventory,
            bgColor = Color(0xFFE5E7EB),
            tint = Color(0xFF374151)
        )
        "PRICE" -> IconInfo(
            icon = Icons.Default.LocalOffer,
            bgColor = Color(0xFFE5E7EB),
            tint = Color(0xFF374151)
        )
        "AUTHENTICATION" -> IconInfo(
            icon = Icons.Default.Logout,
            bgColor = Color(0xFFE5E7EB),
            tint = Color(0xFF374151)
        )
        "PROMO" -> IconInfo(
            icon = Icons.Default.LocalOffer,
            bgColor = Color(0xFFE5E7EB),
            tint = Color(0xFF4F46E5)
        )
        else -> IconInfo(
            icon = Icons.Default.Settings,
            bgColor = Color(0xFFE5E7EB),
            tint = Color(0xFF374151)
        )
    }
}

private fun formatTimestampToTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTimestampToDateHeader(timestamp: Long): String {
    val logCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val todayCal = Calendar.getInstance()
    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        logCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                logCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR) -> "HARI INI"

        logCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
                logCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR) -> "KEMARIN"

        else -> {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            sdf.format(Date(timestamp)).uppercase()
        }
    }
}

private fun getInitials(name: String): String {
    if (name.isBlank()) return "SY"
    val parts = name.trim().split("\\s+".toRegex())
    return when {
        parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
        parts.size == 1 && parts[0].length >= 2 -> parts[0].substring(0, 2).uppercase()
        else -> name.take(2).uppercase()
    }
}

private fun getAvatarBgColor(name: String): Color {
    val colors = listOf(
        Color(0xFF6B7280),
        Color(0xFF7C3AED),
        Color(0xFFD97706),
        Color(0xFF059669),
        Color(0xFF2563EB),
        Color(0xFFDC2626)
    )
    val hash = kotlin.math.abs(name.hashCode())
    return colors[hash % colors.size]
}

@Composable
fun AuditlogItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    judul: String,
    jam: String,
    deskripsi: String,
    avatarInisial: String,
    avatarBgColor: Color,
    namaStaf: String,
    actorRole: String,
    isPending: Boolean,
    isBahaya: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBahaya) Color(0xFFFFF5F5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon kotak kiri
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Konten kanan
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Baris judul + jam
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = judul,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        color = if (isBahaya) Color(0xFFEF4444) else Color(0xFF111827),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = jam,
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // Deskripsi
                Text(
                    text = deskripsi,
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Baris avatar + nama staf
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Avatar inisial
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(avatarBgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarInisial,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Oleh: $namaStaf · ${actorRole.ifBlank { "Tanpa role" }}",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isPending) {
                        Text(
                            text = "Pending",
                            fontFamily = interfamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB45309),
                            modifier = Modifier
                                .background(Color(0xFFFFF7ED), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}