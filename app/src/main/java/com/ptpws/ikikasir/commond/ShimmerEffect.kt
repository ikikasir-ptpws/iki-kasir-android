package com.ptpws.ikikasir.commond

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Modifier efek Shimmer beranimasi halus untuk loading skeleton di seluruh aplikasi.
 */
fun Modifier.shimmerEffect(
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF8FAFC),
    durationMillis: Int = 1100
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat().coerceAtLeast(600f),
        targetValue = 2 * size.width.toFloat().coerceAtLeast(600f),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                baseColor,
                highlightColor,
                baseColor
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(
                startOffsetX + size.width.toFloat().coerceAtLeast(600f),
                size.height.toFloat().coerceAtLeast(300f)
            )
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

/**
 * Box sederhana beranimasi Shimmer
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF8FAFC)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmerEffect(baseColor = baseColor, highlightColor = highlightColor)
    )
}

/**
 * Skeleton Shimmer untuk kartu KPI ringkasan
 */
@Composable
fun LaporanKpiCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(modifier = Modifier.size(width = 75.dp, height = 12.dp))
                ShimmerBox(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.size(width = 95.dp, height = 18.dp))
        }
    }
}

/**
 * Skeleton Shimmer untuk kartu Produk Terjual (Laporan Penjualan)
 */
@Composable
fun ProdukTerjualShimmerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge Skeleton
                ShimmerBox(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Image Skeleton
                ShimmerBox(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Detail Produk Skeleton
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(modifier = Modifier.size(width = 110.dp, height = 14.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 12.dp), shape = RoundedCornerShape(4.dp))
                }

                // Unit & Omzet Skeleton
                Column(horizontalAlignment = Alignment.End) {
                    ShimmerBox(modifier = Modifier.size(width = 65.dp, height = 13.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 14.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar Skeleton
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                shape = RoundedCornerShape(3.dp)
            )
        }
    }
}

/**
 * Skeleton Shimmer untuk Daftar Produk
 */
@Composable
fun ProdukCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 14.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.size(width = 70.dp, height = 12.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 14.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            ShimmerBox(
                modifier = Modifier.size(width = 50.dp, height = 24.dp),
                shape = RoundedCornerShape(6.dp)
            )
        }
    }
}

/**
 * Skeleton Shimmer untuk Riwayat Transaksi
 */
@Composable
fun TransaksiCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 14.dp))
                ShimmerBox(modifier = Modifier.size(width = 65.dp, height = 18.dp), shape = RoundedCornerShape(12.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 12.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 11.dp))
                }
                ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 16.dp))
            }
        }
    }
}

/**
 * Skeleton Shimmer untuk Kategori
 */
@Composable
fun KategoriCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(modifier = Modifier.size(width = 110.dp, height = 14.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 12.dp))
            }
            ShimmerBox(
                modifier = Modifier.size(24.dp),
                shape = CircleShape
            )
        }
    }
}

/**
 * Skeleton Shimmer untuk Antrean / Waiting List
 */
@Composable
fun AntreanCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier.size(48.dp),
                shape = CircleShape
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(modifier = Modifier.size(width = 110.dp, height = 14.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 12.dp))
            }
            ShimmerBox(
                modifier = Modifier.size(width = 70.dp, height = 28.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

/**
 * Skeleton Hero Card Shimmer untuk Laporan Keuangan
 */
@Composable
fun LaporanKeuanganHeroShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 14.dp))
                ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 20.dp), shape = RoundedCornerShape(100.dp))
            }
            ShimmerBox(modifier = Modifier.size(width = 200.dp, height = 36.dp), shape = RoundedCornerShape(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(12.dp))
                ShimmerBox(modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(12.dp))
            }
        }
    }
}

/**
 * Skeleton Metric KPI Card Shimmer untuk Laporan Keuangan
 */
