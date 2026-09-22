package com.ptpws.ikikasir.feature.role.domain.usecase

import com.ptpws.ikikasir.feature.role.domain.repository.RoleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncRolesUseCase @Inject constructor(
    private val repository: RoleRepository
) {
    suspend operator fun invoke(): Flow<Result<Unit>> = repository.syncPendingRoles()
}
