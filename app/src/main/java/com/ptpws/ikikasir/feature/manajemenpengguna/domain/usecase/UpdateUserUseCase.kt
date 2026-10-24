package com.ptpws.ikikasir.feature.manajemenpengguna.domain.usecase

import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User, plainPassword: String? = null, currentPassword: String? = null, oldEmail: String? = null): Flow<Result<Unit>> =
        repository.updateUser(user, plainPassword, currentPassword, oldEmail)
}
