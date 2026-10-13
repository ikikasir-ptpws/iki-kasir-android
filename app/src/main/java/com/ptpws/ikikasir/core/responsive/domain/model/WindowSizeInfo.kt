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
    val layoutMode: ResponsiveLayoutMode,
    val fontScale: Float = 1.0f
) {
    /** Apakah ukuran font sistem HP sedang disetel besar oleh pengguna (> 1.1x) */
    val isFontEnlarged: Boolean
        get() = fontScale > 1.1f

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
     * Hanya aktif pada mode Landscape dengan lebar minimal 720dp.
     * Pada mode portrait (termasuk tablet), tampilan kasir mengikuti mode HP standar.
     */
    val shouldUseSplitScreen: Boolean
        get() = isLandscape && screenWidthDp >= 720

    /**
     * Menentukan apakah navigasi utama harus menggunakan Navigation Rail vertikal.
     * Dinonaktifkan pada tablet portrait agar seluruh orientasi portrait menyerupai tampilan HP.
     */
    val shouldUseNavigationRail: Boolean
        get() = false

    /**
     * Menentukan apakah navigasi utama harus menggunakan Permanent Navigation Drawer (Sidebar).
     * Aktif HANYA pada Tablet Landscape (orientasi landscape dengan lebar >= 600dp).
     * Pada mode portrait (termasuk tablet), navigasi menggunakan Bottom Navigation Bar seperti di HP.
     */
    val shouldUsePermanentDrawer: Boolean
        get() = isLandscape && screenWidthDp >= 600

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

    /**
     * Padding horizontal adaptif yang dihitung untuk ukuran layar saat ini.
     */
    val horizontalPadding: androidx.compose.ui.unit.Dp
        get() = ResponsiveDimensions.getAdaptiveHorizontalPadding(screenWidthDp)


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
            layoutMode = ResponsiveLayoutMode.COMPACT_PHONE,
            fontScale = 1.0f
        )
    }
}
