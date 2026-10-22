package com.ptpws.ikikasir.core.responsive.presentation.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.ui.unit.Density
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Konfigurasi Skala Tampilan & Font Standar (Display Size & Font Scale Fix).
 *
 * Mengunci ukuran aplikasi agar tampil STANDAR, ringkas, dan proporsional (tidak kebesaran):
 * 1. fontScale = 1.0f (tidak terpengaruh oleh setelan ukuran font sistem HP/tablet).
 * 2. densityDpi disesuaikan ke standar resmi Material Design (target 411.4 dp untuk ponsel, contoh 420 DPI pada 1080p).
 *    Ini menghindari efek 'kebesaran / zoom' yang terjadi jika memakai DENSITY_DEVICE_STABLE OEM yang terlalu tinggi.
 * 3. Menghitung ulang screenWidthDp & screenHeightDp secara proporsional sesuai standar sehingga tidak terpengaruh
 *    oleh setelan ukuran tampilan (kecil, standar, besar, terbesar) di HP/Tablet.
 */
object DisplaySizeConfig {

    const val FIXED_FONT_SCALE: Float = 1.0f

    /**
     * Target lebar standar (dalam Dp) untuk perangkat ponsel.
     * Menggunakan 460 dp (setara setelan 'Kecil / Compact' ~376 DPI pada layar 1080p)
     * sehingga layout aplikasi tampil lebih kecil, ringkas, rapi, dan luas.
     */
    const val TARGET_PHONE_WIDTH_DP: Float = 460f

    /**
     * Target lebar standar untuk tablet kecil (7-8 inci): 680 dp.
     */
    const val TARGET_TABLET_COMPACT_WIDTH_DP: Float = 680f

    /**
     * Target lebar standar untuk tablet besar (10 inci ke atas): 900 dp.
     */
    const val TARGET_TABLET_EXPANDED_WIDTH_DP: Float = 900f

    /**
     * Mendapatkan resolusi fisik layar (widthPx, heightPx) yang telah disinkronkan dengan orientasi saat ini.
     */
    fun getScreenPixels(context: Context): Pair<Int, Int> {
        var widthPx = 0
        var heightPx = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            try {
                val bounds = wm?.maximumWindowMetrics?.bounds ?: wm?.currentWindowMetrics?.bounds
                if (bounds != null && bounds.width() > 0 && bounds.height() > 0) {
                    widthPx = bounds.width()
                    heightPx = bounds.height()
                }
            } catch (_: Exception) {}
        }

        if (widthPx <= 0 || heightPx <= 0) {
            val metrics = context.resources.displayMetrics
            widthPx = metrics.widthPixels
            heightPx = metrics.heightPixels
        }

