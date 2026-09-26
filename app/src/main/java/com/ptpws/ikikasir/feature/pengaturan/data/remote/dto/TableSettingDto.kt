package com.ptpws.ikikasir.feature.pengaturan.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting

data class TableSettingDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "default",

    @get:PropertyName("isTableEnabled") @set:PropertyName("isTableEnabled")
    var isTableEnabled: Boolean = true,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Timestamp = Timestamp.now()
) {
    fun toDomain(): TableSetting {
        return TableSetting(
            id = id,
            isTableEnabled = isTableEnabled,
            updatedAt = updatedAt
        )
    }
}

fun TableSetting.toDto(): TableSettingDto {
    return TableSettingDto(
        id = id,
        isTableEnabled = isTableEnabled,
        updatedAt = updatedAt
    )
}
