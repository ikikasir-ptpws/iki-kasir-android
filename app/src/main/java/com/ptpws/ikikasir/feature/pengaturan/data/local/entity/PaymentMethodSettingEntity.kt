package com.ptpws.ikikasir.feature.pengaturan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting

@Entity(tableName = "payment_method_settings")
data class PaymentMethodSettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "default",

    @ColumnInfo(name = "isTunaiEnabled")
    val isTunaiEnabled: Boolean = true,

    @ColumnInfo(name = "isQrisEnabled")
    val isQrisEnabled: Boolean = true,

    @ColumnInfo(name = "isTransferEnabled")
    val isTransferEnabled: Boolean = true,

    @ColumnInfo(name = "isKartuKreditEnabled")
    val isKartuKreditEnabled: Boolean = true,

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false
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

    companion object {
        fun fromDomain(domain: PaymentMethodSetting, isSynced: Boolean = false): PaymentMethodSettingEntity {
            return PaymentMethodSettingEntity(
                id = domain.id,
                isTunaiEnabled = domain.isTunaiEnabled,
                isQrisEnabled = domain.isQrisEnabled,
                isTransferEnabled = domain.isTransferEnabled,
                isKartuKreditEnabled = domain.isKartuKreditEnabled,
                updatedAt = domain.updatedAt,
                isSynced = isSynced
            )
        }
    }
}
