package com.ptpws.ikikasir.feature.sidebar.domain.usecase

import com.ptpws.ikikasir.feature.sidebar.domain.model.SidebarMenuItem
import com.ptpws.ikikasir.feature.sidebar.domain.repository.SidebarRepository
import javax.inject.Inject

/**
 * UseCase untuk mendapatkan hierarki menu dan sub-menu sidebar yang telah
 * disesuaikan dengan hak akses dan role pengguna.
 */
class GetSidebarMenuUseCase @Inject constructor(
    private val repository: SidebarRepository
) {
    operator fun invoke(
        userRole: String,
        menuAccess: Map<String, Boolean>
    ): List<SidebarMenuItem> {
        return repository.getSidebarMenuItems(userRole, menuAccess)
    }
}
