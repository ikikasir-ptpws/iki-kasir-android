package com.ptpws.ikikasir.feature.role.presentation.viewmodel

import com.ptpws.ikikasir.feature.role.presentation.state.RoleListState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk RoleListUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface RoleListUiProvider {
    val listState: StateFlow<RoleListState>
    fun deleteRole(id: String)
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke RoleViewModel asli.
 */
class HiltRoleListUiProvider(private val viewModel: RoleViewModel) : RoleListUiProvider {
    override val listState: StateFlow<RoleListState> get() = viewModel.listState
    override fun deleteRole(id: String) = viewModel.deleteRole(id)
}
