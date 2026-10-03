package com.ptpws.ikikasir.feature.auth.domain.usecase

import com.ptpws.ikikasir.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> =
        authRepository.changePassword(currentPassword, newPassword)
}
