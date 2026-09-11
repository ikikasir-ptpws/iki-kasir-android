package com.ptpws.ikikasir.screens.kategori

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.presentation.util.KategoriIconHelper
import com.ptpws.ikikasir.feature.kategori.presentation.viewmodel.KategoriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarKategoriScreen(
    navController: NavController,
    onTambahPromo: () -> Unit = {},
    viewModel: KategoriViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearUserMessage()
        }
    }

    val totalMenu = state.filteredList.sumOf { it.productCount }
    val menuMenipis = state.filteredList.sumOf { it.lowStockCount + it.outOfStockCount }

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                // ── AppBar row ───────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF0F172A))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kategori Produk", fontWeight = FontWeight.Bold, fontFamily = interfamily, fontSize = 20.sp, color = Color(0xFF0F172A))
                        Text("Katalog POS & Manajemen Stok", fontFamily = interfamily, fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    // Green dot + count badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                            Text(
                                text = "${state.kategoriList.size} Kategori",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                }

                // ── Search bar row ───────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search field
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                            BasicTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontFamily = interfamily, fontSize = 13.sp, color = Color(0xFF0F172A)),
                                cursorBrush = SolidColor(Color(0xFF4F46E5)),
                                singleLine = true,
                                decorationBox = { inner ->
                                    if (state.searchQuery.isEmpty()) {
                                        Text("Cari kategori atau menu...", fontFamily = interfamily, fontSize = 13.sp, color = Color(0xFF94A3B8))
                                    }
                                    inner()
                                }
                            )
                        }
                    }
                    // Filter icon button
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF1F5F9)) {
                        Box(modifier = Modifier.padding(11.dp)) {
                            Icon(Icons.Default.Tune, contentDescription = "Filter", tint = Color(0xFF475569), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { context.startActivity(Intent(context, TambahKategoriActivity::class.java)) },
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.wrapContentWidth().height(50.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Tambah Kategori", fontFamily = interfamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Summary banner ───────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                        Text(
                            text = "$totalMenu Total Menu Siap Jual",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151)
                        )
                    }
                    if (menuMenipis > 0) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFF7ED)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(13.dp))
                                Text(
                                    text = "$menuMenipis Menu Menipis",
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEA580C)
                                )
                            }
                        }
                    }
                }
            }

            // ── Empty state ──────────────────────────────────────────────────
            if (state.filteredList.isEmpty() && !state.isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(56.dp))
                        Text(
                            text = if (state.searchQuery.isNotEmpty()) "Kategori tidak ditemukan" else "Belum ada kategori",
                            fontFamily = interfamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF374151)
                        )
                        Text(
                            text = if (state.searchQuery.isNotEmpty()) "Coba kata kunci lain" else "Ketuk Tambah Kategori untuk memulai",
                            fontFamily = interfamily, fontSize = 12.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Loading
            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4F46E5), modifier = Modifier.size(32.dp))
                    }
                }
            }

            // ── Category cards ───────────────────────────────────────────────
            items(items = state.filteredList, key = { it.id }) { kategori ->
                val iconOption = KategoriIconHelper.getIconOption(kategori.iconName)
                val accentColor = try {
                    Color(android.graphics.Color.parseColor(kategori.colorHex))
                } catch (e: Exception) { Color(0xFF4F46E5) }

                KategoriCardItem(
                    kategori = kategori,
                    icon = iconOption.icon,
                    accentColor = accentColor,
                    onEdit = {
                        val intent = Intent(context, TambahKategoriActivity::class.java).apply {
                            putExtra("kategoriId", kategori.id)
                            putExtra("kategoriNama", kategori.nama)
                            putExtra("kategoriDeskripsi", kategori.deskripsi)
                            putExtra("kategoriIcon", kategori.iconName)
                            putExtra("kategoriColor", kategori.colorHex)
                            putExtra("kategoriIsVisible", kategori.isVisibleInCashier)
                        }
                        context.startActivity(intent)
                    },
                    onDelete = { viewModel.requestDeleteKategori(kategori) },
                    onToggleCashier = { viewModel.toggleCashierVisibility(kategori) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // ── Delete dialog ────────────────────────────────────────────────────────
    state.kategoriToDelete?.let { kategori ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    Text("Hapus Kategori", fontWeight = FontWeight.Bold, fontFamily = interfamily, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    "Hapus kategori \"${kategori.nama}\"?\nData terkait akan dihapus permanen.",
                    fontFamily = interfamily, fontSize = 14.sp, color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteKategori(kategori.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Hapus", color = Color.White, fontFamily = interfamily, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.dismissDeleteDialog() }, shape = RoundedCornerShape(10.dp)) {
                    Text("Batal", fontFamily = interfamily, color = Color(0xFF6B7280))
                }
            }
        )
    }
}

// ─── Kategori Card ─────────────────────────────────────────────────────────────
@Composable
fun KategoriCardItem(
    kategori: Kategori,
    icon: ImageVector,
    accentColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleCashier: () -> Unit
) {
    val isHabis = kategori.outOfStockCount > 0 && kategori.outOfStockCount >= kategori.productCount
    val isStokKurang = kategori.outOfStockCount > 0 || kategori.lowStockCount > 0
    val isTerlaris = kategori.productCount >= 20 && !isHabis && !isStokKurang
    val isNonAktif = !kategori.isVisibleInCashier

    // Dimmed jika habis atau non-aktif
    val iconBgColor = if (isHabis || isNonAktif) Color(0xFFF1F5F9) else accentColor.copy(alpha = 0.13f)
    val iconTint = if (isHabis || isNonAktif) Color(0xFFCBD5E1) else accentColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (isNonAktif) 0.55f else 1.0f },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isNonAktif) Color(0xFFF8FAFC) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Section 1: Icon + Nama + Badge + Deskripsi + Stok ────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon box
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Name + badge row
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = kategori.nama,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            fontSize = 16.sp,
                            color = if (isHabis || isNonAktif) Color(0xFF94A3B8) else Color(0xFF0F172A),
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        when {
                            isNonAktif -> BadgePill(label = "KASIR OFF", color = Color(0xFF6B7280), withIcon = false)
                            isHabis -> BadgePill(label = "HABIS TERJUAL", color = Color(0xFFEF4444), withIcon = true)
                            isTerlaris -> BadgePill(label = "TERLARIS", color = Color(0xFF6B7280), withIcon = false)
                            isStokKurang -> BadgePill(label = "${kategori.lowStockCount + kategori.outOfStockCount} Stok Menipis", color = Color(0xFFEA580C), withIcon = false)
                        }
                        if (!kategori.isSynced) {
                            BadgePill(label = "Pending", color = Color(0xFFD97706), withIcon = false)
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Description
                    Text(
                        text = if (kategori.deskripsi.isNotBlank()) kategori.deskripsi else "Tidak ada deskripsi",
                        fontSize = 12.sp,
                        fontFamily = interfamily,
                        color = if (isHabis) Color(0xFFCBD5E1) else Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stats chips row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Menu count chip
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF1F5F9)) {
                            Text(
                                text = "${kategori.productCount} Menu",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF374151),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        // Stock status chip
                        if (isHabis || isStokKurang) {
                            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFF1F2)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                    Text(
                                        text = if (isHabis) "Habis Terjual / Stok Kosong" else "${kategori.outOfStockCount + kategori.lowStockCount} Stok Kurang",
                                        fontSize = 11.sp,
                                        fontFamily = interfamily,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        } else {
                            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF0FDF4)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                                    Text("Semua Tersedia", fontSize = 11.sp, fontFamily = interfamily, color = Color(0xFF16A34A))
                                }
                            }
                        }
                    }
                }

                // Drag handle dots (top right)
                Icon(Icons.Default.DragIndicator, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp))
            }

            // ── Divider ──────────────────────────────────────────────────────
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            // ── Section 2: Status stok + Kelola Menu ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    Text(
                        text = "Status stok & visibilitas\nitem",
                        fontSize = 11.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF94A3B8)
                    )
                }
                // Kelola Menu button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFF),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Kelola Menu (${kategori.productCount}\nProduk)",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF4F46E5)
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // ── Divider ──────────────────────────────────────────────────────
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            // ── Section 3: Toggle + Kasir label + Edit + Delete ──────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = kategori.isVisibleInCashier,
                    onCheckedChange = { onToggleCashier() },
                    modifier = Modifier.height(26.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF059669),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kasir: ",
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Text(
                    text = if (kategori.isVisibleInCashier) "Aktif" else "Nonaktif",
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (kategori.isVisibleInCashier) Color(0xFF059669) else Color(0xFF94A3B8),
                    modifier = Modifier.weight(1f)
                )

                // Edit button
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8FAFC)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF374151), modifier = Modifier.size(17.dp))
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                // Delete button
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFFF1F2)) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444), modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}

// ─── Badge Pill ────────────────────────────────────────────────────────────────
@Composable
fun BadgePill(label: String, color: Color, withIcon: Boolean) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (withIcon) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
            }
            Text(
                text = label,
                fontSize = 9.sp,
                fontFamily = interfamily,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DaftarKategoriScreenPreview() {
    MaterialTheme {
        DaftarKategoriScreen(navController = rememberNavController())
    }
}