package com.ptpws.ikikasir.screens.manajemenstok

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.manajemenstok.presentation.viewmodel.ManajemenStokViewModel
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.screens.navigation.AppScreen
import com.ptpws.ikikasir.screens.produk.DetailProdukActivity
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenStokScreen(
    navController: NavController,
    onBack: () -> Unit = {},
    viewModel: ManajemenStokViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // ML Kit Barcode Scanner Launcher
    val barcodeScanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedCode = result.data?.getStringExtra("SCAN_RESULT")
            if (!scannedCode.isNullOrBlank()) {
                viewModel.onSearchQueryChange(scannedCode)
            }
        }
    }

    LaunchedEffect(state.isSuccessRestock) {
        if (state.isSuccessRestock) {
            Toast.makeText(context, "Stok berhasil diperbarui", Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessRestock()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Quick Restock Dialog
    state.selectedRestockProduk?.let { produk ->
        AlertDialog(
            onDismissRequest = { viewModel.closeRestockDialog() },
            title = {
                Text(
                    text = "Restock Stok Produk",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Tambah stok untuk produk \"${produk.name}\".",
                        fontSize = 13.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF475569)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Stok Saat Ini: ${produk.stock} Unit",
                            fontSize = 13.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    OutlinedTextField(
                        value = state.restockTambahJumlahText,
                        onValueChange = { viewModel.onRestockJumlahChange(it) },
                        label = { Text("Jumlah Restock (Tambah Stok)", fontSize = 12.sp, fontFamily = interfamily) },
                        placeholder = { Text("Contoh: 10", fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.simpanRestock() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Simpan Restock", color = Color.White, fontFamily = interfamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.closeRestockDialog() }) {
                    Text("Batal", fontFamily = interfamily)
                }
            }
        )
    }

    // Filter & Sort Logic
    val categoryProductMap = remember(state.produkList) {
        state.produkList.groupingBy { it.categoryId }.eachCount()
    }

    val filteredList = remember(state.produkList, state.searchQuery, state.selectedCategoryId, state.isFilterKritisOnly) {
        state.produkList.filter { produk ->
            val matchesQuery = state.searchQuery.isBlank() ||
                    produk.name.contains(state.searchQuery, ignoreCase = true) ||
                    produk.barcode.contains(state.searchQuery, ignoreCase = true)
            val matchesCategory = state.selectedCategoryId == null || produk.categoryId == state.selectedCategoryId
            val matchesKritis = !state.isFilterKritisOnly || (produk.stock <= produk.lowStockThreshold)

            matchesQuery && matchesCategory && matchesKritis
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manajemen Stok",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 18.sp,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!navController.popBackStack()) {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(AppScreen.Riwayat.route) }) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Riwayat Stok",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. Total Unit Keseluruhan Card (Gradient Blue)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF3843DB), Color(0xFF2E33CA))
                                )
                            )
                            .padding(horizontal = 22.dp, vertical = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "TOTAL UNIT KESELURUHAN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interfamily,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = NumberFormat.getNumberInstance(Locale("id", "ID")).format(state.totalUnitKeseluruhan),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = interfamily,
                                    color = Color.White
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_cube_3d),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Card Peringatan Stok (Restock Warning)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF2)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Warning Circle Icon with Red Dot Badge
                            Box(modifier = Modifier.size(42.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFFE4E6),
                                    modifier = Modifier
                                        .size(38.dp)
                                        .align(Alignment.BottomStart)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFE11D48),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                // Red Notification Dot Badge on Top-Right Edge
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                        .border(1.5.dp, Color(0xFFFFFDF2), CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color(0xFFFFE4E6)
                                ) {
                                    Text(
                                        text = "PERINGATAN STOK",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = interfamily,
                                        letterSpacing = 0.5.sp,
                                        color = Color(0xFFE11D48),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = if (state.jumlahRestockDibutuhkan == 1) "1 Produk Stoknya Menipis" else "${state.jumlahRestockDibutuhkan} Produk Stoknya Menipis",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interfamily,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Search & Scan Bar
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = {
                        Text(
                            text = "Cari Produk atau SKU",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = interfamily
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEEF2FF),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable {
                                    triggerBarcodeScanner(context, barcodeScanLauncher) { code ->
                                        viewModel.onSearchQueryChange(code)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan",
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Scan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interfamily,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF4F46E5)
                    )
                )
            }

            // 4. Section Header: Daftar Produk with Item Count on Far Right
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Produk",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF111827)
                    )

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Text(
                            text = "${filteredList.size} Item",
                            fontSize = 12.sp,
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // 6. Product Stock Item Cards List
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada produk stok yang ditemukan",
                            fontSize = 14.sp,
                            fontFamily = interfamily,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                items(
                    items = filteredList,
                    key = { it.id }
                ) { produk ->
                    StokProdukCardItem(
                        produk = produk,
                        onRestock = { navController.navigate(AppScreen.UpdateStok.routeWith(produk.id)) },
                        onDetail = {

                            val intent = Intent(context, DetailProdukActivity::class.java).apply {
                                putExtra("produkId", produk.id)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StokProdukCardItem(
    produk: Produk,
    onRestock: () -> Unit,
    onDetail: () -> Unit
) {
    val isKritis = produk.stock <= produk.lowStockThreshold

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isKritis) Color(0xFFFCA5A5) else Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Upper Row: Image, Name & SKU, Stock Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image
                if (produk.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = produk.imageUrl,
                        contentDescription = produk.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1F5F9))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Middle Info Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isKritis) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFE4E6)
                        ) {
                            Text(
                                text = "⚠️ KRITIS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interfamily,
                                color = Color(0xFFE11D48),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = produk.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "SKU: ${produk.barcode.ifBlank { "ARB-TRJ-250" }}",
                        fontSize = 11.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF64748B)
                    )
                }

                // Right Column: Total Stock Value
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL STOK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = if (isKritis) Color(0xFFE11D48) else Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${produk.stock}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = if (isKritis) Color(0xFFE11D48) else Color(0xFF0F172A)
                    )
                }
            }

            // Lower Row: Action Buttons (RESTOCK & Eye/Detail Icon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRestock,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isKritis) Color(0xFFE11D48) else Color(0xFF4F46E5)
                    )
                ) {
                    Text(
                        text = "RESTOCK",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onDetail() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = "Detail Produk",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun triggerBarcodeScanner(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
    onResult: (String) -> Unit
) {
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
                    onResult(rawValue)
                    Toast.makeText(context, "Barcode discan: $rawValue", Toast.LENGTH_SHORT).show()
                }
            }
    } catch (e: Exception) {
        val scanIntent = Intent("com.google.zxing.client.android.SCAN").apply {
            putExtra("SCAN_MODE", "PRODUCT_MODE")
        }
        if (scanIntent.resolveActivity(context.packageManager) != null) {
            launcher.launch(scanIntent)
        }
    }
}