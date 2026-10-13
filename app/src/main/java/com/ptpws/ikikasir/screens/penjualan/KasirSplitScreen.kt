package com.ptpws.ikikasir.screens.penjualan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.core.responsive.presentation.components.AdaptiveTwoPane
import com.ptpws.ikikasir.feature.penjualan.presentation.state.KasirState
import com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel.KasirViewModel
import com.ptpws.ikikasir.screens.penjualan.component.KasirCartPane
import com.ptpws.ikikasir.screens.penjualan.component.KasirCatalogPane

private val PrimaryRoyalBlue = Color(0xFF3B32D1)

/**
 * Tampilan Layar Kasir Dual-Pane (Split-Screen) untuk Tablet & POS Layar Lebar.
 * Menghubungkan KasirCatalogPane di sisi kiri dan KasirCartPane di sisi kanan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasirSplitScreen(
    state: KasirState,
    viewModel: KasirViewModel,
    navController: NavController,
    onBayar: () -> Unit,
    onScanBarcode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Kasir Pintar (Mode POS)",
                                fontWeight = FontWeight.Bold,
                                fontFamily = interfamily,
                                color = Color(0xFF0F172A),
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Terminal Transaksi & Katalog Produk",
                                fontFamily = interfamily,
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        }
                    }
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
        }
    ) { paddingValues ->
        AdaptiveTwoPane(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            firstPaneRatio = 0.58f,
            firstPane = {
                KasirCatalogPane(
                    state = state,
                    onSearchChange = { viewModel.onCatalogSearchQueryChange(it) },
                    onCategoryChange = { viewModel.onCategoryFilterChange(it) },
                    onIncrement = { viewModel.incrementCartItem(it) },
                    onDecrement = { viewModel.decrementCartItem(it) },
                    onScanBarcode = onScanBarcode
                )
            },
            secondPane = {
                KasirCartPane(
                    state = state,
                    onIncrement = { produkId, qty -> viewModel.updateQuantity(produkId, qty) },
                    onDecrement = { produkId, qty -> viewModel.updateQuantity(produkId, qty) },
                    onRemove = { produkId -> viewModel.removeFromCart(produkId) },
                    onClearCart = { viewModel.clearCart() },
                    onBayar = onBayar
                )
            }
        )
    }
}
