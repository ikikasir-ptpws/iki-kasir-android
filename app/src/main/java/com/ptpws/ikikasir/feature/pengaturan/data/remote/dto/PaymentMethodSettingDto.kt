package com.ptpws.ikikasir.feature.pengaturan.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting

data class PaymentMethodSettingDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "default",

    @get:PropertyName("isTunaiEnabled") @set:PropertyName("isTunaiEnabled")
    var isTunaiEnabled: Boolean = true,

    @get:PropertyName("isQrisEnabled") @set:PropertyName("isQrisEnabled")
    var isQrisEnabled: Boolean = true,

    @get:PropertyName("isTransferEnabled") @set:PropertyName("isTransferEnabled")
    var isTransferEnabled: Boolean = true,

    @get:PropertyName("isKartuKreditEnabled") @set:PropertyName("isKartuKreditEnabled")
    var isKartuKreditEnabled: Boolean = true,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Timestamp = Timestamp.now()
) {
    fun toDomain(): PaymentMethodSetting {
        return PaymentMethodSetting(
            id = id,
            isTunaiEnabled = isTunaiEnabled,
            isQrisEnabled = isQrisEnabled,
            isTransferEnabled = isTransferEnabled,
            isKartuKreditEnabled = isKartuKreditEnabled,
            updatedAt = updatedAt
        )
    }
}

fun PaymentMethodSetting.toDto(): PaymentMethodSettingDto {
    return PaymentMethodSettingDto(
        id = id,
        isTunaiEnabled = isTunaiEnabled,
        isQrisEnabled = isQrisEnabled,
        isTransferEnabled = isTransferEnabled,
        isKartuKreditEnabled = isKartuKreditEnabled,
        updatedAt = updatedAt
    )
}
