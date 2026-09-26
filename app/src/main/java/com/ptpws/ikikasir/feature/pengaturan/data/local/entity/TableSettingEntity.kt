package com.ptpws.ikikasir.feature.pengaturan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting

@Entity(tableName = "table_settings")
data class TableSettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "default",

    @ColumnInfo(name = "isTableEnabled")
    val isTableEnabled: Boolean = true,

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false
) {
    fun toDomain(): TableSetting {
        return TableSetting(
            id = id,
            isTableEnabled = isTableEnabled,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(domain: TableSetting, isSynced: Boolean = false): TableSettingEntity {
            return TableSettingEntity(
                id = domain.id,
                isTableEnabled = domain.isTableEnabled,
                updatedAt = domain.updatedAt,
                isSynced = isSynced
            )
        }
    }
}
