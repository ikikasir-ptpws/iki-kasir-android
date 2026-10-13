package com.ptpws.ikikasir.feature.sidebar.domain.repository

import com.ptpws.ikikasir.feature.sidebar.domain.model.SidebarMenuItem

/**
 * Interface Repository untuk menyediakan struktur menu sidebar dan
 * menyaring hak akses pengguna berdasarkan role dan izin menu.
 */
interface SidebarRepository {
    /**
     * Mengambil daftar menu sidebar yang telah disaring berdasarkan hak akses pengguna saat ini.
     *
     * @param userRole Nama role pengguna aktif (misalnya "Admin", "Kasir").
     * @param menuAccess Map hak akses izin per fitur menu.
     * @return Daftar [SidebarMenuItem] yang diizinkan untuk diakses.
     */
    fun getSidebarMenuItems(
        userRole: String,
        menuAccess: Map<String, Boolean>
    ): List<SidebarMenuItem>
}
