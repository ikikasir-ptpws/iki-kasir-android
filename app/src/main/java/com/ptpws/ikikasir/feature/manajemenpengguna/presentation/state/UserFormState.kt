package com.ptpws.ikikasir.feature.manajemenpengguna.presentation.state

data class UserFormState(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val roleId: String = "",
    val isActive: Boolean = true,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
