package com.ptpws.ikikasir.feature.sidebar.presentation.state

import com.ptpws.ikikasir.feature.sidebar.domain.model.SidebarMenuItem

/**
 * State UI untuk komponen navigasi Sidebar pada layar tablet/landscape.
 *
 * @property menuItems Daftar menu navigasi utama yang diizinkan untuk pengguna saat ini.
 * @property expandedGroupIds Kumpulan ID menu yang saat ini dalam kondisi terbuka (expanded).
 * @property currentRoute Rute navigasi aktif saat ini.
 * @property userName Nama lengkap pengguna yang sedang masuk.
 * @property userRole Nama role / level akses pengguna (misalnya "Super Admin", "Kasir").
 * @property userPhotoUrl URL foto profil pengguna (opsional).
 * @property isLoading Menandakan proses pemuatan data sesi/izin pengguna.
 */
data class SidebarUiState(
    val menuItems: List<SidebarMenuItem> = emptyList(),
    val expandedGroupIds: Set<String> = emptySet(),
    val currentRoute: String? = null,
    val userName: String = "Pengguna",
    val userRole: String = "Admin",
    val userPhotoUrl: String? = null,
    val isLoading: Boolean = false
)
