package com.example.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import com.ptpws.ikikasir.feature.role.data.local.dao.RoleDao
import com.ptpws.ikikasir.feature.role.domain.model.Role
import com.ptpws.ikikasir.feature.role.domain.usecase.GetRolesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.datasource.UserRemoteDataSource
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto.toDto
import kotlinx.coroutines.tasks.await

private val DEFAULT_UNASSIGNED_ROLE_ACCESS = listOf(
    "Dashboard",
    "Profil",
    "Produk",
    "Kategori Produk",
    "Manajemen Stok",
    "Kasir",
    "Transaksi",
    "Promo",
    "Antrean",
    "Riwayat Antrean",
    "Laporan Keuangan",
    "Manajemen Pengguna",
    "Audit Log"
).associateWith { true }

data class UserSessionState(
    val user: User? = null,
    val roleName: String = "",
    val menuAccess: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val getRolesUseCase: GetRolesUseCase,
    private val userRemoteDataSource: UserRemoteDataSource
) : ViewModel() {

    private val _sessionState = MutableStateFlow(UserSessionState())
    val sessionState: StateFlow<UserSessionState> = _sessionState.asStateFlow()

    init {
        loadCurrentUserAndPermissions()
    }

    fun loadCurrentUserAndPermissions() {
        viewModelScope.launch {
            val firebaseUser = firebaseAuth.currentUser
            val email = firebaseUser?.email.orEmpty()
            if (firebaseUser == null) {
                _sessionState.value = UserSessionState(isLoading = false)
                return@launch
            }

            // Validasi apakah akun masih terdaftar di Firebase Auth
            try {
                firebaseUser.reload().await()
            } catch (e: Exception) {
                if (e is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                    e.message?.contains("no user record", ignoreCase = true) == true ||
                    e.message?.contains("user-not-found", ignoreCase = true) == true) {
                    withContext(Dispatchers.IO) {
                        try {
                            val local = email.takeIf { it.isNotBlank() }?.let { userDao.getUserByEmail(it) }
                            if (local != null) {
                                userDao.deletePermanently(local.id)
                                userRemoteDataSource.deleteUser(local.id)
                            }
                        } catch (_: Exception) {}
                    }
                    firebaseAuth.signOut()
                    _sessionState.value = UserSessionState(user = null, isLoading = false)
                    return@launch
                }
            }

            // 1. Instantly load from local Room DB cache on IO thread (zero UI delay)
            withContext(Dispatchers.IO) {
                val localUser = email.takeIf { it.isNotBlank() }
                    ?.let { userDao.getUserByEmail(it) }

                val remoteUser = if (localUser == null && email.isNotBlank()) {
                    try {
                        userRemoteDataSource.getAllUsers().find { it.email.equals(email, ignoreCase = true) }
                    } catch (e: Exception) {
                        null
                    }
                } else null

                // Cek apakah akun terdaftar di deleted_accounts
                val deletedDoc = try {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("deleted_accounts")
                        .document(email.lowercase())
                        .get()
                        .await()
                } catch (e: Exception) {
                    null
                }

                val isDeletedAccount = if (deletedDoc != null && deletedDoc.exists()) {
                    val deletedAtMillis = deletedDoc.getTimestamp("deletedAt")?.toDate()?.time
                        ?: deletedDoc.getLong("deletedAt")
                        ?: 0L
                    val creationMillis = firebaseUser.metadata?.creationTimestamp ?: 0L

                    if (creationMillis > deletedAtMillis) {
                        try {
                            deletedDoc.reference.delete().await()
                        } catch (_: Exception) {}
                        false
                    } else {
                        true
                    }
                } else {
                    false
                }

                if (isDeletedAccount) {
                    try {
                        firebaseUser.delete().await()
                    } catch (_: Exception) {}
                    try {
                        deletedDoc?.reference?.delete()?.await()
                    } catch (_: Exception) {}
                    firebaseAuth.signOut()
                    _sessionState.value = UserSessionState(user = null, isLoading = false)
                    return@withContext
                }

                val activeUser = localUser?.let {
                    User(
                        id = it.id,
                        fullName = it.fullName,
                        email = it.email,
                        roleId = it.roleId.ifBlank { "Admin" },
                        isActive = it.isActive,
                        photoUrl = it.photoUrl
                    )
                } ?: remoteUser?.let {
                    userDao.insertOrUpdate(it.toEntity())
                    it.toDomain()
                } ?: run {
                    // Akun dibuat langsung di Firebase Auth Console
                    val newUserId = firebaseUser.uid
                    val newFullName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@").ifBlank { "Pengguna" }
                    val newPhotoUrl = firebaseUser.photoUrl?.toString().orEmpty()
                    val newUser = User(
                        id = newUserId,
                        fullName = newFullName,
                        email = email,
                        roleId = "Admin",
                        isActive = true,
                        photoUrl = newPhotoUrl,
                        createdAt = com.google.firebase.Timestamp.now(),
                        updatedAt = com.google.firebase.Timestamp.now(),
                        isSynced = true
                    )
                    try {
                        userRemoteDataSource.saveUser(newUser.toDto())
                    } catch (_: Exception) {}
                    userDao.insertOrUpdate(newUser.toEntity(isSynced = true))
                    newUser
                }

                if (activeUser != null && !activeUser.isActive) {
                    firebaseAuth.signOut()
                    _sessionState.value = UserSessionState(user = null, isLoading = false)
                    return@withContext
                }

                val roleName = activeUser?.roleId.takeIf { !it.isNullOrBlank() } ?: "Admin"
                val isAdminRole = roleName.equals("Admin", ignoreCase = true) || 
                        roleName.equals("System Administrator", ignoreCase = true) ||
                        roleName.isBlank()

                val allLocalRoles = roleDao.getAllRoles()

                val matchedRole = allLocalRoles.find { role ->
                    roleName.isNotBlank() && (
                        role.name.equals(roleName, ignoreCase = true) ||
                            role.id.equals(roleName, ignoreCase = true)
                        )
                }

                val menuAccess = when {
                    isAdminRole -> DEFAULT_UNASSIGNED_ROLE_ACCESS
                    matchedRole != null -> matchedRole.menuAccess
                    else -> DEFAULT_UNASSIGNED_ROLE_ACCESS
                }

                _sessionState.value = UserSessionState(
                    user = activeUser,
                    roleName = if (isAdminRole) "Admin" else roleName,
                    menuAccess = menuAccess,
                    isLoading = false
                )
            }

            // 2. Realtime sync updates from GetRolesUseCase
            getRolesUseCase().collect { roles ->
                val currentState = _sessionState.value
                val roleName = currentState.roleName
                val isAdminRole = roleName.equals("Admin", ignoreCase = true) || 
                        roleName.equals("System Administrator", ignoreCase = true) ||
                        roleName.isBlank()

                if (isAdminRole) {
                    _sessionState.value = currentState.copy(
                        menuAccess = DEFAULT_UNASSIGNED_ROLE_ACCESS,
                        isLoading = false
                    )
                    return@collect
                }

                val matchedRole = roles.find { role ->
                    roleName.isNotBlank() && (
                        role.name.equals(roleName, ignoreCase = true) ||
                            role.id.equals(roleName, ignoreCase = true)
                        )
                }

                if (matchedRole != null) {
                    _sessionState.value = currentState.copy(
                        menuAccess = matchedRole.menuAccess,
                        isLoading = false
                    )
                } else if (roleName.isNotBlank() && currentState.menuAccess.isNotEmpty()) {
                    _sessionState.value = currentState.copy(
                        menuAccess = emptyMap(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun isAllowed(menuKey: String): Boolean {
        val state = _sessionState.value
        val currentRole = state.roleName
        val isAdminRole = currentRole.isBlank() || 
                currentRole.equals("Admin", ignoreCase = true) || 
                currentRole.equals("System Administrator", ignoreCase = true)

        // Admin, System Administrator, dan akun Firebase default memiliki akses penuh ke seluruh fitur
        if (isAdminRole) {
            return true
        }

        // Dashboard dan Profil selalu diizinkan
        if (menuKey.equals("Dashboard", ignoreCase = true) || menuKey.equals("Profil", ignoreCase = true)) {
            return true
        }

        val direct = state.menuAccess[menuKey]
        if (direct != null) return direct

        val matched = state.menuAccess.entries.find { it.key.equals(menuKey, ignoreCase = true) }
        return matched?.value ?: false
    }
}
