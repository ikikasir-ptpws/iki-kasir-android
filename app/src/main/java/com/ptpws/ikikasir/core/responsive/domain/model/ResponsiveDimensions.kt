package com.ptpws.ikikasir.core.responsive.domain.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Domain Model / Token Dimensi Responsif untuk Iki Kasir.
 * Menstandarisasi breakpoint ukuran layar, batas padding adaptif,
 * serta ukuran minimum touch target untuk aksesibilitas.
 */
object ResponsiveDimensions {
    /** Batas bawah HP sangat kecil (seperti Galaxy Z Fold cover screen atau HP lawas) */
    val SmallPhoneBreakpoint: Dp = 360.dp

    /** Batas bawah tablet kecil / mode dual-pane */
    val TabletBreakpoint: Dp = 600.dp

    /** Batas bawah tablet besar / POS lebar */
    val ExpandedBreakpoint: Dp = 840.dp

    /** Ukuran minimum area sentuh sesuai Material Design & Android Accessibility Guidelines */
    val MinTouchTargetSize: Dp = 48.dp

    /** Padding horizontal standar untuk HP sempit (< 360dp) */
    val PaddingSmallPhone: Dp = 12.dp

    /** Padding horizontal standar untuk HP umum (360dp - 599dp) */
    val PaddingStandardPhone: Dp = 16.dp

    /** Padding horizontal standar untuk Tablet / POS (>= 600dp) */
    val PaddingTablet: Dp = 24.dp

    /**
     * Menghitung padding horizontal adaptif berdasarkan lebar layar (dp).
     */
    fun getAdaptiveHorizontalPadding(screenWidthDp: Int): Dp {
        return when {
            screenWidthDp < 360 -> PaddingSmallPhone
            screenWidthDp < 600 -> PaddingStandardPhone
            else -> PaddingTablet
        }
    }

    /**
     * Menghitung padding horizontal adaptif berdasarkan Dp.
     */
    fun getAdaptiveHorizontalPadding(screenWidth: Dp): Dp {
        return when {
            screenWidth < SmallPhoneBreakpoint -> PaddingSmallPhone
            screenWidth < TabletBreakpoint -> PaddingStandardPhone
            else -> PaddingTablet
        }
    }
}
