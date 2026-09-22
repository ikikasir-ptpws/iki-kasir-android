package com.ptpws.ikikasir.feature.role.domain.usecase

import com.ptpws.ikikasir.feature.role.domain.repository.RoleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteRoleUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(id: String): Flow<Result<Unit>> = repository.deleteRole(id)
}
