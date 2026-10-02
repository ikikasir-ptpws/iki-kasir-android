package com.ptpws.ikikasir.screens.promo

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.model.isAvailableOn
import com.ptpws.ikikasir.feature.promo.domain.model.isEndingWithinDays
import com.ptpws.ikikasir.feature.promo.domain.model.isExpiredOn
import com.ptpws.ikikasir.feature.promo.domain.model.isUpcomingOn
import com.ptpws.ikikasir.feature.promo.presentation.viewmodel.PromoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenPromoScreen(
    onBack: () -> Unit = {},
    onTambahPromo: () -> Unit = {},
    onEditPromo: (String) -> Unit = {},
    viewModel: PromoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var promoToDelete by remember { mutableStateOf<Promo?>(null) }
    var selectedPromoForDetail by remember { mutableStateOf<Promo?>(null) }

    val activeCount = remember(state.promoList) { state.promoList.count { it.isAvailableOn() } }
    val endingThisWeekCount = remember(state.promoList) { state.promoList.count { it.isEndingWithinDays(7) } }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daftar Promo",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF0F172A),
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                onClick = onTambahPromo,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Tambah Promo",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. Search Bar ─────────────────────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            ),
                            cursorBrush = SolidColor(Color(0xFF2563EB)),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (state.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari promo ..",
                                        fontFamily = interfamily,
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            // ── 2. Stat Cards (Promo Aktif & Berakhir Minggu Ini) ─────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card Promo Aktif
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4F46E5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PROMO AKTIF",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$activeCount",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp,
                                    color = Color.White
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    // Card Berakhir Minggu Ini
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "BERAKHIR\nMINGGU INI",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFFEF4444),
                                    letterSpacing = 0.5.sp,
                                    lineHeight = 13.sp
                                )
                                Text(
                                    text = "$endingThisWeekCount",
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFEF4444).copy(alpha = 0.35f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }

            // ── 3. Filter Chips ──────────────────────────────────────────
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    val filters = listOf("Semua", "Aktif", "Akan Datang", "Kedaluwarsa")
                    items(filters) { filterText ->
                        val isSelected = state.selectedFilterTab == filterText
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.onFilterTabSelected(filterText) }
                        ) {
                            Text(
                                text = filterText,
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // ── 4. Promo Items List ──────────────────────────────────────
            if (state.filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada promo",
                            fontFamily = interfamily,
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                items(items = state.filteredList, key = { it.id }) { promo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPromoForDetail = promo },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Text Info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                if (!promo.isSynced) {
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
                                            fontFamily = interfamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }
                                Text(
                                    text = promo.name,
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                val nilaidiskonText = if (promo.discountType == "%") {
                                    "Diskon ${promo.discountValue.toInt()}%"
                                } else {
                                    "Potongan Rp ${promo.discountValue.toInt()}"
                                }
                                Text(
                                    text = nilaidiskonText,
                                    fontFamily = interfamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                                Text(
                                    text = "Periode: ${promo.startDate} - ${promo.endDate}",
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            // Right Controls Row (Switch + Edit + Delete + Chevron)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Switch(
                                    checked = promo.isActive && !promo.isExpiredOn(),
                                    enabled = !promo.isExpiredOn(),
                                    onCheckedChange = { viewModel.toggleStatus(promo.id, promo.isActive) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2563EB),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFCBD5E1),
                                        uncheckedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.scale(0.85f)
                                )

                                IconButton(
                                    onClick = { onEditPromo(promo.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Promo",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { promoToDelete = promo },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus Promo",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { selectedPromoForDetail = promo },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForwardIos,
                                        contentDescription = "Detail Promo",
                                        tint = Color(0xFFCBD5E1),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Promo Dialog
    selectedPromoForDetail?.let { detailPromo ->
        DetailPromoDialog(
            promo = detailPromo,
            onDismiss = { selectedPromoForDetail = null },
            onEdit = {
                selectedPromoForDetail = null
                onEditPromo(detailPromo.id)
            }
        )
    }

    // Delete Confirmation Dialog
    promoToDelete?.let { targetPromo ->
        AlertDialog(
            onDismissRequest = { promoToDelete = null },
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
                        text = "Hapus Promo",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus promo \"${targetPromo.name}\"?",
                    fontFamily = interfamily,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePromo(targetPromo.id)
                        promoToDelete = null
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
                    onClick = { promoToDelete = null },
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

@Composable
fun DetailPromoDialog(
    promo: Promo,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Detail Promo",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (promo.isAvailableOn()) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                ) {
                    Text(
                        text = when {
                            promo.isExpiredOn() -> "Kedaluwarsa"
                            promo.isUpcomingOn() -> "Akan Datang"
                            promo.isAvailableOn() -> "Aktif"
                            else -> "Nonaktif"
                        },
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (promo.isAvailableOn()) Color(0xFF16A34A) else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Nama Promo
                Column {
                    Text(
                        text = "Nama Promo",
                        fontSize = 11.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = promo.name,
                        fontSize = 15.sp,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                // Diskon & Tipe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Besar Diskon",
                            fontSize = 11.sp,
                            fontFamily = interfamily,
                            color = Color(0xFF64748B)
                        )
                        val diskonText = if (promo.discountType == "%") "${promo.discountValue.toInt()}%" else "Rp ${promo.discountValue.toInt()}"
                        Text(
                            text = diskonText,
                            fontSize = 14.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }

                    if (promo.promoType.isNotBlank()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tipe Promo",
                                fontSize = 11.sp,
                                fontFamily = interfamily,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = promo.promoType,
                                fontSize = 14.sp,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }

                // Periode
                Column {
                    Text(
                        text = "Periode Promo",
                        fontSize = 11.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${promo.startDate} - ${promo.endDate}",
                        fontSize = 13.sp,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                }

                // Produk Promo List
                if (promo.items.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Produk Terkait (${promo.items.size}):",
                            fontSize = 11.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                promo.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "• ${item.productName}",
                                            fontSize = 12.sp,
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (item.price > 0) {
                                            Text(
                                                text = "Rp ${item.price.toInt()}",
                                                fontSize = 12.sp,
                                                fontFamily = interfamily,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onEdit()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Edit Promo",
                        color = Color.White,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Tutup",
                    fontFamily = interfamily,
                    color = Color(0xFF6B7280)
                )
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ManajemenPromoScreenPreview() {
    ManajemenPromoScreen()
}