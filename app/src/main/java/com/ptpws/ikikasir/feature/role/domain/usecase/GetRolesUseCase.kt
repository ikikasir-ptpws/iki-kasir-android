package com.ptpws.ikikasir.feature.role.domain.usecase

import com.ptpws.ikikasir.feature.role.domain.model.Role
import com.ptpws.ikikasir.feature.role.domain.repository.RoleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRolesUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    operator fun invoke(): Flow<List<Role>> = repository.getRoleList()
}
