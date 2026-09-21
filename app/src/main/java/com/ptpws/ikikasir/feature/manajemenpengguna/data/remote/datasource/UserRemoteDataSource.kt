package com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.datasource

import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto.UserDto
import kotlinx.coroutines.flow.Flow

interface UserRemoteDataSource {
    fun getUserFlow(): Flow<List<UserDto>>
    suspend fun getAllUsers(): List<UserDto>
    suspend fun getUserById(id: String): UserDto?
    suspend fun saveUser(userDto: UserDto)
    suspend fun updateUser(userDto: UserDto)
    suspend fun deleteUser(id: String)
}
