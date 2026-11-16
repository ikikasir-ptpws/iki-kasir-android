package com.ptpws.ikikasir.feature.pengaturan.data.preferences

import android.content.Context
import android.content.SharedPreferences

class ArSettingPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ArSettingPrefs", Context.MODE_PRIVATE)

    fun isArEnabled(): Boolean {
        return prefs.getBoolean("isArEnabled", false)
    }

    fun setArEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("isArEnabled", enabled).apply()
    }
}
