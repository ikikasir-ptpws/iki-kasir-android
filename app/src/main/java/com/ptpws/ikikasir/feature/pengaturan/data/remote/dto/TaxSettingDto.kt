package com.ptpws.ikikasir.feature.pengaturan.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.TaxSettingEntity
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting

data class TaxSettingDto(
    @get:Exclude @set:Exclude
    var id: String = "default",

    @get:PropertyName("isActive") @set:PropertyName("isActive")
    var isActive: Boolean = true,

    @get:PropertyName("percentage") @set:PropertyName("percentage")
    var percentage: Double = 11.0,

    @get:PropertyName("type") @set:PropertyName("type")
    var type: String = TaxSetting.TAX_TYPE_EXCLUSIVE,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @get:ServerTimestamp
    var updatedAt: Timestamp? = null
) {
    fun toEntity(): TaxSettingEntity {
        return TaxSettingEntity(
            id = id.ifBlank { "default" },
            isActive = isActive,
            percentage = percentage,
            type = type,
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true
        )
    }
}

fun TaxSetting.toDto(): TaxSettingDto {
    return TaxSettingDto(
        id = id,
        isActive = isActive,
        percentage = percentage,
        type = type,
        updatedAt = null
    )
}
