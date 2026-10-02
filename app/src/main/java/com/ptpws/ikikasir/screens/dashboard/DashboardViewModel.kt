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
    "Manajemen Role",
    "Pengaturan Menu",
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
    private val getRolesUseCase: GetRolesUseCase
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

            // 1. Instantly load from local Room DB cache on IO thread (zero UI delay)
            withContext(Dispatchers.IO) {
                val localUser = email.takeIf { it.isNotBlank() }
                    ?.let { userDao.getUserByEmail(it) }
                val roleName = localUser?.roleId.takeIf { !it.isNullOrBlank() } ?: "Admin"
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

                val user = localUser?.let {
                    User(
                        id = it.id,
                        fullName = it.fullName,
                        email = it.email,
                        roleId = it.roleId.ifBlank { "Admin" },
                        isActive = it.isActive,
                        photoUrl = it.photoUrl
                    )
                } ?: User(
                    id = firebaseUser.uid,
                    fullName = firebaseUser.displayName
                        ?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@").ifBlank { "Pengguna" },
                    email = email,
                    roleId = "Admin",
                    isActive = true
                )

                _sessionState.value = UserSessionState(
                    user = user,
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
