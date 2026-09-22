package com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.UserEntity
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User

data class UserDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    @get:PropertyName("fullName") @set:PropertyName("fullName")
    var fullName: String = "",

    @get:PropertyName("email") @set:PropertyName("email")
    var email: String = "",

    @get:PropertyName("password") @set:PropertyName("password")
    var password: String = "",

    @get:PropertyName("roleId") @set:PropertyName("roleId")
    var roleId: String = "Kasir",

    @get:PropertyName("isActive") @set:PropertyName("isActive")
    var isActive: Boolean = true,

    @get:PropertyName("photoUrl") @set:PropertyName("photoUrl")
    var photoUrl: String = "",

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    @get:ServerTimestamp
    var createdAt: Timestamp? = null,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @get:ServerTimestamp
    var updatedAt: Timestamp? = null
) {
    fun toDomain(): User {
        return User(
            id = id,
            fullName = fullName,
            email = email,
            password = password,
            roleId = roleId,
            isActive = isActive,
            photoUrl = photoUrl,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true
        )
    }

    fun toEntity(): UserEntity {
        return UserEntity(
            id = id,
            fullName = fullName,
            email = email,
            password = password,
            roleId = roleId,
            isActive = isActive,
            photoUrl = photoUrl,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true,
            isDeleted = false
        )
    }
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        fullName = fullName,
        email = email,
        password = password,
        roleId = roleId,
        isActive = isActive,
        photoUrl = photoUrl,
        createdAt = null,
        updatedAt = null
    )
}
