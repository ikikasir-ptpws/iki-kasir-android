package com.ptpws.ikikasir.feature.role.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ptpws.ikikasir.feature.role.data.local.entity.RoleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {

    @Query("SELECT * FROM roles WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllRolesFlow(): Flow<List<RoleEntity>>

    @Query("SELECT * FROM roles WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getRoleByIdFlow(id: String): Flow<RoleEntity?>

    @Query("SELECT * FROM roles WHERE id = :id LIMIT 1")
    suspend fun getRoleById(id: String): RoleEntity?

    @Upsert
    suspend fun insertOrUpdate(role: RoleEntity)

    @Upsert
    suspend fun insertOrUpdateAll(roles: List<RoleEntity>)

    @Query("UPDATE roles SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsDeleted(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM roles WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("SELECT * FROM roles WHERE isSynced = 0")
    suspend fun getUnsyncedRoles(): List<RoleEntity>

    @Query("UPDATE roles SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