@Composable
fun LaporanKeuanganMetricCardShimmer(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 12.dp))
                ShimmerBox(modifier = Modifier.size(28.dp), shape = CircleShape)
            }
            ShimmerBox(modifier = Modifier.size(width = 110.dp, height = 18.dp))
            ShimmerBox(modifier = Modifier.size(width = 70.dp, height = 14.dp), shape = RoundedCornerShape(4.dp))
        }
    }
}

/**
 * Skeleton Chart Shimmer untuk Laporan Keuangan
 */
@Composable
fun LaporanKeuanganChartShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(modifier = Modifier.size(width = 140.dp, height = 16.dp))
                ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 18.dp), shape = RoundedCornerShape(8.dp))
            }
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(150.dp), shape = RoundedCornerShape(12.dp))
        }
    }
}

/**
 * Skeleton Shimmer untuk Audit Log
 */
@Composable
fun AuditLogCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerBox(modifier = Modifier.size(24.dp), shape = CircleShape)
                    ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 12.dp))
                }
                ShimmerBox(modifier = Modifier.size(width = 65.dp, height = 16.dp), shape = RoundedCornerShape(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.size(width = 160.dp, height = 14.dp))
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp))
        }
    }
}

/**
 * Skeleton Shimmer untuk User Manajemen
 */
@Composable
fun UserCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(modifier = Modifier.size(46.dp), shape = CircleShape)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(modifier = Modifier.size(width = 130.dp, height = 14.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.size(width = 90.dp, height = 12.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 16.dp), shape = RoundedCornerShape(4.dp))
            }
            ShimmerBox(modifier = Modifier.size(20.dp), shape = CircleShape)
        }
    }
}

/**
 * Skeleton Shimmer untuk Role & Izin
 */
@Composable
fun RoleCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 16.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.size(width = 180.dp, height = 12.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 14.dp), shape = RoundedCornerShape(6.dp))
            }
            ShimmerBox(modifier = Modifier.size(24.dp), shape = CircleShape)
        }
    }
}

/**
 * Skeleton Shimmer untuk Manajemen Stok
 */
@Composable
fun StokCardShimmer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 14.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 12.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.size(width = 100.dp, height = 14.dp))
            }
            ShimmerBox(modifier = Modifier.size(width = 60.dp, height = 28.dp), shape = RoundedCornerShape(8.dp))
        }
    }
}

/**
 * Skeleton Shimmer untuk Detail Produk
 */
@Composable
fun DetailProdukShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(20.dp)
        )
        ShimmerBox(modifier = Modifier.size(width = 180.dp, height = 24.dp))
        ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 28.dp), shape = RoundedCornerShape(8.dp))
            ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 28.dp), shape = RoundedCornerShape(8.dp))
        }
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(16.dp))
    }
}

/**
 * Skeleton Shimmer untuk Detail Transaksi
 */
@Composable
fun DetailTransaksiShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(20.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(16.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(16.dp))
    }
}

/**
 * Skeleton Shimmer untuk Header Dashboard
 */
@Composable
fun DashboardHeaderShimmer(
    isSmallScreen: Boolean = false
) {
    val horizontalPadding = if (isSmallScreen) 14.dp else 20.dp
    val verticalPadding = if (isSmallScreen) 14.dp else 20.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShimmerBox(modifier = Modifier.size(width = 110.dp, height = if (isSmallScreen) 12.dp else 14.dp))
            ShimmerBox(modifier = Modifier.size(width = 160.dp, height = if (isSmallScreen) 18.dp else 22.dp))
            ShimmerBox(modifier = Modifier.size(width = 190.dp, height = if (isSmallScreen) 11.dp else 13.dp))
        }
        ShimmerBox(
            modifier = Modifier.size(if (isSmallScreen) 38.dp else 44.dp),
            shape = CircleShape
        )
    }
}

/**
 * Skeleton Shimmer untuk Baris Filter Dashboard
 */
