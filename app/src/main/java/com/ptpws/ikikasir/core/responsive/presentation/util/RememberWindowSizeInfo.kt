package com.ptpws.ikikasir.core.responsive.presentation.util

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import com.ptpws.ikikasir.core.responsive.domain.model.ResponsiveLayoutMode
import com.ptpws.ikikasir.core.responsive.domain.model.WindowSizeInfo

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * CompositionLocal untuk mengakses informasi ukuran dan mode responsif layar
 * di seluruh hierarki Composable aplikasi Iki Kasir.
 */
val LocalWindowSizeInfo = staticCompositionLocalOf { WindowSizeInfo.Default }

/**
 * Menghitung dan mengingat WindowSizeInfo saat ini secara adaptif.
 * Bereaksi secara otomatis terhadap rotasi layar dan dapat digunakan
 * baik pada perangkat fisik maupun di dalam Android Studio @Preview.
 */
@Composable
fun rememberWindowSizeInfo(): WindowSizeInfo {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val fontScale = configuration.fontScale

    return remember(widthDp, heightDp, isLandscape, fontScale) {
        val widthClass = when {
            widthDp < 600 -> WindowWidthSizeClass.Compact
            widthDp < 840 -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }

        val heightClass = when {
            heightDp < 480 -> WindowHeightSizeClass.Compact
            heightDp < 900 -> WindowHeightSizeClass.Medium
            else -> WindowHeightSizeClass.Expanded
        }

        val layoutMode = when {
            widthDp < 600 -> ResponsiveLayoutMode.COMPACT_PHONE
            widthDp < 840 -> ResponsiveLayoutMode.MEDIUM_TABLET
            else -> ResponsiveLayoutMode.EXPANDED_POS
        }

        WindowSizeInfo(
            screenWidthDp = widthDp,
            screenHeightDp = heightDp,
            widthSizeClass = widthClass,
            heightSizeClass = heightClass,
            isLandscape = isLandscape,
            layoutMode = layoutMode,
            fontScale = fontScale
        )
    }
}

/**
 * Penyedia CompositionLocal untuk WindowSizeInfo dan Density Tetap (Normal 1.0f).
 * Tempatkan di level tertinggi (misalnya di MainActivity atau root Composable).
 *
 * Mengunci ukuran font pada skala normal (1.0f) sehingga tata letak, tombol, kartu produk,
 * keranjang, dan dialog aplikasi kasir selalu tampil stabil dan rapi, tanpa terpengaruh
 * oleh setelan ukuran font HP (baik pengguna menyetel font kecil maupun font besar di HP).
 */
@Composable
fun ProvideWindowSizeInfo(
    windowSizeInfo: WindowSizeInfo = rememberWindowSizeInfo(),
    lockFontScale: Boolean = true,
    content: @Composable () -> Unit
) {
    val currentDensity = LocalDensity.current

    // Kunci fontScale pada 1.0f agar ukuran teks selalu normal dan stabil
    val targetDensity = remember(currentDensity.density, lockFontScale) {
        Density(
            density = currentDensity.density,
            fontScale = if (lockFontScale) 1.0f else currentDensity.fontScale
        )
    }

    val updatedWindowSizeInfo = remember(windowSizeInfo, lockFontScale) {
        windowSizeInfo.copy(fontScale = if (lockFontScale) 1.0f else windowSizeInfo.fontScale)
    }

    CompositionLocalProvider(
        LocalWindowSizeInfo provides updatedWindowSizeInfo,
        LocalDensity provides targetDensity
    ) {
        content()
    }
}
