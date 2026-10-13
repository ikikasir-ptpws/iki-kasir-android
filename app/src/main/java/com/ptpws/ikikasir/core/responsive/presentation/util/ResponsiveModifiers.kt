package com.ptpws.ikikasir.core.responsive.presentation.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ptpws.ikikasir.core.responsive.domain.model.ResponsiveDimensions

/**
 * Kumpulan Modifier Utility untuk memastikan komponen mematuhi
 * prinsip responsif dan aksesibilitas Jetpack Compose di Iki Kasir.
 */

/**
 * Memastikan tombol memiliki batas tinggi minimum (default 48.dp)
 * tanpa mengunci tinggi maksimum, sehingga jika ukuran font sistem Android
 * diperbesar oleh pengguna, teks di dalam tombol tetap membungkus rapi dan tidak terpotong.
 */
fun Modifier.accessibleButtonMinHeight(minHeight: Dp = ResponsiveDimensions.MinTouchTargetSize): Modifier {
    return this.heightIn(min = minHeight)
}

/**
 * Menerapkan padding horizontal adaptif yang menyesuaikan lebar layar yang tersedia.
 */
fun Modifier.adaptiveHorizontalPadding(screenWidth: Dp): Modifier {
    return this.padding(horizontal = ResponsiveDimensions.getAdaptiveHorizontalPadding(screenWidth))
}

/**
 * Memastikan konten mengisi lebar secara responsif dengan batas maksimum lebar
 * agar tidak terlalu renggang ketika dibuka pada tablet landscape atau layar lebar.
 */
fun Modifier.responsiveContentWidth(maxWidth: Dp = 680.dp): Modifier {
    return this
        .fillMaxWidth()
        .widthIn(max = maxWidth)
}
