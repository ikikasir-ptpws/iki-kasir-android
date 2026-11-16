package com.ptpws.ikikasir.feature.pengaturan.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.feature.pengaturan.domain.model.ArSetting

data class ArSettingDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "default",

    @get:PropertyName("isArEnabled") @set:PropertyName("isArEnabled")
    var isArEnabled: Boolean = false,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Timestamp = Timestamp.now()
) {
    fun toDomain(): ArSetting {
        return ArSetting(
            id = id,
            isArEnabled = isArEnabled,
            updatedAt = updatedAt,
            isSynced = true
        )
    }
}

fun ArSetting.toDto(): ArSettingDto {
    return ArSettingDto(
        id = id,
        isArEnabled = isArEnabled,
        updatedAt = updatedAt
    )
}
