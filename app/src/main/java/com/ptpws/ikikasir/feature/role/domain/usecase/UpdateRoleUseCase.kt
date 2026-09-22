package com.ptpws.ikikasir.feature.role.domain.usecase

import com.ptpws.ikikasir.feature.role.domain.model.Role
import com.ptpws.ikikasir.feature.role.domain.repository.RoleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateRoleUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(role: Role): Flow<Result<Unit>> = repository.updateRole(role)
}
