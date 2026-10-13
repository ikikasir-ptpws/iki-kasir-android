package com.ptpws.ikikasir.feature.sidebar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.role.data.local.dao.RoleDao
import com.ptpws.ikikasir.feature.role.domain.usecase.GetRolesUseCase
import com.ptpws.ikikasir.feature.sidebar.domain.usecase.GetSidebarMenuUseCase
import com.ptpws.ikikasir.feature.sidebar.presentation.state.SidebarUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SidebarViewModel @Inject constructor(
    private val getSidebarMenuUseCase: GetSidebarMenuUseCase,
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val getRolesUseCase: GetRolesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SidebarUiState(isLoading = true))
    val uiState: StateFlow<SidebarUiState> = _uiState.asStateFlow()

    init {
        loadUserAndPermissions()
        observeRoles()
        observeCurrentUser()
    }

    /**
     * Memuat sesi pengguna lokal dan hak akses peran untuk membangun daftar menu sidebar.
     */
    fun loadUserAndPermissions() {
        viewModelScope.launch {
            val firebaseUser = firebaseAuth.currentUser
            val email = firebaseUser?.email.orEmpty().trim()

            withContext(Dispatchers.IO) {
                val localUser = if (email.isNotBlank()) userDao.getUserByEmail(email) else null
                val userName = localUser?.fullName?.ifBlank { firebaseUser?.displayName.orEmpty() }
                    ?.ifBlank { "Pengguna" } ?: "Pengguna"
                val roleName = localUser?.roleId.takeIf { !it.isNullOrBlank() } ?: "Admin"
                val photoUrl = localUser?.photoUrl ?: firebaseUser?.photoUrl?.toString()

                val isAdmin = roleName.equals("Admin", ignoreCase = true) ||
                        roleName.equals("Super Admin", ignoreCase = true) ||
                        roleName.equals("System Administrator", ignoreCase = true) ||
                        roleName.isBlank()

                val menuAccess = if (isAdmin) {
                    emptyMap()
                } else {
                    val allRoles = roleDao.getAllRoles()
                    val matchedRole = allRoles.find {
                        it.name.equals(roleName, ignoreCase = true) || it.id.equals(roleName, ignoreCase = true)
                    }
                    matchedRole?.menuAccess ?: emptyMap()
                }

                val menuItems = getSidebarMenuUseCase(
                    userRole = if (isAdmin) "Admin" else roleName,
                    menuAccess = menuAccess
                )

                _uiState.update { current ->
                    current.copy(
                        menuItems = menuItems,
                        userName = userName,
                        userRole = if (isAdmin) "Super Admin" else roleName,
                        userPhotoUrl = photoUrl,
                        isLoading = false
                    )
                }

                // Otomatis buka grup jika currentRoute sudah disetel
                _uiState.value.currentRoute?.let { autoExpandMatchingGroup(it) }
            }
        }
    }

    /**
     * Mengamati perubahan daftar role secara realtime untuk memperbarui hak akses menu.
     */
    private fun observeRoles() {
        viewModelScope.launch {
            getRolesUseCase().collect {
                val currentState = _uiState.value
                val roleName = currentState.userRole
                val isAdmin = roleName.equals("Admin", ignoreCase = true) ||
                        roleName.equals("Super Admin", ignoreCase = true) ||
                        roleName.equals("System Administrator", ignoreCase = true) ||
                        roleName.isBlank()

                if (!isAdmin) {
                    withContext(Dispatchers.IO) {
                        val allRoles = roleDao.getAllRoles()
                        val matchedRole = allRoles.find {
                            it.name.equals(roleName, ignoreCase = true) || it.id.equals(roleName, ignoreCase = true)
                        }
                        val menuAccess = matchedRole?.menuAccess ?: emptyMap()
                        val updatedMenus = getSidebarMenuUseCase(userRole = roleName, menuAccess = menuAccess)
                        _uiState.update { it.copy(menuItems = updatedMenus) }
                    }
                }
            }
        }
    }

    /**
     * Membuka atau menutup grup menu accordion (expand / collapse toggle).
     *
     * @param groupId ID menu yang di-klik pengguna (misalnya "produk", "penjualan").
     */
    fun toggleGroup(groupId: String) {
        _uiState.update { current ->
            val updated = if (groupId in current.expandedGroupIds) {
                current.expandedGroupIds - groupId
            } else {
                current.expandedGroupIds + groupId
            }
            current.copy(expandedGroupIds = updated)
        }
    }

    /**
     * Membuka grup menu tertentu secara spesifik.
     */
    fun expandGroup(groupId: String) {
        _uiState.update { current ->
            current.copy(expandedGroupIds = current.expandedGroupIds + groupId)
        }
    }

    /**
     * Menutup grup menu tertentu.
     */
    fun collapseGroup(groupId: String) {
        _uiState.update { current ->
            current.copy(expandedGroupIds = current.expandedGroupIds - groupId)
        }
    }

    /**
     * Memperbarui rute aktif dan secara otomatis memastikan parent grup
     * dari rute tersebut dalam kondisi terbuka (expanded).
     */
    fun updateCurrentRoute(route: String?) {
        if (route == null) return
        _uiState.update { it.copy(currentRoute = route) }
        autoExpandMatchingGroup(route)
    }

    private fun autoExpandMatchingGroup(route: String) {
        val currentMenus = _uiState.value.menuItems
        val matchingGroup = currentMenus.find { it.isExpandable && it.containsRoute(route) }
        if (matchingGroup != null && matchingGroup.id !in _uiState.value.expandedGroupIds) {
            _uiState.update { current ->
                current.copy(expandedGroupIds = current.expandedGroupIds + matchingGroup.id)
            }
        }
    }

    /**
     * Mengamati perubahan profil user dari Room DB secara realtime
     * agar perubahan nama/foto di Edit Profil langsung tercermin di Sidebar.
     */
    private fun observeCurrentUser() {
        val email = firebaseAuth.currentUser?.email.orEmpty().trim()
        if (email.isBlank()) return
        viewModelScope.launch {
            userDao.getUserByEmailFlow(email).filterNotNull().collect { entity ->
                val newName = entity.fullName.ifBlank { "Pengguna" }
                val newRole = entity.roleId.takeIf { it.isNotBlank() } ?: "Admin"
                val isAdmin = newRole.equals("Admin", ignoreCase = true) ||
                        newRole.equals("Super Admin", ignoreCase = true) ||
                        newRole.equals("System Administrator", ignoreCase = true)

                _uiState.update { current ->
                    current.copy(
                        userName = newName,
                        userRole = if (isAdmin) "Super Admin" else newRole,
                        userPhotoUrl = entity.photoUrl,
                        isLoading = false
                    )
                }
            }
        }
    }
}
