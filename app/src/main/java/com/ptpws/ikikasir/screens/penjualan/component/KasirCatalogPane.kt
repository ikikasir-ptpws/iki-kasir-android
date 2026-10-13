package com.ptpws.ikikasir.screens.penjualan.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.penjualan.domain.model.getEffectivePrice
import com.ptpws.ikikasir.feature.penjualan.presentation.state.KasirState
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import java.text.NumberFormat
import java.util.Locale

private val PrimaryRoyalBlue = Color(0xFF3B32D1)

/**
 * Panel Katalog Produk untuk sisi kiri Split-Screen pada Tablet & POS.
 * Menampilkan bilah pencarian, pemindai barcode, filter kategori, dan katalog produk grid adaptif.
 */
@Composable
fun KasirCatalogPane(
    state: KasirState,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onIncrement: (Produk) -> Unit,
    onDecrement: (Produk) -> Unit,
    onScanBarcode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatRupiah = remember {
        { amount: Double ->
            NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(amount.toLong())
        }
    }

    val filteredList = remember(state.produkKatalog, state.catalogSearchQuery, state.selectedCategoryId) {
        state.produkKatalog.filter { produk ->
            val matchQuery = if (state.catalogSearchQuery.isBlank()) true else {
                produk.name.contains(state.catalogSearchQuery, ignoreCase = true) ||
                        produk.barcode.contains(state.catalogSearchQuery, ignoreCase = true)
            }
            val matchCategory = if (state.selectedCategoryId == null) true else {
                produk.categoryId == state.selectedCategoryId
            }
            matchQuery && matchCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 1. Search Bar + Barcode Scanner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari Produk",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = state.catalogSearchQuery,
                        onValueChange = onSearchChange,
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
                                        text = "Cari nama atau barcode produk...",
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

            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clickable { onScanBarcode() },
                shape = RoundedCornerShape(12.dp),
                color = PrimaryRoyalBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ── 2. Category Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            item {
                val isSelected = state.selectedCategoryId == null
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) PrimaryRoyalBlue else Color.White,
                    border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.clickable { onCategoryChange(null) }
                ) {
                    Text(
                        text = "Semua (${state.produkKatalog.size})",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            items(state.kategoriList, key = { it.id }) { kategori ->
                val isSelected = state.selectedCategoryId == kategori.id
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) PrimaryRoyalBlue else Color.White,
                    border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.clickable { onCategoryChange(kategori.id) }
                ) {
                    Text(
                        text = kategori.name,
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // ── 3. Adaptive Grid Katalog Produk
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_box_archive),
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tidak ada produk yang sesuai",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 135.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(filteredList, key = { it.id }) { produk ->
                    val currentQty = state.getItemQuantity(produk.id)
                    val effectivePrice = produk.getEffectivePrice(state.taxSetting)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (currentQty < produk.stock) {
                                    onIncrement(produk)
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(
                            width = if (currentQty > 0) 1.5.dp else 1.dp,
                            color = if (currentQty > 0) PrimaryRoyalBlue else Color(0xFFE2E8F0)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (currentQty > 0) 2.dp else 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            // Gambar Produk & Badge Kuantitas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (produk.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = produk.imageUrl,
                                        contentDescription = produk.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_box_archive),
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                // Badge terpilih jika ada di keranjang
                                if (currentQty > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryRoyalBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$currentQty",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interfamily
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Nama Produk
                            Text(
                                text = produk.name,
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Sisa Stok
                            val isLow = produk.stock <= produk.lowStockThreshold
                            Text(
                                text = "Stok: ${produk.stock}",
                                fontFamily = interfamily,
                                fontSize = 10.sp,
                                color = if (isLow) Color(0xFFE11D48) else Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Harga & Tombol Tambah
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rp ${formatRupiah(effectivePrice)}",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryRoyalBlue
                                )

                                if (currentQty > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFF1F5F9),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable { onDecrement(produk) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Kurang",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = PrimaryRoyalBlue,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable {
                                                    if (currentQty < produk.stock) onIncrement(produk)
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Tambah",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (produk.stock > 0) PrimaryRoyalBlue else Color(0xFFCBD5E1),
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clickable(enabled = produk.stock > 0) { onIncrement(produk) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
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
        }
    }
}
