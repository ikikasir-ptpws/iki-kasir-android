package com.ptpws.ikikasir.feature.manajemenpengguna.domain.repository

import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserList(): Flow<List<User>>
    fun getUserById(id: String): Flow<User?>
    // plainPassword hanya untuk Firebase Auth — tidak disimpan ke DB atau Firestore
    suspend fun insertUser(user: User, plainPassword: String): Flow<Result<Unit>>
    suspend fun updateUser(user: User, plainPassword: String? = null): Flow<Result<Unit>>
    suspend fun deleteUser(id: String): Flow<Result<Unit>>
    suspend fun syncPendingUsers(): Flow<Result<Unit>>
}
