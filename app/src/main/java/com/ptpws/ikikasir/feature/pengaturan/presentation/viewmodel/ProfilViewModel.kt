package com.ptpws.ikikasir.feature.pengaturan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.datasource.UserRemoteDataSource
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetNotaSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetPaymentMethodSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetTableSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetTaxSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.SaveNotaSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.SavePaymentMethodSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.SaveTableSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.SaveTaxSettingUseCase
import com.ptpws.ikikasir.feature.role.data.local.dao.RoleDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class UserProfileState(
    val user: User? = null,
    val roleName: String = "Kasir",
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val getNotaSettingUseCase: GetNotaSettingUseCase,
    private val saveNotaSettingUseCase: SaveNotaSettingUseCase,
    private val getTaxSettingUseCase: GetTaxSettingUseCase,
    private val saveTaxSettingUseCase: SaveTaxSettingUseCase,
    private val getPaymentMethodSettingUseCase: GetPaymentMethodSettingUseCase,
    private val savePaymentMethodSettingUseCase: SavePaymentMethodSettingUseCase,
    private val getTableSettingUseCase: GetTableSettingUseCase,
    private val saveTableSettingUseCase: SaveTableSettingUseCase,
    private val logActivityUseCase: LogActivityUseCase,
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val userRemoteDataSource: UserRemoteDataSource
) : ViewModel() {

    private val _userProfileState = MutableStateFlow(UserProfileState())
    val userProfileState: StateFlow<UserProfileState> = _userProfileState.asStateFlow()

    private val _notaSetting = MutableStateFlow(NotaSetting())
    val notaSetting: StateFlow<NotaSetting> = _notaSetting.asStateFlow()

    private val _taxSetting = MutableStateFlow(TaxSetting())
    val taxSetting: StateFlow<TaxSetting> = _taxSetting.asStateFlow()

    private val _paymentMethodSetting = MutableStateFlow(PaymentMethodSetting())
    val paymentMethodSetting: StateFlow<PaymentMethodSetting> = _paymentMethodSetting.asStateFlow()

    private val _tableSetting = MutableStateFlow(TableSetting())
    val tableSetting: StateFlow<TableSetting> = _tableSetting.asStateFlow()

    init {
        loadUserProfile()
        loadNotaSetting()
        loadTaxSetting()
        loadPaymentMethodSetting()
        loadTableSetting()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _userProfileState.value = _userProfileState.value.copy(isLoading = true)
            val firebaseUser = firebaseAuth.currentUser
            val email = firebaseUser?.email.orEmpty()

            withContext(Dispatchers.IO) {
                val localUser = if (email.isNotBlank()) userDao.getUserByEmail(email)?.toDomain() else null
                val user = localUser?.let {
                    if (it.roleId.isBlank()) it.copy(roleId = "Admin") else it
                } ?: User(
                    id = firebaseUser?.uid ?: "user_default",
                    fullName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@").ifBlank { "Admin User" },
                    email = email,
                    roleId = "Admin",
                    photoUrl = firebaseUser?.photoUrl?.toString().orEmpty()
                )

                val allRoles = roleDao.getAllRoles()
                val matchedRole = allRoles.find { role ->
                    user.roleId.isNotBlank() && (
                        role.id.equals(user.roleId, ignoreCase = true) ||
                            role.name.equals(user.roleId, ignoreCase = true)
                        )
                }
                val roleDisplayName = matchedRole?.name 
                    ?: if (user.roleId.equals("Admin", ignoreCase = true) || user.roleId.isBlank()) "System Administrator" 
                    else user.roleId

                _userProfileState.value = UserProfileState(
                    user = user,
                    roleName = roleDisplayName,
                    isLoading = false
                )
            }
        }
    }

    fun updateUserProfile(newFullName: String, newPhotoUrl: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val current = _userProfileState.value.user ?: return@launch
            val updatedUser = current.copy(
                fullName = newFullName.trim(),
                photoUrl = newPhotoUrl,
                updatedAt = com.google.firebase.Timestamp.now()
            )

            withContext(Dispatchers.IO) {
                try {
                    // Update Firebase Auth user profile
                    firebaseAuth.currentUser?.let { fUser ->
                        val changeRequest = UserProfileChangeRequest.Builder()
                            .setDisplayName(newFullName.trim())
                            .apply {
                                if (newPhotoUrl.isNotBlank()) setPhotoUri(android.net.Uri.parse(newPhotoUrl))
                            }
                            .build()
                        try {
                            fUser.updateProfile(changeRequest).await()
                        } catch (e: Exception) {
                            android.util.Log.w("ProfilViewModel", "Firebase Auth update profile error: ${e.message}")
                        }
                    }

                    // Update Room DB
                    userDao.insertOrUpdate(updatedUser.toEntity(isSynced = true))

                    // Update Firestore
                    try {
                        userRemoteDataSource.updateUser(updatedUser.toDto())
                    } catch (e: Exception) {
                        userDao.insertOrUpdate(updatedUser.toEntity(isSynced = false))
                    }

                    _userProfileState.value = _userProfileState.value.copy(user = updatedUser)

                    logActivityUseCase(
                        title = "Pembaruan Profil",
                        description = "Profil pengguna '${updatedUser.fullName}' diperbarui.",
                        category = "USER_MANAGEMENT",
                        action = "UPDATE"
                    )

                    withContext(Dispatchers.Main) {
                        GlobalCrudResultDialog.success("Profil berhasil diperbarui.")
                        onComplete(true)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        GlobalCrudResultDialog.failure(e.message ?: "Gagal memperbarui profil.")
                        onComplete(false)
                    }
                }
            }
        }
    }

    fun loadNotaSetting() {
        viewModelScope.launch {
            getNotaSettingUseCase().collect { setting ->
                _notaSetting.value = setting
            }
        }
    }

    fun loadTaxSetting() {
        viewModelScope.launch {
            getTaxSettingUseCase().collect { setting ->
                _taxSetting.value = setting
            }
        }
    }

    fun loadPaymentMethodSetting() {
        viewModelScope.launch {
            getPaymentMethodSettingUseCase().collect { setting ->
                _paymentMethodSetting.value = setting
            }
        }
    }

    fun loadTableSetting() {
        viewModelScope.launch {
            getTableSettingUseCase().collect { setting ->
                _tableSetting.value = setting
            }
        }
    }

    fun saveNotaSetting(setting: NotaSetting, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            saveNotaSettingUseCase(setting).collect { result ->
                result.fold(
                    onSuccess = {
                        _notaSetting.value = setting
                        GlobalCrudResultDialog.success("Pengaturan nota berhasil disimpan.")
                        logActivityUseCase(
                            title = "Perubahan Pengaturan Nota",
                            description = "Pengaturan identitas toko, jaringan nota, atau ukuran kertas diperbarui.",
                            category = "SYSTEM",
                            action = "UPDATE"
                        )
                        onComplete(true)
                    },
                    onFailure = { error ->
                        GlobalCrudResultDialog.failure(error.message ?: "Gagal menyimpan pengaturan nota.")
                        onComplete(false)
                    }
                )
            }
        }
    }

    fun saveTaxSetting(setting: TaxSetting, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            saveTaxSettingUseCase(setting).collect { result ->
                result.fold(
                    onSuccess = {
                        _taxSetting.value = setting
                        GlobalCrudResultDialog.success("Pengaturan pajak berhasil disimpan.")
                        logActivityUseCase(
                            title = "Perubahan Pengaturan Pajak",
                            description = "Pengaturan pajak ${if (setting.isActive) "diaktifkan" else "dinonaktifkan"} menjadi ${setting.percentage}% (${setting.type}).",
                            category = "SYSTEM",
                            action = "UPDATE"
                        )
                        onComplete(true)
                    },
                    onFailure = { error ->
                        GlobalCrudResultDialog.failure(error.message ?: "Gagal menyimpan pengaturan pajak.")
                        onComplete(false)
                    }
                )
            }
        }
    }

    fun savePaymentMethodSetting(setting: PaymentMethodSetting, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = savePaymentMethodSettingUseCase(setting)
            result.fold(
                onSuccess = {
                    _paymentMethodSetting.value = setting
                    GlobalCrudResultDialog.success("Pengaturan metode pembayaran berhasil disimpan.")
                    logActivityUseCase(
                        title = "Perubahan Metode Pembayaran",
                        description = "Pengaturan metode pembayaran tunai, QRIS, transfer, dan kartu diperbarui.",
                        category = "SYSTEM",
                        action = "UPDATE"
                    )
                    onComplete(true)
                },
                onFailure = { error ->
                    GlobalCrudResultDialog.failure(error.message ?: "Gagal menyimpan metode pembayaran.")
                    onComplete(false)
                }
            )
        }
    }

    fun saveTableSetting(setting: TableSetting, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = saveTableSettingUseCase(setting)
            result.fold(
                onSuccess = {
                    _tableSetting.value = setting
                    GlobalCrudResultDialog.success("Pengaturan meja berhasil disimpan.")
                    logActivityUseCase(
                        title = "Perubahan Pengaturan Meja",
                        description = "Fitur meja ${if (setting.isTableEnabled) "diaktifkan" else "dinonaktifkan"}.",
                        category = "SYSTEM",
                        action = "UPDATE"
                    )
                    onComplete(true)
                },
                onFailure = { error ->
                    GlobalCrudResultDialog.failure(error.message ?: "Gagal menyimpan pengaturan meja.")
                    onComplete(false)
                }
            )
        }
    }
}
