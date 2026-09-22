package com.ptpws.ikikasir.feature.role.domain.repository

import com.ptpws.ikikasir.feature.role.domain.model.Role
import kotlinx.coroutines.flow.Flow

interface RoleRepository {
    fun getRoleList(): Flow<List<Role>>
    fun getRoleById(id: String): Flow<Role?>
    suspend fun insertRole(role: Role): Flow<Result<Unit>>
    suspend fun updateRole(role: Role): Flow<Result<Unit>>
    suspend fun deleteRole(id: String): Flow<Result<Unit>>
    suspend fun syncPendingRoles(): Flow<Result<Unit>>
}
