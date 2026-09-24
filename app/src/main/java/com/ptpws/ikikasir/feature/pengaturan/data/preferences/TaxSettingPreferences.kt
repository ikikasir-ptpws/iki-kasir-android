package com.ptpws.ikikasir.feature.pengaturan.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting

class TaxSettingPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("TaxSettingPrefs", Context.MODE_PRIVATE)

    fun getSetting(): TaxSetting {
        val isActive = prefs.getBoolean("isActive", true)
        val percentage = prefs.getFloat("percentage", 11.0f).toDouble()
        val type = prefs.getString("type", TaxSetting.TAX_TYPE_EXCLUSIVE) ?: TaxSetting.TAX_TYPE_EXCLUSIVE

        return TaxSetting(
            id = "default",
            isActive = isActive,
            percentage = percentage,
            type = type
        )
    }

    fun saveSetting(setting: TaxSetting) {
        prefs.edit()
            .putBoolean("isActive", setting.isActive)
            .putFloat("percentage", setting.percentage.toFloat())
            .putString("type", setting.type)
            .apply()
    }
}
