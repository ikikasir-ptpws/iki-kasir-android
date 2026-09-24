package com.ptpws.ikikasir.feature.pengaturan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting

@Entity(tableName = "tax_settings")
data class TaxSettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "default",

    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "percentage")
    val percentage: Double = 11.0,

    @ColumnInfo(name = "type")
    val type: String = TaxSetting.TAX_TYPE_EXCLUSIVE,

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false
) {
    fun toDomain(): TaxSetting {
        return TaxSetting(
            id = id,
            isActive = isActive,
            percentage = percentage,
            type = type,
            updatedAt = updatedAt
        )
    }
}

fun TaxSetting.toEntity(isSynced: Boolean = false): TaxSettingEntity {
    return TaxSettingEntity(
        id = id,
        isActive = isActive,
        percentage = percentage,
        type = type,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}
