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

import com.ptpws.ikikasir.feature.auth.domain.repository.AuthRepository
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val insertUserUseCase: InsertUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val syncUsersUseCase: SyncUsersUseCase,
    private val logActivityUseCase: LogActivityUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(UserListState())
    val listState: StateFlow<UserListState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(UserFormState())
    val formState: StateFlow<UserFormState> = _formState.asStateFlow()

    init {
        loadUsers()
        syncData()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true) }
            getUsersUseCase().collect { userList ->
                _listState.update { state ->
                    val filtered = filterUsers(userList, state.searchQuery, state.filterRole, state.filterStatus)
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
            val filtered = filterUsers(state.users, query, state.filterRole, state.filterStatus)
            state.copy(
                searchQuery = query,
                filteredUsers = filtered
            )
        }
    }

    fun onFilterRoleChange(role: String) {
        _listState.update { state ->
            val filtered = filterUsers(state.users, state.searchQuery, role, state.filterStatus)
            state.copy(filterRole = role, filteredUsers = filtered)
        }
    }

    fun onFilterStatusChange(status: String) {
        _listState.update { state ->
            val filtered = filterUsers(state.users, state.searchQuery, state.filterRole, status)
            state.copy(filterStatus = status, filteredUsers = filtered)
        }
    }

    fun clearFilters() {
        _listState.update { state ->
            val filtered = filterUsers(state.users, state.searchQuery, "", "")
            state.copy(filterRole = "", filterStatus = "", filteredUsers = filtered)
        }
    }

    private fun filterUsers(
        users: List<User>,
        query: String,
        role: String = "",
        status: String = ""
    ): List<User> {
        return users.filter { user ->
            val matchesQuery = if (query.isBlank()) true else {
                val q = query.trim().lowercase()
                user.fullName.lowercase().contains(q) ||
                        user.email.lowercase().contains(q) ||
                        user.roleId.lowercase().contains(q)
            }
            val matchesRole = if (role.isBlank()) true else
                user.roleId.lowercase() == role.lowercase()
            val matchesStatus = when (status.lowercase()) {
                "aktif" -> user.isActive
                "nonaktif" -> !user.isActive
                else -> true
            }
            matchesQuery && matchesRole && matchesStatus
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
        _formState.update { it.copy(fullName = value, error = null) }
    }

    fun onEmailChange(value: String) {
        _formState.update { it.copy(email = value, error = null) }
    }

    fun onCurrentPasswordChange(value: String) {
        _formState.update { it.copy(currentPassword = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _formState.update { it.copy(password = value, error = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _formState.update { it.copy(confirmPassword = value, error = null) }
    }

    fun onRoleChange(value: String) {
        _formState.update { it.copy(roleId = value, error = null) }
    }

    fun onActiveChange(value: Boolean) {
        _formState.update { it.copy(isActive = value) }
    }

    fun onPhotoUrlChange(value: String) {
        _formState.update { it.copy(photoUrl = value) }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            onResult(false, "Alamat email tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(cleanEmail).collect { result ->
                result.onSuccess {
                    onResult(
                        true,
                        "Tautan reset kata sandi berhasil dikirim ke $cleanEmail. Silakan periksa kotak masuk atau folder spam email tersebut."
                    )
                }.onFailure { err ->
                    val rawMsg = err.message.orEmpty().lowercase()
                    val friendlyMsg = when {
                        rawMsg.contains("blocked all requests") || rawMsg.contains("too-many-requests") || rawMsg.contains(
                            "unusual activity"
                        ) ->
                            "Terlalu banyak permintaan. Silakan tunggu beberapa menit sebelum mencoba mengirim lagi."

                        rawMsg.contains("user-not-found") || rawMsg.contains("no user record") ->
                            "Akun dengan email $cleanEmail belum terdaftar di sistem login."

                        rawMsg.contains("network") || rawMsg.contains("timeout") ->
                            "Koneksi internet bermasalah. Periksa koneksi internet Anda lalu coba lagi."

                        else ->
                            "Gagal mengirim email reset kata sandi. Pastikan email aktif dan coba lagi."
                    }
                    onResult(false, friendlyMsg)
                }
            }
        }
    }

    private var loadUserJob: kotlinx.coroutines.Job? = null

    fun loadUserById(userId: String) {
        if (userId.isBlank()) return
        // Jangan timpa form jika data user ini sudah pernah dimuat dan sedang diedit
        if (_formState.value.id == userId) return

        loadUserJob?.cancel()
        loadUserJob = viewModelScope.launch {
            getUsersUseCase().collect { userList ->
                val existing = userList.find { it.id == userId }
                if (existing != null) {
                    if (_formState.value.id != userId) {
                        _formState.value = UserFormState(
                            id = existing.id,
                            fullName = existing.fullName,
                            email = existing.email,
                            initialEmail = existing.email,
                            currentPassword = "",
                            password = "",
                            confirmPassword = "",
                            roleId = existing.roleId,
                            isActive = existing.isActive,
                            photoUrl = existing.photoUrl
                        )
                    }
                    // Batalkan listener setelah data berhasil dimuat agar background sync tidak me-reset form yang sedang diketik user
                    loadUserJob?.cancel()
                }
            }
        }
    }

    fun resetForm() {
        loadUserJob?.cancel()
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
        if (!current.email.contains("@")) {
            val message = "Format email tidak valid (harus mengandung '@')"
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(error = message) }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(current.email.trim()).matches()) {
            val message = "Format email tidak valid (contoh: nama@domain.com)"
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(error = message) }
            return
        }
        val isEdit = current.id.isNotBlank()
        val isEmailChanged = isEdit && current.initialEmail.isNotBlank() && !current.initialEmail.trim()
            .equals(current.email.trim(), ignoreCase = true)

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
            if (current.confirmPassword.isBlank()) {
                val message = "Konfirmasi kata sandi tidak boleh kosong"
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
        } else {
            // Edit pengguna:
            if (isEmailChanged) {
                // Perubahan email memerlukan sandi saat ini atau sandi baru untuk sinkronisasi Firebase Auth
                if (current.password.isBlank() && current.currentPassword.isBlank()) {
                    val message =
                        "Mengubah email memerlukan kata sandi saat ini atau kata sandi baru untuk sinkronisasi akun login Firebase."
                    GlobalCrudResultDialog.failure(message)
                    _formState.update { it.copy(error = message) }
                    return
                }
            }

            // Validasi kata sandi baru jika diisi
            if (current.password.isNotBlank() || current.confirmPassword.isNotBlank()) {
                if (current.password.isBlank()) {
                    val message = "Kata sandi baru tidak boleh kosong"
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
                if (current.confirmPassword.isBlank()) {
                    val message = "Konfirmasi kata sandi baru tidak boleh kosong"
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
                if (current.currentPassword.isBlank()) {
                    val message =
                        "Masukkan kata sandi saat ini untuk mengubah kata sandi, atau gunakan opsi 'Lupa kata sandi?' di atas."
                    GlobalCrudResultDialog.failure(message)
                    _formState.update { it.copy(error = message) }
                    return
                }
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
                insertUserUseCase(user, current.password)
            } else {
                updateUserUseCase(
                    user = user,
                    plainPassword = current.password.ifBlank { null },
                    currentPassword = current.currentPassword.ifBlank { null },
                    oldEmail = current.initialEmail.ifBlank { null }
                )
            }
            flow.collect { result ->
                result.onSuccess {
                    GlobalCrudResultDialog.success(
                        if (isEdit) "Data pengguna \"${user.fullName}\" berhasil diperbarui."
                        else "Pengguna \"${user.fullName}\" berhasil ditambahkan."
                    )
                    val actionTitle =
                        if (isEdit) "Perubahan Pengguna: ${user.fullName}" else "Pengguna Baru: ${user.fullName}"
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

