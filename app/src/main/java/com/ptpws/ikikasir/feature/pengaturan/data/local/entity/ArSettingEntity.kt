package com.ptpws.ikikasir.feature.pengaturan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.pengaturan.domain.model.ArSetting

@Entity(tableName = "ar_settings")
data class ArSettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "default",

    @ColumnInfo(name = "isArEnabled")
    val isArEnabled: Boolean = false,

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false
) {
    fun toDomain(): ArSetting {
        return ArSetting(
            id = id,
            isArEnabled = isArEnabled,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }

    companion object {
        fun fromDomain(domain: ArSetting, isSynced: Boolean = false): ArSettingEntity {
            return ArSettingEntity(
                id = domain.id,
                isArEnabled = domain.isArEnabled,
                updatedAt = domain.updatedAt,
                isSynced = isSynced
            )
        }
    }
}
