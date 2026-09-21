package com.ptpws.ikikasir.feature.manajemenpengguna.domain.usecase

import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InsertUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User): Flow<Result<Unit>> = repository.insertUser(user)
}
