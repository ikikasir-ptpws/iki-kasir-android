package com.ptpws.ikikasir.feature.manajemenpengguna.domain.usecase

import com.ptpws.ikikasir.feature.manajemenpengguna.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(id: String): Flow<Result<Unit>> = repository.deleteUser(id)
}
