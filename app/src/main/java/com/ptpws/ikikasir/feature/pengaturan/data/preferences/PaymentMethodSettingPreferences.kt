package com.ptpws.ikikasir.feature.pengaturan.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting

class PaymentMethodSettingPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("PaymentMethodSettingPrefs", Context.MODE_PRIVATE)

    fun getSetting(): PaymentMethodSetting {
        return PaymentMethodSetting(
            id = "default",
            isTunaiEnabled = prefs.getBoolean("isTunaiEnabled", true),
            isQrisEnabled = prefs.getBoolean("isQrisEnabled", true),
            isTransferEnabled = prefs.getBoolean("isTransferEnabled", true),
            isKartuKreditEnabled = prefs.getBoolean("isKartuKreditEnabled", true)
        )
    }

    fun saveSetting(setting: PaymentMethodSetting) {
        prefs.edit()
            .putBoolean("isTunaiEnabled", setting.isTunaiEnabled)
            .putBoolean("isQrisEnabled", setting.isQrisEnabled)
            .putBoolean("isTransferEnabled", setting.isTransferEnabled)
            .putBoolean("isKartuKreditEnabled", setting.isKartuKreditEnabled)
            .apply()
    }
}
