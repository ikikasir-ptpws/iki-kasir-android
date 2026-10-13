package com.ptpws.ikikasir.feature.sidebar.domain.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Model Domain untuk menu utama navigasi sidebar (parent menu).
 *
 * @property id Pengidentifikasi unik menu.
 * @property title Judul atau nama grup menu.
 * @property iconRes ID resource ikon drawable (opsional).
 * @property iconVector Vector icon Compose (opsional).
 * @property route Rute navigasi langsung jika bukan grup accordion (null jika menu memiliki sub-items).
 * @property permissionName Nama izin/role yang diperlukan (null jika publik).
 * @property subItems Daftar sub-menu di bawah menu utama ini.
 */
data class SidebarMenuItem(
    val id: String,
    val title: String,
    @DrawableRes val iconRes: Int? = null,
    val iconVector: ImageVector? = null,
    val route: String? = null,
    val permissionName: String? = null,
    val subItems: List<SidebarSubMenuItem> = emptyList()
) {
    /** Menandakan apakah menu ini berupa accordion dropdown yang dapat di-expand/collapse */
    val isExpandable: Boolean
        get() = subItems.isNotEmpty()

    /** Memeriksa apakah rute saat ini cocok dengan salah satu sub-menu di dalamnya */
    fun containsRoute(currentRoute: String?): Boolean {
        if (currentRoute == null) return false
        if (route != null && (currentRoute == route || currentRoute.startsWith(route))) return true
        return subItems.any { sub ->
            currentRoute == sub.route || currentRoute.startsWith(sub.route.substringBefore("?"))
        }
    }
}
