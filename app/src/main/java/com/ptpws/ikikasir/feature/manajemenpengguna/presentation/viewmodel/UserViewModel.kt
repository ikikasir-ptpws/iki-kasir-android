package com.ptpws.ikikasir.feature.manajemenpengguna.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.usecase.DeleteUserUseCase
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.usecase.GetUsersUseCase
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.usecase.InsertUserUseCase
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.usecase.SyncUsersUseCase
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.usecase.UpdateUserUseCase
import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.state.UserFormState
import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.state.UserListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val insertUserUseCase: InsertUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val syncUsersUseCase: SyncUsersUseCase
) : ViewModel() {

    private val _listState = MutableStateFlow(UserListState())
    val listState: StateFlow<UserListState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(UserFormState())
    val formState: StateFlow<UserFormState> = _formState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true) }
            getUsersUseCase().collect { userList ->
                _listState.update { state ->
                    val query = state.searchQuery
                    val filtered = filterUsers(userList, query)
                    state.copy(
                        isLoading = false,
                        users = userList,
                        filteredUsers = filtered
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _listState.update { state ->
            val filtered = filterUsers(state.users, query)
            state.copy(
                searchQuery = query,
                filteredUsers = filtered
            )
        }
    }

    private fun filterUsers(users: List<User>, query: String): List<User> {
        if (query.isBlank()) return users
        val q = query.trim().lowercase()
        return users.filter {
            it.fullName.lowercase().contains(q) ||
            it.email.lowercase().contains(q) ||
            it.roleId.lowercase().contains(q)
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            deleteUserUseCase(id).collect { result ->
                result.onSuccess {
                    _listState.update { it.copy(message = "Pengguna berhasil dihapus") }
                }.onFailure { err ->
                    _listState.update { it.copy(error = err.message) }
                }
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _listState.update { it.copy(isSyncing = true) }
            syncUsersUseCase().collect { result ->
                result.onSuccess {
                    _listState.update { it.copy(isSyncing = false, message = "Sinkronisasi berhasil") }
                }.onFailure { err ->
                    _listState.update { it.copy(isSyncing = false, error = err.message ?: "Gagal sinkronisasi") }
                }
            }
        }
    }

    // ── Form Actions ──

    fun onFullNameChange(value: String) {
        _formState.update { it.copy(fullName = value) }
    }

    fun onEmailChange(value: String) {
        _formState.update { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        _formState.update { it.copy(password = value) }
    }

    fun onRoleChange(value: String) {
        _formState.update { it.copy(roleId = value) }
    }

    fun onActiveChange(value: Boolean) {
        _formState.update { it.copy(isActive = value) }
    }

    fun loadUserById(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            getUsersUseCase().collect { userList ->
                val existing = userList.find { it.id == userId }
                if (existing != null) {
                    _formState.value = UserFormState(
                        id = existing.id,
                        fullName = existing.fullName,
                        email = existing.email,
                        password = existing.password,
                        roleId = existing.roleId,
                        isActive = existing.isActive
                    )
                }
            }
        }
    }

    fun resetForm() {
        _formState.value = UserFormState()
    }

    fun saveUser(onSuccess: () -> Unit = {}) {
        val current = _formState.value
        if (current.fullName.isBlank()) {
            _formState.update { it.copy(error = "Nama lengkap tidak boleh kosong") }
            return
        }
        if (current.email.isBlank()) {
            _formState.update { it.copy(error = "Alamat email tidak boleh kosong") }
            return
        }
        if (current.roleId.isBlank()) {
            _formState.update { it.copy(error = "Pilih role terlebih dahulu") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, error = null) }
            val userId = if (current.id.isBlank()) UUID.randomUUID().toString() else current.id
            val user = User(
                id = userId,
                fullName = current.fullName.trim(),
                email = current.email.trim(),
                password = current.password,
                roleId = current.roleId,
                isActive = current.isActive,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val flow = if (current.id.isBlank()) insertUserUseCase(user) else updateUserUseCase(user)
            flow.collect { result ->
                result.onSuccess {
                    _formState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                }.onFailure { err ->
                    _formState.update { it.copy(isLoading = false, error = err.message ?: "Gagal menyimpan pengguna") }
                }
            }
        }
    }

    fun clearMessage() {
        _listState.update { it.copy(message = null, error = null) }
    }
}
