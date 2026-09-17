package com.ptpws.ikikasir.screens.produk

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.presentation.viewmodel.DetailProdukViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailProdukScreen(
    onBack: () -> Unit = {},
    produkId: String? = null,
    viewModel: DetailProdukViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(produkId) {
        if (!produkId.isNullOrBlank()) {
            viewModel.loadProduk(produkId)
        }
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            Toast.makeText(context, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.requestDeleteDialog(false) },
            title = {
                Text(
                    text = "Hapus Produk",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus produk \"${state.produk?.name ?: ""}\"? Tindakan ini tidak dapat dibatalkan.",
                    fontFamily = interfamily
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteProduk() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Hapus", color = Color.White, fontFamily = interfamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.requestDeleteDialog(false) }) {
                    Text("Batal", fontFamily = interfamily)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Produk",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 18.sp,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            state.produk?.let { produk ->
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol Delete Icon (Merah Soft)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { viewModel.requestDeleteDialog(true) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Hapus Produk",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Tombol Edit Produk (Biru Utama)
                        Button(
                            onClick = { navigateToEditProduk(context, produk) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Produk",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        if (state.isLoading && state.produk == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4F46E5))
            }
        } else {
            val produk = state.produk
            if (produk != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // 1. Gambar Banner + Badges Overlay
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (produk.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = produk.imageUrl,
                                        contentDescription = produk.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Inventory2,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                }

                                // Overlay Badges
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Kategori Badge Left
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(alpha = 0.95f)
                                    ) {
                                        Text(
                                            text = state.categoryName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interfamily,
                                            color = Color(0xFF1E293B),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }

                                    // Stok Status Badge Right
                                    val isStokLow = produk.stock <= produk.lowStockThreshold
                                    val isStokEmpty = produk.stock <= 0
                                    val (badgeBg, badgeText, badgeLabel) = when {
                                        isStokEmpty -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), "STOK HABIS")
                                        isStokLow -> Triple(Color(0xFFFEE2E2), Color(0xFFEF4444), "STOK RENDAH")
                                        else -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "STOK AMAN")
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = badgeBg
                                    ) {
                                        Text(
                                            text = badgeLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = interfamily,
                                            color = badgeText,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Info Utama Produk Card (Pills, Title, Line Divider, Harga Jual & Beli Cards presisi persis sama tinggi)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Pills Row: Barcode & Kategori
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCode,
                                                contentDescription = null,
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = produk.barcode.ifBlank { "8992753210123" },
                                                fontSize = 13.sp,
                                                fontFamily = interfamily,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFEEF2FF)
                                    ) {
                                        Text(
                                            text = "Kategori: ${state.categoryName}",
                                            fontSize = 13.sp,
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4F46E5),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Nama Produk
                                Text(
                                    text = produk.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interfamily,
                                    color = Color(0xFF0F172A)
                                )

                                // Divider Pemisah
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = Color(0xFFF1F5F9),
                                    thickness = 1.dp
                                )

                                // Pricing Cards Grid (Set IntrinsicSize.Max & fillMaxHeight agar persis SAMA TINGGI)
                                val labaKotor = produk.sellingPrice - produk.costPrice
                                val marginPercent = if (produk.sellingPrice > 0) (labaKotor / produk.sellingPrice * 100) else 0.0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Max),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Left Card: HARGA JUAL
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "HARGA JUAL",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF64748B)
                                                )
                                                Text(
                                                    text = "Rp ${formatRupiah(produk.sellingPrice)}",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF4F46E5)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFECFDF5)
                                            ) {
                                                Text(
                                                    text = "Margin ~${String.format(Locale.US, "%.1f%%", marginPercent)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF059669),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Right Card: HARGA BELI (MODAL)
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = "HARGA BELI",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF64748B)
                                                )
                                                Text(
                                                    text = "(MODAL)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Rp ${formatRupiah(produk.costPrice)}",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = interfamily,
                                                color = Color(0xFF1E293B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Manajemen Inventaris Card (Cards persis SAMA TINGGI)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Header Row dengan Icon 3D Cube
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_cube_3d),
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Manajemen Inventaris",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interfamily,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    Text(
                                        text = "Sesuaikan",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interfamily,
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.clickable { navigateToEditProduk(context, produk) }
                                    )
                                }

                                // Stock Cards Grid (Tersedia & Minimum) - PERSIS SAMA TINGGI
                                val isLowStock = produk.stock <= produk.lowStockThreshold
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Max),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Stok Tersedia
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isLowStock) Color(0xFFFEF2F2) else Color(0xFFF8FAFC)
                                        ),
                                        border = BorderStroke(1.dp, if (isLowStock) Color(0xFFFEE2E2) else Color(0xFFF1F5F9))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "Stok Tersedia",
                                                    fontSize = 12.sp,
                                                    fontFamily = interfamily,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isLowStock) Color(0xFFEF4444) else Color(0xFF64748B)
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${produk.stock}",
                                                        fontSize = 22.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = interfamily,
                                                        color = if (isLowStock) Color(0xFFEF4444) else Color(0xFF0F172A)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Unit",
                                                        fontSize = 13.sp,
                                                        fontFamily = interfamily,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isLowStock) Color(0xFFEF4444) else Color(0xFF64748B)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            if (isLowStock) {
                                                Text(
                                                    text = "⚠️ Di bawah batas minimum",
                                                    fontSize = 10.sp,
                                                    fontFamily = interfamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFEF4444)
                                                )
                                            } else {
                                                Text(
                                                    text = "Stok mencukupi",
                                                    fontSize = 10.sp,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF16A34A)
                                                )
                                            }
                                        }
                                    }

                                    // Stok Minimum
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "Stok Minimum",
                                                    fontSize = 12.sp,
                                                    fontFamily = interfamily,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF64748B)
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${produk.lowStockThreshold}",
                                                        fontSize = 22.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = interfamily,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Unit",
                                                        fontSize = 13.sp,
                                                        fontFamily = interfamily,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Notifikasi otomatis aktif",
                                                fontSize = 10.sp,
                                                fontFamily = interfamily,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }

                                // Sub-Row: HPP & Estimasi Laba Kotor
                                val labaKotor = produk.sellingPrice - produk.costPrice
                                val isProfitable = labaKotor >= 0

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Harga Modal Pokok (HPP)",
                                                fontSize = 11.sp,
                                                fontFamily = interfamily,
                                                color = Color(0xFF64748B)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Rp ${formatRupiah(produk.costPrice)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = interfamily,
                                                color = Color(0xFF0F172A)
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Estimasi Laba Kotor",
                                                fontSize = 11.sp,
                                                fontFamily = interfamily,
                                                color = Color(0xFF64748B)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isProfitable) "+Rp ${formatRupiah(labaKotor)} / unit" else "-Rp ${formatRupiah(kotlin.math.abs(labaKotor))} / unit",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = interfamily,
                                                color = if (isProfitable) Color(0xFF16A34A) else Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Pengaturan Diskon Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Pengaturan Diskon",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interfamily,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    val hasDiscount = produk.discount > 0
                                    Text(
                                        text = if (hasDiscount) "Diskon Aktif" else "Tidak Ada Diskon",
                                        fontSize = 12.sp,
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.Medium,
                                        color = if (hasDiscount) Color(0xFF16A34A) else Color(0xFF94A3B8)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Diskon Produk",
                                            fontSize = 13.sp,
                                            fontFamily = interfamily,
                                            color = Color(0xFF475569)
                                        )

                                        val discountText = if (produk.discount > 0) {
                                            if (produk.discountType == "PERCENT") "${produk.discount}%" else "Rp ${formatRupiah(produk.discount)}"
                                        } else {
                                            "0% (Tidak aktif)"
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Text(
                                                text = discountText,
                                                fontSize = 12.sp,
                                                fontFamily = interfamily,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Visibilitas Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Visibilitas",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interfamily,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PointOfSale,
                                            contentDescription = null,
                                            tint = Color(0xFF059669),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Tampilkan di Kasir",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = interfamily,
                                                color = Color(0xFF0F172A)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Langsung muncul pada layar pemesanan kasir",
                                                fontSize = 11.sp,
                                                fontFamily = interfamily,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Switch(
                                            checked = produk.isVisibleInCashier,
                                            onCheckedChange = { viewModel.onVisibilityToggle(it) },
                                            modifier = Modifier.scale(0.75f),
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF059669),
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = Color(0xFFE2E8F0)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 6. Ringkasan Penjualan Card (PERSIS SAMA TINGGI)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.BarChart,
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Ringkasan Penjualan",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interfamily,
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    Text(
                                        text = "Bulan Ini",
                                        fontSize = 12.sp,
                                        fontFamily = interfamily,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Max),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Terjual Hari Ini",
                                                fontSize = 12.sp,
                                                fontFamily = interfamily,
                                                color = Color(0xFF64748B)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = "${state.terjualHariIni}",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "unit",
                                                    fontSize = 12.sp,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Total 30 Hari Terakhir",
                                                fontSize = 12.sp,
                                                fontFamily = interfamily,
                                                color = Color(0xFF64748B)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = "${state.total30HariTerakhir}",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "unit",
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
            }
        }
    }
}

private fun formatRupiah(amount: Double): String {
    return try {
        NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount.toLong())
    } catch (e: Exception) {
        "0"
    }
}

private fun navigateToEditProduk(context: Context, produk: Produk) {
    val intent = Intent(context, TambahProdukActivity::class.java).apply {
        putExtra("produkId", produk.id)
        putExtra("produkName", produk.name)
        putExtra("produkCostPrice", produk.costPrice.toString())
        putExtra("produkSellingPrice", produk.sellingPrice.toString())
        putExtra("produkPrice", produk.sellingPrice.toString())
        putExtra("produkStock", produk.stock.toString())
        putExtra("produkLowStockThreshold", produk.lowStockThreshold.toString())
        putExtra("produkCategoryId", produk.categoryId)
        putExtra("produkImageUrl", produk.imageUrl)
        putExtra("produkDiscount", produk.discount.toString())
        putExtra("produkDiscountType", produk.discountType)
        putExtra("produkBarcode", produk.barcode)
        putExtra("produkIsVisibleInCashier", produk.isVisibleInCashier)
    }
    context.startActivity(intent)
}
