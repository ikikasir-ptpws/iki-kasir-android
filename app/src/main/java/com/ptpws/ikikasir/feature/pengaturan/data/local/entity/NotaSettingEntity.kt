package com.ptpws.ikikasir.feature.pengaturan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting

@Entity(tableName = "nota_settings")
data class NotaSettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "default_nota_setting",

    @ColumnInfo(name = "storeName")
    val storeName: String = "",

    @ColumnInfo(name = "storeAddress")
    val storeAddress: String = "",

    @ColumnInfo(name = "wifiName")
    val wifiName: String = "",

    @ColumnInfo(name = "wifiPassword")
    val wifiPassword: String = "",

    @ColumnInfo(name = "paperWidth")
    val paperWidth: String = "58mm",

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true
) {
    fun toDomain(): NotaSetting {
        return NotaSetting(
            id = id,
            storeName = storeName,
            storeAddress = storeAddress,
            wifiName = wifiName,
            wifiPassword = wifiPassword,
            paperWidth = paperWidth,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }
}

fun NotaSetting.toEntity(isSynced: Boolean = this.isSynced): NotaSettingEntity {
    return NotaSettingEntity(
        id = id,
        storeName = storeName,
        storeAddress = storeAddress,
        wifiName = wifiName,
        wifiPassword = wifiPassword,
        paperWidth = paperWidth,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}
