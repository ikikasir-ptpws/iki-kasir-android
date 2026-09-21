package com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "fullName")
    val fullName: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "roleId")
    val roleId: String,

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
    fun toDomain(): User {
        return User(
            id = id,
            fullName = fullName,
            email = email,
            password = password,
            roleId = roleId,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }
}

fun User.toEntity(isSynced: Boolean = true, isDeleted: Boolean = false): UserEntity {
    return UserEntity(
        id = id,
        fullName = fullName,
        email = email,
        password = password,
        roleId = roleId,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}
