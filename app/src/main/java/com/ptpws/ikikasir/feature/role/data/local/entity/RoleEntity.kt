package com.ptpws.ikikasir.feature.role.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.role.domain.model.Role

@Entity(tableName = "roles")
data class RoleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "menuAccess")
    val menuAccess: Map<String, Boolean>,

    @ColumnInfo(name = "userCount")
    val userCount: Int = 0,

    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true,

    @ColumnInfo(name = "isDeleted")
    val isDeleted: Boolean = false
) {
    fun toDomain(): Role {
        return Role(
            id = id,
            name = name,
            description = description,
            menuAccess = menuAccess,
            userCount = userCount,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }
}

fun Role.toEntity(isSynced: Boolean = true, isDeleted: Boolean = false): RoleEntity {
    return RoleEntity(
        id = id,
        name = name,
        description = description,
        menuAccess = menuAccess,
        userCount = userCount,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}
