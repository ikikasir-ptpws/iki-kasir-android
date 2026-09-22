package com.ptpws.ikikasir.feature.role.data.remote.datasource

import com.ptpws.ikikasir.feature.role.data.remote.dto.RoleDto
import kotlinx.coroutines.flow.Flow

interface RoleRemoteDataSource {
    fun getRoleFlow(): Flow<List<RoleDto>>
    suspend fun getAllRoles(): List<RoleDto>
    suspend fun getRoleById(id: String): RoleDto?
    suspend fun saveRole(roleDto: RoleDto)
    suspend fun updateRole(roleDto: RoleDto)
    suspend fun deleteRole(id: String)
}
