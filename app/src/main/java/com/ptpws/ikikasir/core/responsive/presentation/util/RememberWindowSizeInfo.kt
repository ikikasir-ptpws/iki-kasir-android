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
 * Penyedia CompositionLocal untuk WindowSizeInfo, Density Tetap (Normal), dan Configuration Tetap.
 * Tempatkan di level tertinggi (misalnya di MainActivity atau root Composable).
 *
 * Mengunci ukuran font pada skala normal (1.0f) dan ukuran tampilan (display size / density)
 * pada standar bawaan pabrik perangkat sehingga seluruh tata letak, tombol, kartu produk,
 * keranjang, dan dialog aplikasi kasir selalu tampil normal, rapi, dan proporsional,
 * tanpa terpengaruh oleh setelan ukuran tampilan atau font di HP/Tablet (kecil, standar, besar, dsb).
 */
@Composable
fun ProvideWindowSizeInfo(
    windowSizeInfo: WindowSizeInfo? = null,
    lockFontScale: Boolean = true,
    lockDisplaySize: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentDensity = LocalDensity.current
    val currentConfig = LocalConfiguration.current

    // Kunci density pada standar pabrik (DisplayMetrics.DENSITY_DEVICE_STABLE) dan fontScale pada 1.0f
    val targetDensity = remember(context, currentDensity.density, lockFontScale, lockDisplaySize) {
        val density = if (lockDisplaySize) {
            DisplaySizeConfig.getStableDensity(context)
        } else {
            currentDensity.density
        }
        val fontScale = if (lockFontScale) DisplaySizeConfig.FIXED_FONT_SCALE else currentDensity.fontScale
        Density(density = density, fontScale = fontScale)
    }

    // Kunci Configuration (screenWidthDp, screenHeightDp, densityDpi) agar normal dan stabil
    val targetConfig = remember(context, currentConfig, lockDisplaySize, lockFontScale) {
        val cfg = Configuration(currentConfig)
        if (lockDisplaySize || lockFontScale) {
            DisplaySizeConfig.applyFixedConfiguration(cfg, context)
        }
        cfg
    }

    val calculatedWindowInfo = remember(targetConfig, windowSizeInfo, lockFontScale) {
        if (windowSizeInfo != null) {
            windowSizeInfo.copy(fontScale = if (lockFontScale) DisplaySizeConfig.FIXED_FONT_SCALE else windowSizeInfo.fontScale)
        } else {
            val widthDp = targetConfig.screenWidthDp
            val heightDp = targetConfig.screenHeightDp
            val isLandscape = targetConfig.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                fontScale = if (lockFontScale) DisplaySizeConfig.FIXED_FONT_SCALE else targetConfig.fontScale
            )
        }
    }

    CompositionLocalProvider(
        LocalWindowSizeInfo provides calculatedWindowInfo,
        LocalDensity provides targetDensity,
        LocalConfiguration provides targetConfig
    ) {
        content()
    }
}
