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

    return remember(widthDp, heightDp, isLandscape) {
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
            layoutMode = layoutMode
        )
    }
}

/**
 * Penyedia CompositionLocal untuk WindowSizeInfo.
 * Tempatkan di level tertinggi (misalnya di MainActivity atau root Composable).
 */
@Composable
fun ProvideWindowSizeInfo(
    windowSizeInfo: WindowSizeInfo = rememberWindowSizeInfo(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalWindowSizeInfo provides windowSizeInfo) {
        content()
    }
}
