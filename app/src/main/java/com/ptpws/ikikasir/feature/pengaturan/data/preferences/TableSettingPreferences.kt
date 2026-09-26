package com.ptpws.ikikasir.feature.pengaturan.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting

class TableSettingPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("TableSettingPrefs", Context.MODE_PRIVATE)

    fun getSetting(): TableSetting {
        return TableSetting(
            id = "default",
            isTableEnabled = prefs.getBoolean("isTableEnabled", false)
        )
    }

    fun saveSetting(setting: TableSetting) {
        prefs.edit()
            .putBoolean("isTableEnabled", setting.isTableEnabled)
            .apply()
    }

    fun isTableEnabled(): Boolean {
        return prefs.getBoolean("isTableEnabled", false)
    }

    fun setTableEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean("isTableEnabled", enabled)
            .apply()
    }
}
