package com.ptpws.ikikasir.feature.role.presentation.state

import com.ptpws.ikikasir.feature.role.domain.model.Role

data class RoleListState(
    val isLoading: Boolean = false,
    val roles: List<Role> = emptyList(),
    val error: String? = null,
    val isSyncing: Boolean = false,
    val message: String? = null
)
