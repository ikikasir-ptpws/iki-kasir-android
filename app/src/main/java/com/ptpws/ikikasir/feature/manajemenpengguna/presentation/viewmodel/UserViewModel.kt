package com.ptpws.ikikasir.feature.manajemenpengguna.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
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

import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val insertUserUseCase: InsertUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val syncUsersUseCase: SyncUsersUseCase,
    private val logActivityUseCase: LogActivityUseCase
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
        val targetUser = _listState.value.users.find { it.id == id }
        val userName = targetUser?.fullName ?: id

        viewModelScope.launch {
            deleteUserUseCase(id).collect { result ->
                result.onSuccess {
                    GlobalCrudResultDialog.success("Pengguna \"$userName\" berhasil dihapus.")
                    logActivityUseCase(
                        title = "Penghapusan Pengguna: $userName",
                        description = "Akun pengguna $userName telah dihapus dari sistem.",
                        category = "AUTHENTICATION",
                        action = "DELETE",
                        isWarning = true
                    )
                    _listState.update { it.copy(message = "Pengguna berhasil dihapus") }
                }.onFailure { err ->
                    GlobalCrudResultDialog.failure(err.message ?: "Gagal menghapus pengguna.")
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

    fun onPhotoUrlChange(value: String) {
        _formState.update { it.copy(photoUrl = value) }
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
                        // password tidak disimpan di DB, field dikosongkan saat edit
                        password = "",
                        confirmPassword = "",
                        roleId = existing.roleId,
                        isActive = existing.isActive,
                        photoUrl = existing.photoUrl
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
            val message = "Nama lengkap tidak boleh kosong"
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(error = message) }
            return
        }
        if (current.email.isBlank()) {
            val message = "Alamat email tidak boleh kosong"
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(error = message) }
            return
        }
        val isEdit = current.id.isNotBlank()
        if (!isEdit) {
            // Tambah pengguna baru: password wajib diisi
            if (current.password.isBlank()) {
                val message = "Kata sandi tidak boleh kosong"
                GlobalCrudResultDialog.failure(message)
                _formState.update { it.copy(error = message) }
                return
            }
            if (current.password.length < 6) {
                val message = "Kata sandi minimal 6 karakter"
                GlobalCrudResultDialog.failure(message)
                _formState.update { it.copy(error = message) }
                return
            }
            if (current.password != current.confirmPassword) {
                val message = "Konfirmasi kata sandi tidak cocok"
                GlobalCrudResultDialog.failure(message)
                _formState.update { it.copy(error = message) }
                return
            }
        }
        if (current.roleId.isBlank()) {
            val message = "Pilih role terlebih dahulu"
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(error = message) }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, error = null) }
            val userId = if (current.id.isBlank()) UUID.randomUUID().toString() else current.id
            // User domain model TIDAK mengandung password
            val user = User(
                id = userId,
                fullName = current.fullName.trim(),
                email = current.email.trim(),
                roleId = current.roleId,
                isActive = current.isActive,
                photoUrl = current.photoUrl,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val flow = if (!isEdit) {
                // plainPassword hanya diteruskan ke Firebase Auth, tidak disimpan
                insertUserUseCase(user, current.password)
            } else {
                updateUserUseCase(user)
            }
            flow.collect { result ->
                result.onSuccess {
                    GlobalCrudResultDialog.success(
                        if (isEdit) "Data pengguna \"${user.fullName}\" berhasil diperbarui."
                        else "Pengguna \"${user.fullName}\" berhasil ditambahkan."
                    )
                    val actionTitle = if (isEdit) "Perubahan Pengguna: ${user.fullName}" else "Pengguna Baru: ${user.fullName}"
                    val actionDesc = if (isEdit) {
                        "Data pengguna ${user.fullName} (${user.roleId}) telah diperbarui. Status aktif: ${if (user.isActive) "Aktif" else "Non-Aktif"}."
                    } else {
                        "Pengguna baru ${user.fullName} dengan role ${user.roleId} telah dibuat."
                    }
                    logActivityUseCase(
                        title = actionTitle,
                        description = actionDesc,
                        category = "AUTHENTICATION",
                        action = if (isEdit) "UPDATE" else "CREATE",
                        isWarning = false
                    )
                    _formState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                }.onFailure { err ->
                    val message = err.message ?: "Gagal menyimpan pengguna"
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

