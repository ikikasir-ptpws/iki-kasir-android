package com.ptpws.ikikasir.feature.sidebar.domain.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Model Domain untuk item sub-menu navigasi sidebar.
 *
 * @property id Pengidentifikasi unik sub-menu.
 * @property title Judul atau label teks sub-menu.
 * @property route Rute navigasi Composable yang dituju.
 * @property iconRes ID resource ikon drawable (opsional).
 * @property iconVector Vector icon Compose (opsional).
 * @property permissionName Nama izin/role yang diperlukan untuk mengakses sub-menu ini (null jika publik).
 */
data class SidebarSubMenuItem(
    val id: String,
    val title: String,
    val route: String,
    @DrawableRes val iconRes: Int? = null,
    val iconVector: ImageVector? = null,
    val permissionName: String? = null
)
