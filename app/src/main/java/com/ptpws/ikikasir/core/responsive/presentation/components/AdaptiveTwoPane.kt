package com.ptpws.ikikasir.core.responsive.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Komponen reusable untuk tata letak Split-Screen (Dua Panel Berdampingan).
 * Digunakan pada mode Layar Lebar & POS Kasir:
 * - Panel Pertama (firstPane): Umumnya Katalog Produk, pencarian, dan filter kategori.
 * - Panel Kedua (secondPane): Umumnya Keranjang Belanja, rincian pembayaran, dan tombol bayar.
 *
 * @param modifier Modifier untuk container utama
 * @param firstPaneRatio Fraksi lebar panel pertama (default 0.58f atau 58% dari total lebar)
 * @param dividerWidth Lebar garis pemisah antar panel (default 1.dp)
 * @param dividerColor Warna garis pemisah antar panel
 * @param firstPane Konten panel sisi kiri (Katalog)
 * @param secondPane Konten panel sisi kanan (Keranjang & Pembayaran)
 */
@Composable
fun AdaptiveTwoPane(
    modifier: Modifier = Modifier,
    firstPaneRatio: Float = 0.58f,
    dividerWidth: Dp = 1.dp,
    dividerColor: Color = Color(0xFFE2E8F0),
    firstPane: @Composable BoxScope.() -> Unit,
    secondPane: @Composable BoxScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Panel Sisi Kiri (Katalog Produk)
        Box(
            modifier = Modifier
                .weight(firstPaneRatio)
                .fillMaxHeight()
        ) {
            firstPane()
        }

        // Garis Pemisah Vertikal Halus
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = dividerWidth,
            color = dividerColor
        )

        // Panel Sisi Kanan (Keranjang & Checkout)
        Box(
            modifier = Modifier
                .weight(1f - firstPaneRatio)
                .fillMaxHeight()
        ) {
            secondPane()
        }
    }
}
