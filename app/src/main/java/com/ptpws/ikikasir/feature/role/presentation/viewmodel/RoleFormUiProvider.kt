package com.ptpws.ikikasir.feature.role.presentation.viewmodel

import com.ptpws.ikikasir.feature.role.presentation.state.RoleFormState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk RoleFormUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface RoleFormUiProvider {
    val formState: StateFlow<RoleFormState>
    fun loadRoleById(roleId: String)
    fun onNameChange(value: String)
    fun onDescriptionChange(value: String)
    fun toggleMenuAccess(menuKey: String, isEnabled: Boolean)
    fun saveRole(onSuccess: () -> Unit = {})
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke RoleViewModel asli.
 */
class HiltRoleFormUiProvider(private val viewModel: RoleViewModel) : RoleFormUiProvider {
    override val formState: StateFlow<RoleFormState> get() = viewModel.formState
    override fun loadRoleById(roleId: String) = viewModel.loadRoleById(roleId)
    override fun onNameChange(value: String) = viewModel.onNameChange(value)
    override fun onDescriptionChange(value: String) = viewModel.onDescriptionChange(value)
    override fun toggleMenuAccess(menuKey: String, isEnabled: Boolean) = viewModel.toggleMenuAccess(menuKey, isEnabled)
    override fun saveRole(onSuccess: () -> Unit) = viewModel.saveRole(onSuccess)
}
