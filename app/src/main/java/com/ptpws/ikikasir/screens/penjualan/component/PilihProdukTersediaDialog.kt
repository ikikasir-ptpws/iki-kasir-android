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
import com.ptpws.ikikasir.commond.interfamily
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
    onSelesai: () -> Unit
) {
    val formatRupiah = remember {
        { amount: Double ->
            NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount.toLong())
        }
    }

    val filteredList = remember(state.produkKatalog, state.catalogSearchQuery, state.selectedCategoryId, state.cartItems) {
        state.produkKatalog
            .filter { produk ->
                // Sembunyikan produk stok habis atau yang tidak aktif di kasir
                val hasStock = produk.stock > 0
                val isVisible = produk.isVisibleInCashier
                val matchesQuery = state.catalogSearchQuery.isBlank() ||
                        produk.name.contains(state.catalogSearchQuery, ignoreCase = true) ||
                        produk.barcode.contains(state.catalogSearchQuery, ignoreCase = true)
                val matchesCategory = state.selectedCategoryId == null || produk.categoryId == state.selectedCategoryId
                hasStock && isVisible && matchesQuery && matchesCategory
            }
            .sortedWith(
                // Produk yang sudah dipilih (qty > 0) otomatis naik ke paling atas
                compareByDescending<Produk> { state.getItemQuantity(it.id) > 0 }
                    .thenByDescending { state.getItemQuantity(it.id) }
                    .thenBy { it.name }
            )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        items(filteredList, key = { it.id }) { produk ->
                            val currentQty = state.getItemQuantity(produk.id)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Product Image
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        modifier = Modifier.size(64.dp)
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
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Product Title, Category, Stock & Price
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = produk.name,
                                            fontFamily = interfamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        val categoryName = state.kategoriList.find { it.id == produk.categoryId }?.name ?: "Makanan"

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = categoryName.ifBlank { "Makanan" },
                                                fontFamily = interfamily,
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                            Text(
                                                text = "•",
                                                fontFamily = interfamily,
                                                fontSize = 12.sp,
                                                color = Color(0xFFCBD5E1)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = "Stok ${produk.stock}",
                                                    fontFamily = interfamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Rp ${formatRupiah(produk.price)}",
                                            fontFamily = interfamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3B32D1)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Quantity Modifier Counter Container
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Minus Icon Button
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .clickable { onDecrement(produk) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Kurangi",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Text(
                                                text = "$currentQty",
                                                fontFamily = interfamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A),
                                                modifier = Modifier.widthIn(min = 16.dp),
                                                textAlign = TextAlign.Center
                                            )

                                            val canIncrement = currentQty < produk.stock

                                            // Plus Icon Button (Solid Blue when available, Grayed when max stock reached)
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (canIncrement) Color(0xFF3B32D1) else Color(0xFFCBD5E1))
                                                    .clickable { onIncrement(produk) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Tambah",
                                                    tint = Color.White,
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

                // ── 5. Bottom Action Button: Selesai Memilih (Matches Gambar 1)
                Button(
                    onClick = onSelesai,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B32D1)
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Selesai Memilih",
                            fontFamily = interfamily,
                            fontSize = 15.sp,
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
