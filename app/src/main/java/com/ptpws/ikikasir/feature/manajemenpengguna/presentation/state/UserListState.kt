package com.ptpws.ikikasir.feature.manajemenpengguna.presentation.state

import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User

data class UserListState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val isSyncing: Boolean = false,
    val message: String? = null
)
