package com.ptpws.ikikasir.screens.produk

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.kategori.presentation.viewmodel.KategoriViewModel
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.presentation.viewmodel.ProdukViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarProdukScreen(
    navController: NavController,
    onBack: () -> Unit = {},
    onTambah: () -> Unit = {},
    initialCategoryId: String? = null,
    viewModel: ProdukViewModel = hiltViewModel(),
    kategoriViewModel: KategoriViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val kategoriState by kategoriViewModel.state.collectAsState()
    val context = LocalContext.current

    // Apply initial category filter dari navigasi Kelola Menu
    // Jika initialCategoryId null/blank/"{categoryId}" → reset ke "Semua", jika ada ID valid → filter kategori tersebut
    LaunchedEffect(initialCategoryId) {
        val validCategoryId = if (initialCategoryId.isNullOrBlank() || initialCategoryId == "{categoryId}") null else initialCategoryId
        viewModel.onCategoryFilterChange(validCategoryId)
    }

    // Sorting State
    var selectedSortOption by remember { mutableStateOf("Terbaru") }
    var showSortDropdown by remember { mutableStateOf(false) }

    // Map Kategori ID to Kategori
    val categoryMap = remember(kategoriState.kategoriList) {
        kategoriState.kategoriList.associateBy { it.id }
    }

    // Map Kategori ID to Product Count
    val productCountMap = remember(state.produkList) {
        state.produkList.groupingBy { it.categoryId }.eachCount()
    }

    // Filtered Product List by search and category filter (shows ALL products in management screen)
    val filteredList = remember(state.produkList, state.searchQuery, state.selectedCategoryId) {
        state.produkList.filter { produk ->
            val matchesQuery = state.searchQuery.isBlank() ||
                    produk.name.contains(state.searchQuery, ignoreCase = true) ||
                    produk.barcode.contains(state.searchQuery, ignoreCase = true)
            val matchesCategory = state.selectedCategoryId == null || produk.categoryId == state.selectedCategoryId
            matchesQuery && matchesCategory
        }
    }

    // Sorted Product List
    val sortedList = remember(filteredList, selectedSortOption) {
        when (selectedSortOption) {
            "Nama (A-Z)" -> filteredList.sortedBy { it.name.lowercase() }
            "Stok (Terdikit)" -> filteredList.sortedBy { it.stock }
            "Harga (Termurah)" -> filteredList.sortedBy { it.sellingPrice }
            "Harga (Termahal)" -> filteredList.sortedByDescending { it.sellingPrice }
            else -> filteredList
        }
    }

    // Low stock count
    val lowStockCount = remember(state.produkList) {
        state.produkList.count { it.stock <= it.lowStockThreshold }
    }

    // Camera Barcode Scanner Fallback
    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra("SCAN_RESULT")
            if (!code.isNullOrBlank()) {
                viewModel.onSearchQueryChange(code)
            }
        }
    }

    fun triggerScanner() {
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
                        barcodeLauncher.launch(scanIntent)
                    }
                }
        } catch (e: Exception) {
            val scanIntent = Intent("com.google.zxing.client.android.SCAN").apply {
                putExtra("SCAN_MODE", "PRODUCT_MODE")
            }
            if (scanIntent.resolveActivity(context.packageManager) != null) {
                barcodeLauncher.launch(scanIntent)
            }
        }
    }

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

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Daftar Produk",
                            fontWeight = FontWeight.Bold,
                            fontFamily = interfamily,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (state.isOnline) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (state.isOnline) Color(0xFF059669) else Color(0xFFDC2626),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (state.isOnline) "Online" else "Offline",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.isOnline) Color(0xFF059669) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }
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
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sinkronisasi",
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC),
                    titleContentColor = Color(0xFF0F172A),
                    navigationIconContentColor = Color(0xFF1E293B)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, TambahProdukActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(bottom = 86.dp),
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Produk",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Header Stats Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Total Produk
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4F46E5))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Total: ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))) {
                                        append("${state.produkList.size}")
                                    }
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontFamily = interfamily
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(16.dp)
                                .width(1.dp),
                            color = Color(0xFFE2E8F0)
                        )

                        // Total Kategori
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))) {
                                        append("${kategoriState.kategoriList.size}")
                                    }
                                    append(" Kategori")
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontFamily = interfamily
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(16.dp)
                                .width(1.dp),
                            color = Color(0xFFE2E8F0)
                        )

                        // Stok Menipis Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFEF2F2)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$lowStockCount Menipis",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444),
                                    fontFamily = interfamily
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar & Barcode Scanner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (state.searchQuery.isEmpty()) {
                                Text(
                                    text = "Cari produk atau barcode...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8),
                                    fontFamily = interfamily
                                )
                            }
                            BasicTextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A),
                                    fontFamily = interfamily
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        IconButton(
                            onClick = { triggerScanner() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Barcode",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Filter Kategori Pills (Semua & Dynamic List with Counts)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    // "Semua" Pill
                    item {
                        val isSelected = state.selectedCategoryId == null
                        Surface(
                            onClick = { viewModel.onCategoryFilterChange(null) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF4F46E5) else Color.White,
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "Semua (${state.produkList.size})",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                fontFamily = interfamily,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Dynamic Category Pills
                    items(kategoriState.kategoriList) { kategori ->
                        val count = productCountMap[kategori.id] ?: 0
                        val isSelected = state.selectedCategoryId == kategori.id
                        val isCatOff = !kategori.isVisibleInCashier
                        Surface(
                            onClick = { viewModel.onCategoryFilterChange(kategori.id) },
                            shape = RoundedCornerShape(20.dp),
                            color = when {
                                isSelected -> Color(0xFF4F46E5)
                                isCatOff -> Color(0xFFF1F5F9)
                                else -> Color.White
                            },
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = if (isCatOff) "${kategori.name} ($count • Off)" else "${kategori.name} ($count)",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = when {
                                    isSelected -> Color.White
                                    isCatOff -> Color(0xFF94A3B8)
                                    else -> Color(0xFF475569)
                                },
                                fontFamily = interfamily,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Subheader: Label Jumlah Produk & Sorting Dropdown
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAFTAR PRODUK TERDAFTAR (${sortedList.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        fontFamily = interfamily,
                        letterSpacing = 0.3.sp
                    )

                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showSortDropdown = true }
                        ) {
                            Text(
                                text = "Urutkan: ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF0F172A),
                                fontFamily = interfamily
                            )
                            Text(
                                text = selectedSortOption,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontFamily = interfamily
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Urutkan",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortDropdown,
                            onDismissRequest = { showSortDropdown = false }
                        ) {
                            val options = listOf(
                                "Terbaru",
                                "Nama (A-Z)",
                                "Stok (Terdikit)",
                                "Harga (Termurah)",
                                "Harga (Termahal)"
                            )
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            fontSize = 13.sp,
                                            fontFamily = interfamily,
                                            fontWeight = if (option == selectedSortOption) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedSortOption = option
                                        showSortDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Loading Indicator
            if (state.isLoading && state.filteredList.isEmpty()) {
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
            }

            // Empty State
            if (!state.isLoading && sortedList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (state.searchQuery.isNotEmpty())
                                    "Produk \"${state.searchQuery}\" tidak ditemukan"
                                else
                                    "Belum ada produk",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF374151)
                            )
                            Text(
                                text = "Ketuk tombol + di bawah untuk menambahkan produk baru",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Product Items List
            items(
                items = sortedList,
                key = { it.id }
            ) { produk ->
                val category = categoryMap[produk.categoryId]
                val categoryName = category?.name ?: "Umum"
                val isCategoryActive = category?.isVisibleInCashier ?: true
                ProdukCardItem(
                    produk = produk,
                    categoryName = categoryName,
                    isCategoryActive = isCategoryActive,
                    onClick = {
                        val intent = Intent(context, DetailProdukActivity::class.java).apply {
                            putExtra("produkId", produk.id)
                        }
                        context.startActivity(intent)
                    },
                    onEdit = {
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
                    },
                    onDelete = {
                        viewModel.requestDeleteProduk(produk)
                    }
                )
            }
        }
    }

    // Delete Confirmation Dialog
    state.produkToDelete?.let { produk ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = {
                Text(
                    text = "Hapus Produk",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus produk \"${produk.name}\"?",
                    fontFamily = interfamily,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteProduk(produk.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Batal", color = Color(0xFF6B7280))
                }
            }
        )
    }
}

@Composable
fun CategoryBadge(categoryName: String) {
    val (bgColor, textColor) = remember(categoryName) {
        val lower = categoryName.lowercase()
        when {
            lower.contains("makanan") -> Color(0xFFDCFCE7) to Color(0xFF15803D)
            lower.contains("minuman") -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
            lower.contains("kopi") -> Color(0xFFFEF3C7) to Color(0xFFB45309)
            else -> Color(0xFFF1F5F9) to Color(0xFF475569)
        }
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = categoryName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            fontFamily = interfamily,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ProdukCardItem(
    produk: Produk,
    categoryName: String,
    isCategoryActive: Boolean = true,
    onClick: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isStokRendah = produk.stock <= produk.lowStockThreshold
    val isNonAktif = !produk.isVisibleInCashier || !isCategoryActive
    val cardAlpha = if (isNonAktif) 0.55f else 1.0f

    val formattedHarga = remember(produk.price) {
        val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))
        "Rp " + numberFormat.format(produk.price.toLong())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = cardAlpha }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (isStokRendah || isNonAktif) 6.dp else 0.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isNonAktif) Color(0xFFF8FAFC) else Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = when {
                isNonAktif -> BorderStroke(1.dp, Color(0xFFE2E8F0))
                isStokRendah -> BorderStroke(1.dp, Color(0xFFFECDD3))
                else -> BorderStroke(1.dp, Color(0xFFF1F5F9))
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image
                if (produk.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = produk.imageUrl,
                        contentDescription = produk.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Product Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryBadge(categoryName = categoryName)

                        if (isNonAktif) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = if (!produk.isVisibleInCashier) "Off" else "Kategori Off",
                                    fontSize = 10.sp,
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = produk.name,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        color = if (isNonAktif) Color(0xFF64748B) else Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Stok: ",
                            fontSize = 12.sp,
                            fontFamily = interfamily,
                            color = Color(0xFF64748B)
                        )
                        if (isStokRendah) {
                            Text(
                                text = "${produk.stock} Unit (Kritis)",
                                fontSize = 12.sp,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        } else {
                            Text(
                                text = "${produk.stock} Unit",
                                fontSize = 12.sp,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Text(
                        text = formattedHarga,
                        fontSize = 14.sp,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action Buttons Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Hapus",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Overlay Stok Rendah Badge
        if (isStokRendah) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEF4444)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "STOK RENDAH",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}