        val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape && widthPx < heightPx) {
            val temp = widthPx
            widthPx = heightPx
            heightPx = temp
        } else if (!isLandscape && widthPx > heightPx) {
            val temp = widthPx
            widthPx = heightPx
            heightPx = temp
        }

        return Pair(widthPx, heightPx)
    }

    /**
     * Menentukan apakah perangkat saat ini adalah tablet secara akurat.
     */
    fun isTablet(context: Context): Boolean {
        val (widthPx, heightPx) = getScreenPixels(context)
        val shortPx = minOf(widthPx, heightPx)
        val dm = context.resources.displayMetrics

        // 1. Cek diagonal layar fisik (dalam inci) jika xdpi & ydpi valid
        if (dm.xdpi > 30f && dm.ydpi > 30f) {
            val wInches = widthPx.toDouble() / dm.xdpi
            val hInches = heightPx.toDouble() / dm.ydpi
            val diagonalInches = sqrt(wInches * wInches + hInches * hInches)
            if (diagonalInches >= 6.85) {
                return true
            }
        }

        // 2. Cek rasio resolusi fisik terhadap stableDpi perangkat bawaan
        val stableDpi = if (DisplayMetrics.DENSITY_DEVICE_STABLE > 0) {
            DisplayMetrics.DENSITY_DEVICE_STABLE
        } else {
            dm.densityDpi
        }

        if (stableDpi > 0 && shortPx > 0) {
            val rawDp = (shortPx * 160f) / stableDpi
            if (rawDp >= 550f) return true
        }

        return false
    }

    /**
     * Menghitung DPI standar yang proporsional dan tidak kebesaran.
     * Tidak terpengaruh oleh setelan ukuran tampilan sistem (kecil, standar, besar, terbesar).
     */
    fun getStandardDpi(context: Context): Int {
        val (widthPx, heightPx) = getScreenPixels(context)
        val shortPx = minOf(widthPx, heightPx)
        if (shortPx <= 0) {
            return if (DisplayMetrics.DENSITY_DEVICE_STABLE > 0) DisplayMetrics.DENSITY_DEVICE_STABLE else 380
        }

        return if (isTablet(context)) {
            val targetDp = if (shortPx < 1000) TARGET_TABLET_COMPACT_WIDTH_DP else TARGET_TABLET_EXPANDED_WIDTH_DP
            val calculated = ((shortPx / targetDp) * 160f).roundToInt()
            calculated.coerceIn(120, 480)
        } else {
            val calculated = ((shortPx / TARGET_PHONE_WIDTH_DP) * 160f).roundToInt()
            calculated.coerceIn(160, 640)
        }
    }

    /** Alias untuk kompatibilitas */
    fun getStableDpi(context: Context): Int = getStandardDpi(context)

    /** Mendapatkan skala Density float (DPI / 160f) */
    fun getStandardDensity(context: Context): Float = getStandardDpi(context) / 160f

    /** Alias untuk kompatibilitas */
    fun getStableDensity(context: Context): Float = getStandardDensity(context)

    /**
     * Mendapatkan objek Density Jetpack Compose dengan densitas standar dan fontScale 1.0f.
     */
    fun getFixedDensity(context: Context): Density {
        val density = getStandardDensity(context)
        return Density(density = density, fontScale = FIXED_FONT_SCALE)
    }

    /**
     * Menyesuaikan objek Configuration agar menggunakan DPI standar dan fontScale normal 1.0f,
     * serta memperbarui nilai screenWidthDp, screenHeightDp, dan smallestScreenWidthDp.
     */
    fun applyFixedConfiguration(config: Configuration, context: Context) {
        val stdDpi = getStandardDpi(context)
        val stdDensity = stdDpi / 160f

        config.densityDpi = stdDpi
        config.fontScale = FIXED_FONT_SCALE
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO

        val (widthPx, heightPx) = getScreenPixels(context)
        if (widthPx > 0 && heightPx > 0 && stdDensity > 0f) {
            config.screenWidthDp = (widthPx / stdDensity).toInt()
            config.screenHeightDp = (heightPx / stdDensity).toInt()
            config.smallestScreenWidthDp = minOf(config.screenWidthDp, config.screenHeightDp)
        }
    }

    /**
     * Menghasilkan Configuration baru yang telah disesuaikan dengan nilai standar.
     */
    fun getFixedConfiguration(context: Context): Configuration {
        val config = Configuration(context.resources.configuration)
        applyFixedConfiguration(config, context)
        return config
    }

    /**
     * Menghasilkan Context baru yang telah dibungkus dengan konfigurasi tampilan dan font tetap.
     */
    fun getFixedContext(base: Context): Context {
        val config = Configuration(base.resources.configuration)
        applyFixedConfiguration(config, base)

        val fixedContext = base.createConfigurationContext(config)

        val metrics = fixedContext.resources.displayMetrics
        val stdDpi = getStandardDpi(base)
        val stdDensity = stdDpi / 160f
        metrics.densityDpi = stdDpi
        metrics.density = stdDensity
        @Suppress("DEPRECATION")
        metrics.scaledDensity = stdDensity

        return fixedContext
    }

    /**
     * Menyesuaikan Resources aplikasi secara langsung (untuk Application/BaseApplication).
     */
    fun applyToResources(resources: Resources) {
        val metrics = resources.displayMetrics
        val widthPx = metrics.widthPixels
        val heightPx = metrics.heightPixels
        val shortPx = minOf(widthPx, heightPx)

        val stdDpi = if (shortPx > 0) {
            val stableDpi = if (DisplayMetrics.DENSITY_DEVICE_STABLE > 0) DisplayMetrics.DENSITY_DEVICE_STABLE else metrics.densityDpi
            val isTablet = (shortPx * 160f / stableDpi) >= 550f
            if (isTablet) {
                val targetTablet = if (shortPx < 1000) TARGET_TABLET_COMPACT_WIDTH_DP else TARGET_TABLET_EXPANDED_WIDTH_DP
                ((shortPx / targetTablet) * 160f).roundToInt().coerceIn(120, 480)
            } else {
                ((shortPx / TARGET_PHONE_WIDTH_DP) * 160f).roundToInt().coerceIn(160, 640)
            }
        } else {
            if (DisplayMetrics.DENSITY_DEVICE_STABLE > 0) DisplayMetrics.DENSITY_DEVICE_STABLE else 380
        }

        val stdDensity = stdDpi / 160f
        val config = resources.configuration
        config.densityDpi = stdDpi
        config.fontScale = FIXED_FONT_SCALE
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO

        metrics.densityDpi = stdDpi
        metrics.density = stdDensity
        @Suppress("DEPRECATION")
        metrics.scaledDensity = stdDensity

        if (widthPx > 0 && heightPx > 0 && stdDensity > 0f) {
            val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
            val wPx = if (isLandscape && widthPx < heightPx) heightPx else if (!isLandscape && widthPx > heightPx) heightPx else widthPx
            val hPx = if (isLandscape && widthPx < heightPx) widthPx else if (!isLandscape && widthPx > heightPx) widthPx else heightPx

            config.screenWidthDp = (wPx / stdDensity).toInt()
            config.screenHeightDp = (hPx / stdDensity).toInt()
            config.smallestScreenWidthDp = minOf(config.screenWidthDp, config.screenHeightDp)
        }

        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, metrics)
    }
}
