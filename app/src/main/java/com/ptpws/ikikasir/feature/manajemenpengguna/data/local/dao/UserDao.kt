package com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) AND isDeleted = 0 LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Upsert
    suspend fun insertOrUpdate(user: UserEntity)

    @Upsert
    suspend fun insertOrUpdateAll(users: List<UserEntity>)

    @Query("UPDATE users SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsDeleted(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    @Query("UPDATE users SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