@Composable
fun DashboardFilterRowShimmer(
    isSmallScreen: Boolean = false
) {
    val horizontalPadding = if (isSmallScreen) 12.dp else 20.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val chipWidth = when (index) {
                0 -> 72.dp
                1 -> 80.dp
                2 -> 86.dp
                else -> 92.dp
            }
            ShimmerBox(
                modifier = Modifier.size(width = chipWidth, height = 34.dp),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

/**
 * Skeleton Shimmer untuk Kartu Statistik Utama Dashboard
 */
@Composable
fun DashboardStatCardsShimmer(
    isSmallScreen: Boolean = false
) {
    val sectionHorizontalPadding = if (isSmallScreen) 12.dp else 20.dp
    val cardCornerRadius = if (isSmallScreen) 16.dp else 20.dp
    val cardPadding = if (isSmallScreen) 12.dp else 16.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sectionHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 12.dp)
    ) {
        // Card 1
        Card(
            modifier = Modifier
                .weight(1f)
                .height(if (isSmallScreen) 130.dp else 140.dp),
            shape = RoundedCornerShape(cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(cardPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 11.dp))
                    ShimmerBox(modifier = Modifier.size(if (isSmallScreen) 32.dp else 36.dp), shape = CircleShape)
                }
                ShimmerBox(modifier = Modifier.size(width = 110.dp, height = 22.dp))
                ShimmerBox(modifier = Modifier.size(width = 85.dp, height = 14.dp), shape = RoundedCornerShape(100.dp))
            }
        }

        // Card 2
        Card(
            modifier = Modifier
                .weight(1f)
                .height(if (isSmallScreen) 130.dp else 140.dp),
            shape = RoundedCornerShape(cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(cardPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(modifier = Modifier.size(width = 80.dp, height = 11.dp))
                    ShimmerBox(modifier = Modifier.size(if (isSmallScreen) 32.dp else 36.dp), shape = CircleShape)
                }
                ShimmerBox(modifier = Modifier.size(width = 65.dp, height = 22.dp))
                ShimmerBox(modifier = Modifier.size(width = 85.dp, height = 14.dp), shape = RoundedCornerShape(100.dp))
            }
        }
    }
}

/**
 * Skeleton Shimmer untuk Section Menu Dashboard (Produk / Penjualan)
 */
@Composable
fun DashboardMenuSectionShimmer(
    itemCount: Int = 3,
    isSmallScreen: Boolean = false
) {
    val sectionHorizontalPadding = if (isSmallScreen) 12.dp else 20.dp
    val itemWidth = if (isSmallScreen) 64.dp else 72.dp
    val cardSize = if (isSmallScreen) 56.dp else 64.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sectionHorizontalPadding, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 16.dp)
    ) {
        repeat(itemCount) {
            Column(
                modifier = Modifier.width(itemWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShimmerBox(
                    modifier = Modifier.size(cardSize),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(if (isSmallScreen) 6.dp else 8.dp))
                ShimmerBox(modifier = Modifier.size(width = 48.dp, height = 12.dp))
            }
        }
    }
}

/**
 * Skeleton Shimmer untuk Kartu Ringkasan (Pelanggan & Terjual)
 */
@Composable
fun DashboardRingkasanShimmer(
    isSmallScreen: Boolean = false
) {
    val sectionHorizontalPadding = if (isSmallScreen) 12.dp else 20.dp
    val cardCornerRadius = if (isSmallScreen) 16.dp else 20.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sectionHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 12.dp)
    ) {
        repeat(2) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(if (isSmallScreen) 90.dp else 100.dp),
                shape = RoundedCornerShape(cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isSmallScreen) 12.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShimmerBox(modifier = Modifier.size(if (isSmallScreen) 38.dp else 44.dp), shape = CircleShape)
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        ShimmerBox(modifier = Modifier.size(width = 55.dp, height = 11.dp))
                        ShimmerBox(modifier = Modifier.size(width = 65.dp, height = 18.dp))
                        ShimmerBox(modifier = Modifier.size(width = 75.dp, height = 11.dp))
                    }
                }
            }
        }
    }
}

