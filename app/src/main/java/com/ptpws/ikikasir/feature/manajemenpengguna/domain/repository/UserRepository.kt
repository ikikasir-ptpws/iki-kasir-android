package com.ptpws.ikikasir.feature.manajemenpengguna.domain.repository

import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserList(): Flow<List<User>>
    fun getUserById(id: String): Flow<User?>
    suspend fun insertUser(user: User): Flow<Result<Unit>>
    suspend fun updateUser(user: User): Flow<Result<Unit>>
    suspend fun deleteUser(id: String): Flow<Result<Unit>>
    suspend fun syncPendingUsers(): Flow<Result<Unit>>
}
