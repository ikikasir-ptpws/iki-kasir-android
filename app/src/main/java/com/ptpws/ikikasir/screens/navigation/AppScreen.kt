package com.ptpws.ikikasir.screens.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
        "produk?categoryId={categoryId}"
    ) {
        val baseRoute = "produk"
        fun routeWith(categoryId: String?) =
            if (categoryId != null) "produk?categoryId=$categoryId" else "produk"
        val navArguments = listOf(
            navArgument("categoryId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    }

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

    object UpdateStok : AppScreen(
        R.string.screen_manajemen_stok,
        R.drawable.logoikikasir,
        "update_stok?produkId={produkId}"
    ) {
        val baseRoute = "update_stok"
        fun routeWith(produkId: String? = null) = if (produkId != null) "update_stok?produkId=$produkId" else "update_stok"
        val navArguments = listOf(
            navArgument("produkId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    }

    object Transaksi : AppScreen(
        R.string.screen_transaksi,
        R.drawable.iconapk,
        "transaksi"
    )

    object DetailTransaksi : AppScreen(
        R.string.screen_transaksi,
        R.drawable.iconapk,
        "detailtransaksi"
    )

    object LaporanKeuangan : AppScreen(
        R.string.screen_laporan_keuangan,
        R.drawable.iconapk,
        "laporan_keuangan"
    )

    object AuditLog : AppScreen(
        R.string.screen_auditlog,
        R.drawable.logoikikasir,
        "audit_log"
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

    object DetailProduk : AppScreen(
        R.string.screen_produk,
        R.drawable.iconapk,
        "detail_produk/{produkId}"
    ) {
        fun routeWith(produkId: String) = "detail_produk/$produkId"
    }

    object Pembayaran : AppScreen(
        R.string.screen_kasir,
        R.drawable.iconapk,
        "pembayaran"
    )
}