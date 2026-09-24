package com.ptpws.ikikasir.feature.pengaturan.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting

data class NotaSettingDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "default_nota_setting",

    @get:PropertyName("storeName") @set:PropertyName("storeName")
    var storeName: String = "",

    @get:PropertyName("storeAddress") @set:PropertyName("storeAddress")
    var storeAddress: String = "",

    @get:PropertyName("wifiName") @set:PropertyName("wifiName")
    var wifiName: String = "",

    @get:PropertyName("wifiPassword") @set:PropertyName("wifiPassword")
    var wifiPassword: String = "",

    @get:PropertyName("paperWidth") @set:PropertyName("paperWidth")
    var paperWidth: String = "58mm",

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Timestamp = Timestamp.now()
) {
    fun toDomain(): NotaSetting {
        return NotaSetting(
            id = id.ifBlank { "default_nota_setting" },
            storeName = storeName,
            storeAddress = storeAddress,
            wifiName = wifiName,
            wifiPassword = wifiPassword,
            paperWidth = paperWidth,
            updatedAt = updatedAt,
            isSynced = true
        )
    }
}

fun NotaSetting.toDto(): NotaSettingDto {
    return NotaSettingDto(
        id = id,
        storeName = storeName,
        storeAddress = storeAddress,
        wifiName = wifiName,
        wifiPassword = wifiPassword,
        paperWidth = paperWidth,
        updatedAt = updatedAt
    )
}
