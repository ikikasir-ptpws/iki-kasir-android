package com.ptpws.ikikasir.screens.penjualan.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.penjualan.presentation.state.KasirState
import com.ptpws.ikikasir.screens.penjualan.CartItemCard
import java.text.NumberFormat
import java.util.Locale

private val PrimaryRoyalBlue = Color(0xFF3B32D1)

/**
 * Panel Keranjang Transaksi untuk sisi kanan Split-Screen pada Tablet & POS.
 * Menampilkan item terpilih, penyesuaian kuantitas, rincian subtotal & PPN, serta tombol BAYAR instan.
 */
@Composable
fun KasirCartPane(
    state: KasirState,
    onIncrement: (String, Int) -> Unit,
    onDecrement: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onClearCart: () -> Unit,
    onBayar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatRupiah = remember {
        { amount: Double ->
            NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(amount.toLong())
        }
    }

    val cartItems = state.cartItems
    val totalItemCount = state.totalItemCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // ── 1. Header Keranjang
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Keranjang Pesanan",
                    fontFamily = interfamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "$totalItemCount Item Terpilih",
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (cartItems.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier.clickable { onClearCart() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Semua",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Reset",
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF1F5F9))

        // ── 2. List Item Keranjang / Empty State
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            if (cartItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.size(80.dp)
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
                        text = "Keranjang Masih Kosong",
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Klik produk di katalog sebelah kiri untuk menambahkan pesanan",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(cartItems, key = { it.produk.id }) { item ->
                        val categoryName = state.kategoriList.find { it.id == item.produk.categoryId }?.name ?: "Produk"
                        CartItemCard(
                            item = item,
                            categoryName = categoryName,
                            taxSetting = state.taxSetting,
                            onIncrement = { onIncrement(item.produk.id, item.quantity + 1) },
                            onDecrement = { onDecrement(item.produk.id, item.quantity - 1) },
                            onRemove = { onRemove(item.produk.id) },
                            formatRupiah = formatRupiah
                        )
                    }
                }
            }
        }

        // ── 3. Bottom Summary & Tombol BAYAR
        Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Rincian Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtotal ($totalItemCount Item)",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Rp ${formatRupiah(state.subtotal)}",
                        fontFamily = interfamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }

                // Diskon Promo (jika ada)
                if (state.promoDiscountAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Diskon Promo",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF16A34A)
                        )
                        Text(
                            text = "- Rp ${formatRupiah(state.promoDiscountAmount)}",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }

                // PPN / Pajak
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.ppnLabel,
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (state.taxSetting.isActive) Color(0xFF059669) else Color(0xFF94A3B8)
                    )
                    Text(
                        text = "Rp ${formatRupiah(state.ppnAmount)}",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Grand Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Tagihan",
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Rp ${formatRupiah(state.grandTotal)}",
                        fontFamily = interfamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRoyalBlue
                    )
                }

                // Tombol BAYAR SEKARANG (Accessible & Responsive)
                Button(
                    onClick = onBayar,
                    enabled = totalItemCount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
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
                            text = "BAYAR SEKARANG",
                            fontFamily = interfamily,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
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
}
