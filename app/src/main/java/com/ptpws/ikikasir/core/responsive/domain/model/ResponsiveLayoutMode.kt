package com.ptpws.ikikasir.core.responsive.domain.model

/**
 * Representasi mode tata letak antarmuka aplikasi Iki Kasir.
 * Ditentukan secara adaptif berdasarkan lebar layar yang tersedia.
 */
enum class ResponsiveLayoutMode {
    /**
     * Mode Layar Ponsel (< 600 dp).
     * Meliputi HP kecil (320dp), standar (360dp), modern (393dp), dan besar (432dp).
     * Pola: Single Column, Bottom Navigation, Bottom Sheet / Dialog keranjang.
     */
    COMPACT_PHONE,

    /**
     * Mode Layar Tablet Menengah (600 dp - 839 dp).
     * Meliputi Tablet kecil (600dp) dan Tablet standar (800dp) portrait.
     * Pola: Navigation Rail di kiri, Adaptive Grid 3-4 kolom, dialog terpusat.
     */
    MEDIUM_TABLET,

    /**
     * Mode Layar Lebar & POS Kasir (>= 840 dp).
     * Meliputi Tablet besar (1024dp) portrait, Layar POS (1280dp landscape), dan Ekstra Lebar (1440dp landscape).
     * Pola: Split-Screen POS (Katalog di kiri, Keranjang & Pembayaran di kanan), Permanent Navigation Drawer / Rail.
     */
    EXPANDED_POS
}
