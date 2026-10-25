package com.ptpws.ikikasir.feature.sidebar.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PointOfSale
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.feature.sidebar.domain.model.SidebarMenuItem
import com.ptpws.ikikasir.feature.sidebar.domain.model.SidebarSubMenuItem
import com.ptpws.ikikasir.feature.sidebar.domain.repository.SidebarRepository
import com.ptpws.ikikasir.screens.navigation.AppScreen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SidebarRepositoryImpl @Inject constructor() : SidebarRepository {

    override fun getSidebarMenuItems(
        userRole: String,
        menuAccess: Map<String, Boolean>
    ): List<SidebarMenuItem> {
        val isAdmin = userRole.isBlank() ||
                userRole.equals("Admin", ignoreCase = true) ||
                userRole.equals("Super Admin", ignoreCase = true) ||
                userRole.equals("System Administrator", ignoreCase = true)

        fun isPermitted(permission: String?): Boolean {
            if (permission == null) return true
            if (isAdmin) return true
            return menuAccess[permission] == true
        }

        val allMenus = listOf(
            SidebarMenuItem(
                id = "beranda",
                title = "Beranda",
                iconVector = Icons.Outlined.Home,
                route = AppScreen.Dashboard.route,
                permissionName = null
            ),
            SidebarMenuItem(
                id = "produk",
                title = "Produk",
                iconRes = R.drawable.produk,
                iconVector = Icons.Outlined.Inventory2,
                permissionName = null,
                subItems = listOf(
                    SidebarSubMenuItem(
                        id = "produk_daftar",
                        title = "Produk",
                        route = AppScreen.Produk.baseRoute,
                        iconRes = R.drawable.produk,
                        permissionName = "Produk"
                    ),
                    SidebarSubMenuItem(
                        id = "produk_kategori",
                        title = "Kategori Produk",
                        route = AppScreen.KategoriProduk.route,
                        iconRes = R.drawable.kategori,
                        permissionName = "Kategori Produk"
                    ),
                    SidebarSubMenuItem(
                        id = "produk_stok",
                        title = "Manajemen Stok",
                        route = AppScreen.ManajemenStok.route,
                        iconRes = R.drawable.manajemenstok,
                        permissionName = "Manajemen Stok"
                    )
                )
            ),
            SidebarMenuItem(
                id = "penjualan",
                title = "Penjualan",
                iconRes = R.drawable.penjualan,
                iconVector = Icons.Outlined.PointOfSale,
                permissionName = null,
                subItems = listOf(
                    SidebarSubMenuItem(
                        id = "penjualan_kasir",
                        title = "Kasir",
                        route = AppScreen.Kasir.route,
                        iconVector = Icons.Outlined.PointOfSale,
                        permissionName = "Kasir"
                    ),
                    SidebarSubMenuItem(
                        id = "penjualan_transaksi",
                        title = "Transaksi",
                        route = AppScreen.Riwayat.route,
                        iconRes = R.drawable.transaksi,
                        permissionName = "Transaksi"
                    ),
                    SidebarSubMenuItem(
                        id = "penjualan_antrean",
                        title = "Antrean",
                        route = AppScreen.WaitingList.route,
                        iconRes = R.drawable.waitinglist,
                        permissionName = "Antrean"
                    ),
                    SidebarSubMenuItem(
                        id = "penjualan_riwayat_antrean",
                        title = "Riwayat Antrean",
                        route = AppScreen.RiwayatAntrean.route,
                        iconRes = R.drawable.riwayatantrean,
                        permissionName = "Riwayat Antrean"
                    ),
                    SidebarSubMenuItem(
                        id = "penjualan_promo",
                        title = "Promo",
                        route = AppScreen.Promo.route,
                        iconRes = R.drawable.promo,
                        permissionName = "Promo"
                    ),
                    SidebarSubMenuItem(
                        id = "penjualan_laporan",
                        title = "Laporan Penjualan",
                        route = AppScreen.LaporanPenjualan.baseRoute,
                        iconRes = R.drawable.laporanpenjualan,
                        permissionName = "Laporan Penjualan"
                    )
                )
            ),
            SidebarMenuItem(
                id = "keuangan",
                title = "Keuangan",
                iconRes = R.drawable.laporankeuangan,
                permissionName = null,
                subItems = listOf(
                    SidebarSubMenuItem(
                        id = "keuangan_laporan",
                        title = "Laporan Keuangan",
                        route = AppScreen.LaporanKeuangan.route,
                        iconRes = R.drawable.laporankeuangan,
                        permissionName = "Laporan Keuangan"
                    ),
                    SidebarSubMenuItem(
                        id = "keuangan_catatan",
                        title = "Catatan Keuangan",
                        route = AppScreen.CatatanKeuangan.route,
                        iconRes = R.drawable.catatankeuangan,
                        permissionName = "Catatan Keuangan"
                    )
                )
            ),
            SidebarMenuItem(
                id = "pengguna",
                title = "Pengguna",
                iconRes = R.drawable.pengguna,
                iconVector = Icons.Outlined.Person,
                permissionName = null,
                subItems = listOf(
                    SidebarSubMenuItem(
                        id = "pengguna_manajemen",
                        title = "Pengguna",
                        route = AppScreen.Pengguna.route,
                        iconRes = R.drawable.pengguna,
                        permissionName = "Manajemen Pengguna"
                    ),
                    SidebarSubMenuItem(
                        id = "pengguna_auditlog",
                        title = "Audit Log",
                        route = AppScreen.AuditLog.route,
                        iconRes = R.drawable.auditlog,
                        permissionName = "Audit Log"
                    )
                )
            ),
            SidebarMenuItem(
                id = "profil",
                title = "Profil",
                iconVector = Icons.Outlined.Person,
                route = AppScreen.Profil.route,
                permissionName = null
            )
        )

        return allMenus.mapNotNull { menu ->
            if (menu.isExpandable) {
                val filteredSubItems = menu.subItems.filter { sub -> isPermitted(sub.permissionName) }
                if (filteredSubItems.isNotEmpty()) {
                    menu.copy(subItems = filteredSubItems)
                } else {
                    null
                }
            } else {
                if (isPermitted(menu.permissionName)) menu else null
            }
        }
    }
}
