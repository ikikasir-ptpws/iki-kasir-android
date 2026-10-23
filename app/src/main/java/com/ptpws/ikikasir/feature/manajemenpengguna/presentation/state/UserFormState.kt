package com.ptpws.ikikasir.feature.manajemenpengguna.presentation.state

data class UserFormState(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val currentPassword: String = "",
    val password: String = "",         // password baru jika diisi
    val confirmPassword: String = "",  // konfirmasi password baru
    val roleId: String = "",
    val isActive: Boolean = true,
    val photoUrl: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
