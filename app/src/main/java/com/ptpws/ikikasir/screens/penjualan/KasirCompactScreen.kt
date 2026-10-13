package com.ptpws.ikikasir.screens.penjualan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.penjualan.presentation.state.KasirState
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.KasirViewModel
import com.ptpws.ikikasir.screens.penjualan.component.PilihProdukTersediaDialog
import java.text.NumberFormat
import java.util.Locale

private val PrimaryRoyalBlue = Color(0xFF3B32D1)

/**
 * Tampilan Kasir 1 Kolom (Compact) yang dioptimasi untuk HP (Portrait & Landscape).
 * Mempertahankan alur kerja, keranjang aktif, dan dialog pemilihan produk.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirCompactScreen(
    state: KasirState,
    viewModel: KasirViewModel,
    navController: NavController,
    onBayar: () -> Unit,
    triggerBarcodeScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems = state.cartItems
    val totalItemCount = state.totalItemCount

    val formatRupiah = remember {
        { amount: Double ->
            NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(amount.toLong())
        }
    }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                    IconButton(onClick = { navController.popBackStack() }) {
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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                fontSize = 12.5.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.ppnLabel,
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.taxSetting.isActive) Color(0xFF059669) else Color(0xFF94A3B8)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Rp ${formatRupiah(state.grandTotal)}",
                                fontFamily = interfamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            if (state.promoDiscountAmount > 0) {
                                Text(
                                    text = "Hemat Rp ${formatRupiah(state.promoDiscountAmount)}",
                                    fontFamily = interfamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }

                    // BAYAR Button
                    Button(
                        onClick = onBayar,
                        enabled = totalItemCount > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
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
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalItemCount > 0) Color.White else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (totalItemCount > 0) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
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

            // ── 3. Main Content: Empty State vs Keranjang Aktif
            if (cartItems.isEmpty()) {
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
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_shopping_bag),
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Tidak ada produk yang dipilih",
                            fontFamily = interfamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Silakan pilih produk dari katalog atau scan barcode untuk memulai transaksi.",
                            fontFamily = interfamily,
                            fontSize = 12.5.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { viewModel.openProductCatalogDialog(true) },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
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
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { triggerBarcodeScanner() },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
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
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
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
                                items(filteredCartItems, key = { it.produk.id }) { item ->
                                    val categoryName = state.kategoriList.find { it.id == item.produk.categoryId }?.name ?: "Makanan"

                                    CartItemCard(
                                        item = item,
                                        categoryName = categoryName,
                                        taxSetting = state.taxSetting,
                                        onIncrement = { viewModel.updateQuantity(item.produk.id, item.quantity + 1) },
                                        onDecrement = { viewModel.updateQuantity(item.produk.id, item.quantity - 1) },
                                        onRemove = { viewModel.removeFromCart(item.produk.id) },
                                        formatRupiah = formatRupiah
                                    )
                                }
                            }
                        }

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
