package com.ptpws.ikikasir.feature.manajemenpengguna.domain.usecase

import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InsertUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    // plainPassword diteruskan ke Firebase Auth saja, tidak disimpan ke DB manapun
    suspend operator fun invoke(user: User, plainPassword: String): Flow<Result<Unit>> =
        repository.insertUser(user, plainPassword)
}
