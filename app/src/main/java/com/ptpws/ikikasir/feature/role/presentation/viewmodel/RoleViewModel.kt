package com.ptpws.ikikasir.feature.role.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.usecase.GetUsersUseCase
import com.ptpws.ikikasir.feature.role.domain.model.Role
import com.ptpws.ikikasir.feature.role.domain.usecase.DeleteRoleUseCase
import com.ptpws.ikikasir.feature.role.domain.usecase.GetRolesUseCase
import com.ptpws.ikikasir.feature.role.domain.usecase.InsertRoleUseCase
import com.ptpws.ikikasir.feature.role.domain.usecase.SyncRolesUseCase
import com.ptpws.ikikasir.feature.role.domain.usecase.UpdateRoleUseCase
import com.ptpws.ikikasir.feature.role.presentation.state.RoleFormState
import com.ptpws.ikikasir.feature.role.presentation.state.RoleListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RoleViewModel @Inject constructor(
    private val getRolesUseCase: GetRolesUseCase,
    private val insertRoleUseCase: InsertRoleUseCase,
    private val updateRoleUseCase: UpdateRoleUseCase,
    private val deleteRoleUseCase: DeleteRoleUseCase,
    private val syncRolesUseCase: SyncRolesUseCase,
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private val _listState = MutableStateFlow(RoleListState())
    val listState: StateFlow<RoleListState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(RoleFormState())
    val formState: StateFlow<RoleFormState> = _formState.asStateFlow()

    init {
        loadRoles()
    }

    fun loadRoles() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true) }
            combine(getRolesUseCase(), getUsersUseCase()) { roleList, userList ->
                roleList.map { role ->
                    val count = userList.count { user ->
                        user.roleId.equals(role.name, ignoreCase = true) ||
                        user.roleId.equals(role.id, ignoreCase = true)
                    }
                    role.copy(userCount = count)
                }
            }.collect { updatedRoles ->
                _listState.update {
                    it.copy(
                        isLoading = false,
                        roles = updatedRoles
                    )
                }
            }
        }
    }

    fun deleteRole(id: String) {
        viewModelScope.launch {
            deleteRoleUseCase(id).collect { result ->
                result.onSuccess {
                    _listState.update { it.copy(message = "Role berhasil dihapus") }
                }.onFailure { err ->
                    _listState.update { it.copy(error = err.message) }
                }
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _listState.update { it.copy(isSyncing = true) }
            syncRolesUseCase().collect { result ->
                result.onSuccess {
                    _listState.update { it.copy(isSyncing = false, message = "Sinkronisasi berhasil") }
                }.onFailure { err ->
                    _listState.update { it.copy(isSyncing = false, error = err.message ?: "Gagal sinkronisasi") }
                }
            }
        }
    }

    // ── Form & Permissions Actions ──

    fun onNameChange(value: String) {
        _formState.update { it.copy(name = value) }
    }

    fun onDescriptionChange(value: String) {
        _formState.update { it.copy(description = value) }
    }

    fun toggleMenuAccess(menuKey: String, isEnabled: Boolean) {
        _formState.update { state ->
            val updatedMap = state.menuAccess.toMutableMap()
            updatedMap[menuKey] = isEnabled
            state.copy(menuAccess = updatedMap)
        }
    }

    fun loadRoleById(roleId: String) {
        if (roleId.isBlank()) return
        viewModelScope.launch {
            getRolesUseCase().collect { roleList ->
                val existing = roleList.find { it.id == roleId }
                if (existing != null) {
                    _formState.value = RoleFormState(
                        id = existing.id,
                        name = existing.name,
                        description = existing.description,
                        menuAccess = existing.menuAccess
                    )
                }
            }
        }
    }

    fun setEditingRole(role: Role) {
        _formState.value = RoleFormState(
            id = role.id,
            name = role.name,
            description = role.description,
            menuAccess = role.menuAccess
        )
    }

    fun resetForm() {
        _formState.value = RoleFormState()
    }

    fun saveRole(onSuccess: () -> Unit = {}) {
        val current = _formState.value
        if (current.name.isBlank()) {
            _formState.update { it.copy(error = "Nama role tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, error = null) }
            val roleId = if (current.id.isBlank()) UUID.randomUUID().toString() else current.id
            val role = Role(
                id = roleId,
                name = current.name.trim(),
                description = current.description.trim(),
                menuAccess = current.menuAccess,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val flow = if (current.id.isBlank()) insertRoleUseCase(role) else updateRoleUseCase(role)
            flow.collect { result ->
                result.onSuccess {
                    _formState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                }.onFailure { err ->
                    _formState.update { it.copy(isLoading = false, error = err.message ?: "Gagal menyimpan role") }
                }
            }
        }
    }

    fun clearMessage() {
        _listState.update { it.copy(message = null, error = null) }
    }
}
