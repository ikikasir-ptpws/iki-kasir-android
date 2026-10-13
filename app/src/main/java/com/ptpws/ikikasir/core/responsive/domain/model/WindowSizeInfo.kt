package com.ptpws.ikikasir.core.responsive.domain.model

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

/**
 * Model Domain penampung informasi dimensi dan klasifikasi layar perangkat.
 * Mengikuti pedoman Material 3 Window Size Classes.
 */
data class WindowSizeInfo(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass,
    val isLandscape: Boolean,
    val layoutMode: ResponsiveLayoutMode
) {
    /** Apakah perangkat berada di kategori HP (< 600 dp) */
    val isCompactPhone: Boolean
        get() = layoutMode == ResponsiveLayoutMode.COMPACT_PHONE

    /** Apakah perangkat berada di kategori Tablet Menengah (600 - 839 dp) */
    val isMediumTablet: Boolean
        get() = layoutMode == ResponsiveLayoutMode.MEDIUM_TABLET

    /** Apakah perangkat berada di kategori Layar Lebar / POS (>= 840 dp) */
    val isExpandedPos: Boolean
        get() = layoutMode == ResponsiveLayoutMode.EXPANDED_POS

    /**
     * Menentukan apakah layar kasir harus menggunakan tata letak Dual-Pane (Split-Screen).
     * Aktif pada mode Expanded (>= 840dp) atau mode Landscape dengan lebar minimal 720dp.
     */
    val shouldUseSplitScreen: Boolean
        get() = layoutMode == ResponsiveLayoutMode.EXPANDED_POS || (isLandscape && screenWidthDp >= 720)

    /**
     * Menentukan apakah navigasi utama harus menggunakan Navigation Rail vertikal.
     */
    val shouldUseNavigationRail: Boolean
        get() = layoutMode == ResponsiveLayoutMode.MEDIUM_TABLET || (isLandscape && screenWidthDp in 600..1199)

    /**
     * Menentukan apakah navigasi utama harus menggunakan Permanent Navigation Drawer.
     * Aktif pada layar ekstra lebar (>= 1200dp landscape).
     */
    val shouldUsePermanentDrawer: Boolean
        get() = screenWidthDp >= 1200

    /**
     * Rekomendasi jumlah kolom untuk katalog produk kasir.
     */
    val productGridColumns: Int
        get() = when {
            screenWidthDp < 360 -> 2 // HP kecil (320dp)
            screenWidthDp < 600 -> 2 // HP standar/besar
            screenWidthDp < 840 -> 3 // Tablet kecil/sedang portrait
            screenWidthDp < 1200 -> 4 // Tablet besar / POS 1024-1280dp
            else -> 5 // Layar ekstra lebar (1440dp)
        }

    companion object {
        /**
         * Nilai default (fallback untuk HP standar 360x800)
         */
        val Default = WindowSizeInfo(
            screenWidthDp = 360,
            screenHeightDp = 800,
            widthSizeClass = WindowWidthSizeClass.Compact,
            heightSizeClass = WindowHeightSizeClass.Medium,
            isLandscape = false,
            layoutMode = ResponsiveLayoutMode.COMPACT_PHONE
        )
    }
}
