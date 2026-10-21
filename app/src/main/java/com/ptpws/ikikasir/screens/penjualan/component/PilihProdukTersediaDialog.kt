package com.ptpws.ikikasir.screens.penjualan.component

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.GlobalScrollPaginationFooter
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.penjualan.domain.model.getEffectivePrice
import com.ptpws.ikikasir.feature.penjualan.presentation.state.KasirState
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PilihProdukTersediaDialog(
    state: KasirState,
    onDismiss: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onIncrement: (Produk) -> Unit,
    onDecrement: (Produk) -> Unit,
    onSelesai: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }

    val formatRupiah = remember {
        { amount: Double ->
            NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(amount.toLong())
        }
    }

    val filteredList = remember(state.produkKatalog, state.catalogSearchQuery, state.selectedCategoryId, state.cartItems, state.kategoriList) {
        state.produkKatalog
            .filter { produk ->
                // Tampilkan produk yang memiliki stok atau sudah terpilih di keranjang
                val hasStock = produk.stock > 0 || state.getItemQuantity(produk.id) > 0
                val isVisible = produk.isVisibleInCashier
                val matchesQuery = state.catalogSearchQuery.isBlank() ||
                        produk.name.contains(state.catalogSearchQuery, ignoreCase = true) ||
                        produk.barcode.contains(state.catalogSearchQuery, ignoreCase = true)
                val matchesCategory = if (state.selectedCategoryId == null) {
                    true
                } else {
                    val selectedCategoryName = state.kategoriList.find { it.id == state.selectedCategoryId }?.name
                    produk.categoryId.equals(state.selectedCategoryId, ignoreCase = true) ||
                    (selectedCategoryName != null && produk.categoryId.equals(selectedCategoryName, ignoreCase = true)) ||
                    state.kategoriList.find { it.name.equals(produk.categoryId, ignoreCase = true) }?.id == state.selectedCategoryId
                }
                hasStock && isVisible && matchesQuery && matchesCategory
            }
            .sortedWith(
                // Produk yang sudah dipilih (qty > 0) otomatis naik ke paling atas
                compareByDescending<Produk> { state.getItemQuantity(it.id) > 0 }
                    .thenByDescending { state.getItemQuantity(it.id) }
                    .thenBy { it.name }
            )
    }

    // Scroll Pagination State (Tampilkan 10 data awal, muat 10 data berikutnya saat scroll)
    val pageSize = 10
    var displayedCount by remember(state.catalogSearchQuery, state.selectedCategoryId) {
        mutableIntStateOf(pageSize)
    }
    val displayedList = remember(filteredList, displayedCount) {
        filteredList.take(displayedCount)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val isCompactWidth = screenWidth < 600.dp
        val isCompactHeight = screenHeight < 520.dp

        // Penyesuaian ukuran dialog adaptif sesuai layar (HP Portrait, HP Landscape, Tablet Portrait, Tablet Landscape)
        val (dialogWidthFraction, maxDialogWidth, dialogHeightFraction) = when {
            // HP Landscape (pendek tapi lebar)
            isLandscape && screenHeight < 520.dp -> Triple(0.92f, 620.dp, 0.96f)
            // HP Portrait (< 600dp)
            isCompactWidth -> Triple(0.95f, 480.dp, 0.90f)
            // Tablet Portrait (600dp - 899dp)
            !isLandscape -> Triple(0.85f, 720.dp, 0.88f)
            // Tablet Landscape (>= 900dp)
            else -> Triple(0.70f, 860.dp, 0.88f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = maxDialogWidth)
                    .fillMaxWidth(dialogWidthFraction)
                    .fillMaxHeight(dialogHeightFraction),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = if (isCompactWidth) 14.dp else 20.dp,
                            vertical = if (isCompactHeight) 10.dp else 16.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 8.dp else 12.dp)
                ) {

                // ── 1. Dialog Header (Title, Subtitle & Close Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pilih Produk Tersedia",
                            fontFamily = interfamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pilih item untuk ditambahkan ke pesanan",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // ── 2. Search Input Field
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = state.catalogSearchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontFamily = interfamily
                            ),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (state.catalogSearchQuery.isEmpty()) {
                                        Text(
                                            text = "Cari nama produk atau SKU...",
                                            fontFamily = interfamily,
                                            fontSize = 13.sp,
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

                // ── 3. Category Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        val isSelected = state.selectedCategoryId == null
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF3B32D1) else Color(0xFFF1F5F9),
                            modifier = Modifier.clickable { onCategoryFilterChange(null) }
                        ) {
                            Text(
                                text = "Semua",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                            )
                        }
                    }

                    items(state.kategoriList, key = { it.id }) { kategori ->
                        val isSelected = state.selectedCategoryId == kategori.id
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF3B32D1) else Color(0xFFF1F5F9),
                            modifier = Modifier.clickable { onCategoryFilterChange(kategori.id) }
                        ) {
                            Text(
                                text = kategori.name,
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // ── 4. Scrollable Product Cards List
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada produk yang cocok",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(displayedList, key = { it.id }) { produk ->
                            val currentQty = state.getItemQuantity(produk.id)
                            val isSelected = currentQty > 0
                            val canIncrement = currentQty < produk.stock
                            val canDecrement = currentQty > 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFF8FAFC) else Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF3B32D1).copy(alpha = 0.45f) else Color(0xFFE2E8F0)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = if (isCompactWidth) 10.dp else 12.dp,
                                            vertical = if (isCompactHeight) 6.dp else (if (isCompactWidth) 8.dp else 12.dp)
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Product Image
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF8FAFC),
                                        modifier = Modifier.size(if (isCompactHeight) 44.dp else (if (isCompactWidth) 48.dp else 60.dp))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (produk.imageUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = produk.imageUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_box_archive),
                                                    contentDescription = null,
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(if (isCompactWidth) 22.dp else 26.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(if (isCompactWidth) 8.dp else 12.dp))

                                    // Product Title, Category, Stock & Price
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = produk.name,
                                            fontFamily = interfamily,
                                            fontSize = if (isCompactWidth) 13.5.sp else 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        val matchedCategory = state.kategoriList.find {
                                            it.id.equals(produk.categoryId, ignoreCase = true) ||
                                            it.name.equals(produk.categoryId, ignoreCase = true)
                                        }
                                        val categoryName = matchedCategory?.name ?: produk.categoryId.takeIf { it.isNotBlank() } ?: ""

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (categoryName.isNotBlank()) {
                                                Text(
                                                    text = categoryName,
                                                    fontFamily = interfamily,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF94A3B8),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                Text(
                                                    text = "•",
                                                    fontFamily = interfamily,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFCBD5E1)
                                                )
                                            }
                                            val isOutOfStock = produk.stock <= 0
                                            val isLowStock = produk.stock <= produk.lowStockThreshold
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when {
                                                    isOutOfStock -> Color(0xFFFEE2E2)
                                                    isLowStock -> Color(0xFFFEF3C7)
                                                    else -> Color(0xFFDCFCE7)
                                                }
                                            ) {
                                                Text(
                                                    text = when {
                                                        isOutOfStock -> "Habis"
                                                        isLowStock -> "Sisa ${produk.stock}"
                                                        else -> "Stok ${produk.stock}"
                                                    },
                                                    fontFamily = interfamily,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        isOutOfStock -> Color(0xFFDC2626)
                                                        isLowStock -> Color(0xFFD97706)
                                                        else -> Color(0xFF15803D)
                                                    },
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        val effectivePrice = produk.getEffectivePrice(state.taxSetting)
                                        if (effectivePrice <= 0.0) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Text(
                                                    text = "Rp 0",
                                                    fontFamily = interfamily,
                                                    fontSize = if (isCompactWidth) 13.5.sp else 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFDC2626)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFFEE2E2)
                                                ) {
                                                    Text(
                                                        text = "Harga belum diatur",
                                                        fontFamily = interfamily,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFFDC2626),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "Rp ${formatRupiah(effectivePrice)}",
                                                fontFamily = interfamily,
                                                fontSize = if (isCompactWidth) 13.5.sp else 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF3B32D1)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Quantity Modifier Counter Container
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Minus Icon Button (Disabled if qty == 0)
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isCompactWidth) 24.dp else 28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (canDecrement) Color.White else Color(0xFFF1F5F9))
                                                    .clickable(enabled = canDecrement) {
                                                        if (canDecrement) onDecrement(produk)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Kurangi",
                                                    tint = if (canDecrement) Color(0xFF64748B) else Color(0xFFCBD5E1),
                                                    modifier = Modifier.size(if (isCompactWidth) 14.dp else 16.dp)
                                                )
                                            }

                                            Text(
                                                text = "$currentQty",
                                                fontFamily = interfamily,
                                                fontSize = if (isCompactWidth) 12.5.sp else 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (canDecrement) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                                modifier = Modifier.widthIn(min = 16.dp),
                                                textAlign = TextAlign.Center
                                            )

                                            // Plus Icon Button (Solid Blue when available, Grayed & disabled when max stock reached)
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isCompactWidth) 24.dp else 28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (canIncrement) Color(0xFF3B32D1) else Color(0xFFCBD5E1))
                                                    .clickable(enabled = canIncrement) {
                                                        if (canIncrement) onIncrement(produk)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Tambah",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(if (isCompactWidth) 14.dp else 16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Scroll Pagination Footer (CircularProgressIndicator saat discroll)
                        if (filteredList.isNotEmpty()) {
                            item(key = "scroll_pagination_footer") {
                                GlobalScrollPaginationFooter(
                                    displayedCount = displayedCount,
                                    totalItems = filteredList.size,
                                    onLoadMore = {
                                        displayedCount = minOf(displayedCount + pageSize, filteredList.size)
                                    },
                                    itemName = "produk"
                                )
                            }
                        }
                    }
                }

                // ── 5. Bottom Action Button: Selesai Memilih (Fixed & Responsive Height)
                val buttonHeight = when {
                    isCompactHeight -> 44.dp
                    isCompactWidth -> 48.dp
                    else -> 52.dp
                }

                Button(
                    onClick = onSelesai,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonHeight),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B32D1)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selesai Memilih",
                            fontFamily = interfamily,
                            fontSize = if (isCompactWidth) 14.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color(0xFF2563EB),
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text(
                                text = "${state.totalItemCount} Item",
                                fontFamily = interfamily,
                                fontSize = if (isCompactWidth) 11.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}
