package com.ptpws.ikikasir.feature.role.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.ptpws.ikikasir.feature.role.data.local.entity.RoleEntity
import com.ptpws.ikikasir.feature.role.domain.model.Role

data class RoleDto(
    @get:Exclude @set:Exclude
    var id: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("menuAccess") @set:PropertyName("menuAccess")
    var menuAccess: Map<String, Boolean> = emptyMap(),

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    @get:ServerTimestamp
    var createdAt: Timestamp? = null,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @get:ServerTimestamp
    var updatedAt: Timestamp? = null
) {
    fun toDomain(): Role {
        return Role(
            id = id,
            name = name,
            description = description,
            menuAccess = menuAccess,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true
        )
    }

    fun toEntity(): RoleEntity {
        return RoleEntity(
            id = id,
            name = name,
            description = description,
            menuAccess = menuAccess,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true,
            isDeleted = false
        )
    }
}

fun Role.toDto(): RoleDto {
    return RoleDto(
        id = id,
        name = name,
        description = description,
        menuAccess = menuAccess,
        createdAt = null,
        updatedAt = null
    )
}
