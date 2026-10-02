package com.ptpws.ikikasir.feature.manajemenpengguna.presentation.state

data class UserFormState(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",         // hanya dipakai di form UI untuk dikirim ke Firebase Auth
    val confirmPassword: String = "",  // hanya dipakai di form UI untuk validasi
    val roleId: String = "",
    val isActive: Boolean = true,
    val photoUrl: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
