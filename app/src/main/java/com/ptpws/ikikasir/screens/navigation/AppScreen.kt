package com.ptpws.ikikasir.screens.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ptpws.ikikasir.R

sealed class AppScreen(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val route: String
) {

    object Splash : AppScreen(
        R.string.screen_splash,
        R.drawable.logoikikasir,
        "splash"
    )

    object Login : AppScreen(
        R.string.screen_login,
        R.drawable.iconapk,
        "login"
    )

    object Dashboard : AppScreen(
        R.string.screen_dashboard,
        R.drawable.iconapk,
        "dashboard"
    )

    object Semuamenu : AppScreen(
        R.string.screen_dashboard,
        R.drawable.iconapk,
        "semuamenu"
    )

    object Kasir : AppScreen(
        R.string.screen_kasir,
        R.drawable.iconapk,
        "kasir"
    )

    object Produk : AppScreen(
        R.string.screen_produk,
        R.drawable.iconapk,
        "produk"
    )

    object Riwayat : AppScreen(
        R.string.screen_riwayat,
        R.drawable.iconapk,
        "riwayat"
    )

    object Profil : AppScreen(
        R.string.screen_profil,
        R.drawable.iconapk,
        "profil"
    )

    object CariMenu : AppScreen(
        R.string.screen_cari_menu,
        R.drawable.iconapk,
        "cari_menu"
    )

    object KategoriProduk : AppScreen(
        R.string.screen_kategori_produk,
        R.drawable.iconapk,
        "kategori_produk"
    )

    object ManajemenStok : AppScreen(
        R.string.screen_manajemen_stok,
        R.drawable.logoikikasir,
        "manajemen_stok"
    )

    object BarangRusakExp : AppScreen(
        R.string.screen_barang_rusak,
        R.drawable.logoikikasir,
        "barang_rusak_exp"
    )

    object Transaksi : AppScreen(
        R.string.screen_transaksi,
        R.drawable.iconapk,
        "transaksi"
    )

    object LaporanKeuangan : AppScreen(
        R.string.screen_laporan_keuangan,
        R.drawable.iconapk,
        "laporan_keuangan"
    )

    object Hutang : AppScreen(
        R.string.screen_hutang,
        R.drawable.logoikikasir,
        "hutang"
    )

    object AuditLog : AppScreen(
        R.string.screen_auditlog,
        R.drawable.logoikikasir,
        "audit_log"
    )

    object LaporanPenjualan : AppScreen(
        R.string.screen_laporan_penjualan,
        R.drawable.iconapk,
        "laporan_penjualan"
    )

    object Pengguna : AppScreen(
        R.string.screen_pengguna,
        R.drawable.logoikikasir,
        "pengguna"
    )

    object PengaturanMenu : AppScreen(
        R.string.screen_pengaturan,
        R.drawable.logoikikasir,
        "pengaturan_menu"
    )
}