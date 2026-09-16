package com.ptpws.ikikasir.screens.penjualan

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import coil.compose.AsyncImage
import android.content.Intent
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.KasirViewModel
import com.ptpws.ikikasir.screens.penjualan.component.PilihProdukTersediaDialog
import java.text.NumberFormat
import java.util.Locale

// Primary Royal Blue Brand Color matching Gambar
private val PrimaryRoyalBlue = Color(0xFF3B32D1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirScreen(
    navController: NavController,
    onScanProduk: () -> Unit = {},
    onBayar: () -> Unit = {},
    viewModel: KasirViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val cartItems = state.cartItems
    val totalItemCount = state.totalItemCount
    val subtotal = state.subtotal

    // Barcode scanner launcher
    val barcodeScanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra("SCAN_RESULT")
            if (!code.isNullOrBlank()) {
                viewModel.onBarcodeScanned(code)
            }
        }
    }

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
                        viewModel.onBarcodeScanned(rawValue)
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
            val scanIntent = Intent("com.google.zxing.client.android.SCAN").apply {
                putExtra("SCAN_MODE", "PRODUCT_MODE")
            }
            if (scanIntent.resolveActivity(context.packageManager) != null) {
                barcodeScanLauncher.launch(scanIntent)
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
            viewModel.clearError()
        }
    }

    // Filter item keranjang berdasarkan searchQuery (Hanya mencari produk yang SUDAH DIPILIH di keranjang)
    val filteredCartItems = remember(cartItems, state.searchQuery) {
        if (state.searchQuery.isBlank()) {
            cartItems
        } else {
            cartItems.filter {
                it.produk.name.contains(state.searchQuery, ignoreCase = true) ||
                        it.produk.barcode.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }

    val formatRupiah = remember {
        { amount: Double ->
            NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(amount.toLong())
        }
    }

    // Modal Sheet catalog for "+ Pilih Produk"
    if (state.showProductCatalogDialog) {
        PilihProdukTersediaDialog(
            state = state,
            onDismiss = { viewModel.openProductCatalogDialog(false) },
            onSearchQueryChange = { viewModel.onCatalogSearchQueryChange(it) },
            onCategoryFilterChange = { viewModel.onCategoryFilterChange(it) },
            onIncrement = { viewModel.incrementCartItem(it) },
            onDecrement = { viewModel.decrementCartItem(it) },
            onSelesai = { viewModel.openProductCatalogDialog(false) }
        )
    }

    // Dialog Tambah Catatan Pesanan
    if (state.showOrderNoteDialog) {
        var tempNote by remember { mutableStateOf(state.orderNote) }

        AlertDialog(
            onDismissRequest = { viewModel.openOrderNoteDialog(false) },
            title = {
                Text(
                    text = "Catatan Pesanan",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                OutlinedTextField(
                    value = tempNote,
                    onValueChange = { tempNote = it },
                    placeholder = {
                        Text("Masukkan catatan pesanan (misal: Cokelat Keju, Less Sugar)...", fontSize = 13.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onOrderNoteChange(tempNote)
                        viewModel.openOrderNoteDialog(false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRoyalBlue)
                ) {
                    Text("Simpan", fontFamily = interfamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.openOrderNoteDialog(false) }) {
                    Text("Batal", fontFamily = interfamily, color = Color(0xFF64748B))
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kasir Pintar",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF0F172A),
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!navController.popBackStack()) {
                            // Back action
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = PrimaryRoyalBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A),
                    navigationIconContentColor = PrimaryRoyalBlue
                )
            )
        },
        bottomBar = {
            // Bottom Bar berisi Subtotal Info & Tombol BAYAR
            Surface(
                color = Color.White,
                shadowElevation = 12.dp,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Subtotal Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Subtotal ($totalItemCount Item)",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Termasuk PPN 11%",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }

                        Text(
                            text = "Rp ${formatRupiah(subtotal)}",
                            fontFamily = interfamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // BAYAR Button
                    Button(
                        onClick = onBayar,
                        enabled = totalItemCount > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRoyalBlue,
                            disabledContainerColor = Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "BAYAR",
                                fontFamily = interfamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalItemCount > 0) Color.White else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (totalItemCount > 0) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── 1. Search Bar + QR Scanner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input Card (Mentapis produk terpilih)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
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
                            contentDescription = "Search",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp,
                                fontFamily = interfamily
                            ),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (state.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Cari produk...",
                                            fontFamily = interfamily,
                                            fontSize = 14.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // QR Scanner Square Button
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { triggerBarcodeScanner() },
                    shape = RoundedCornerShape(14.dp),
                    color = PrimaryRoyalBlue
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // ── 2. Produk di pilih Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Produk di pilih",
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )
                Text(
                    text = "$totalItemCount",
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            // ── 3. Main Content: Empty State vs Halaman Keranjang Aktif
            if (cartItems.isEmpty()) {
                // ── Empty State ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.size(110.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_shopping_bag),
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Tidak ada produk yang dipilih",
                            fontFamily = interfamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Silakan pilih produk dari katalog atau scan barcode untuk memulai transaksi.",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 28.dp)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = { viewModel.openProductCatalogDialog(true) },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryRoyalBlue
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pilih Produk",
                                    fontFamily = interfamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { triggerBarcodeScanner() },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Scan QR",
                                    fontFamily = interfamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            } else {
                // ── Halaman Keranjang Aktif ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // PESANAN AKTIF + Hapus Semua Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PESANAN AKTIF",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.clearCart() }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Semua",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Hapus Semua",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    // Main Container Box: List Keranjang (Scrollable) + Floating Fixed Button di Pojok Kanan Bawah
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (filteredCartItems.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ada produk di keranjang yang cocok dengan pencarian",
                                    fontFamily = interfamily,
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 68.dp)
                            ) {
                                // Product Item Cards
                                items(filteredCartItems, key = { it.produk.id }) { item ->
                                    val categoryName = state.kategoriList.find { it.id == item.produk.categoryId }?.name ?: "Makanan"

                                    CartItemCard(
                                        item = item,
                                        categoryName = categoryName,
                                        onIncrement = { viewModel.updateQuantity(item.produk.id, item.quantity + 1) },
                                        onDecrement = { viewModel.updateQuantity(item.produk.id, item.quantity - 1) },
                                        onRemove = { viewModel.removeFromCart(item.produk.id) },
                                        formatRupiah = formatRupiah
                                    )
                                }

                                // Catatan Pesanan Card
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.openOrderNoteDialog(true) },
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Edit,
                                                    contentDescription = null,
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = if (state.orderNote.isBlank()) "Catatan Pesanan" else state.orderNote,
                                                    fontFamily = interfamily,
                                                    fontSize = 13.sp,
                                                    color = if (state.orderNote.isBlank()) Color(0xFF64748B) else Color(0xFF0F172A),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Text(
                                                text = if (state.orderNote.isBlank()) "Tambah Catatan" else "Ubah",
                                                fontFamily = interfamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = PrimaryRoyalBlue
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Floating Fixed Sticky Button: SELALU DIAM di Pojok Kanan Bawah tepat di atas Subtotal (Baik produk 1, 2, maupun 50)
                        DashedTambahProdukButton(
                            onClick = { viewModel.openProductCatalogDialog(true) },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Custom Dashed Border Button (+ Tambah Produk Lainnya) Rata Kanan Fixed Sticky
@Composable
fun DashedTambahProdukButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stroke = Stroke(
        width = 2.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )
    val borderColor = Color(0xFFC7D2FE)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = borderColor,
                    style = stroke,
                    cornerRadius = CornerRadius(100.dp.toPx())
                )
            }
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryRoyalBlue,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = "Tambah Produk Lainnya",
                fontFamily = interfamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRoyalBlue
            )
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    categoryName: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    formatRupiah: (Double) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.size(70.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (item.produk.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = item.produk.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_box_archive),
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                // Name & Close [X] Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.produk.name,
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus Item",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable { onRemove() }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Category Pill Badge + Stock status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Dark Pill Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF525252)
                    ) {
                        Text(
                            text = categoryName.ifBlank { "Makanan" },
                            fontFamily = interfamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Stock status badge
                    val isLowStock = item.produk.stock <= item.produk.lowStockThreshold
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLowStock) Color(0xFFFFE4E6) else Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "Sisa ${item.produk.stock} Unit",
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) Color(0xFFE11D48) else Color(0xFF16A34A),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (item.note.isNotBlank()) {
                        Text(
                            text = "• ${item.note}",
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price & Counter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rp ${formatRupiah(item.produk.price)}",
                        fontFamily = interfamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRoyalBlue
                    )

                    // Counter Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { onDecrement() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Kurangi",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Text(
                                text = "${item.quantity}",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.widthIn(min = 16.dp),
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryRoyalBlue)
                                    .clickable { onIncrement() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KasirScreenPreview() {
    MaterialTheme {
        KasirScreen(navController = rememberNavController())
    }
}