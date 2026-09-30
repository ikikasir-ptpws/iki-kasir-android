package com.ptpws.ikikasir.feature.role.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase

@HiltViewModel
class RoleViewModel @Inject constructor(
    private val getRolesUseCase: GetRolesUseCase,
    private val insertRoleUseCase: InsertRoleUseCase,
    private val updateRoleUseCase: UpdateRoleUseCase,
    private val deleteRoleUseCase: DeleteRoleUseCase,
    private val syncRolesUseCase: SyncRolesUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val logActivityUseCase: LogActivityUseCase
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
        val targetRole = _listState.value.roles.find { it.id == id }
        val roleName = targetRole?.name ?: id

        viewModelScope.launch {
            deleteRoleUseCase(id).collect { result ->
                result.onSuccess {
                    GlobalCrudResultDialog.success("Role \"$roleName\" berhasil dihapus.")
                    logActivityUseCase(
                        title = "Penghapusan Role: $roleName",
                        description = "Role & hak akses $roleName telah dihapus dari sistem.",
                        category = "SYSTEM",
                        action = "DELETE",
                        isWarning = true
                    )
                    _listState.update { it.copy(message = "Role berhasil dihapus") }
                }.onFailure { err ->
                    GlobalCrudResultDialog.failure(err.message ?: "Gagal menghapus role.")
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
        if (roleId.isBlank() || _formState.value.id == roleId) return
        viewModelScope.launch {
            val existing = getRolesUseCase()
                .first { roles -> roles.any { it.id == roleId } }
                .first { it.id == roleId }

            _formState.value = RoleFormState(
                id = existing.id,
                name = existing.name,
                description = existing.description,
                createdAt = existing.createdAt,
                menuAccess = existing.menuAccess
            )
        }
    }

    fun setEditingRole(role: Role) {
        _formState.value = RoleFormState(
            id = role.id,
            name = role.name,
            description = role.description,
            createdAt = role.createdAt,
            menuAccess = role.menuAccess
        )
    }

    fun resetForm() {
        _formState.value = RoleFormState()
    }

    fun saveRole(onSuccess: () -> Unit = {}) {
        val current = _formState.value
        if (current.name.isBlank()) {
            val message = "Nama role tidak boleh kosong"
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(error = message) }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, error = null) }
            val roleId = if (current.id.isBlank()) UUID.randomUUID().toString() else current.id
            val finalMenuAccess = current.menuAccess.toMutableMap().apply {
                put("Dashboard", true)
                put("Profil", true)
                remove("Member")
            }
            val role = Role(
                id = roleId,
                name = current.name.trim(),
                description = current.description.trim(),
                menuAccess = finalMenuAccess,
                createdAt = current.createdAt.takeIf { it > 0L } ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val isEdit = current.id.isNotBlank()
            val flow = if (!isEdit) insertRoleUseCase(role) else updateRoleUseCase(role)
            flow.collect { result ->
                result.onSuccess {
                    _formState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                    GlobalCrudResultDialog.success(
                        if (isEdit) "Role \"${role.name}\" berhasil diperbarui."
                        else "Role \"${role.name}\" berhasil ditambahkan."
                    )

                    val actionTitle = if (isEdit) "Perubahan Role: ${role.name}" else "Role Baru: ${role.name}"
                    withContext(NonCancellable) {
                        try {
                            logActivityUseCase(
                                title = actionTitle,
                                description = "Role & hak akses ${role.name} (${role.description}) telah tersimpan.",
                                category = "SYSTEM",
                                action = if (isEdit) "UPDATE" else "CREATE",
                                isWarning = false
                            )
                        } catch (error: Exception) {
                            Log.e("RoleViewModel", "Gagal mencatat aktivitas role", error)
                        }
                    }
                }.onFailure { err ->
                    val message = err.message ?: "Gagal menyimpan role"
                    GlobalCrudResultDialog.failure(message)
                    _formState.update { it.copy(isLoading = false, error = message) }
                }
            }
        }
    }

    fun clearMessage() {
        _listState.update { it.copy(message = null, error = null) }
    }
}
