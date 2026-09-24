package com.ptpws.ikikasir.feature.pengaturan.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting

/**
 * Data layer: SharedPreferences-based repository for nota/receipt settings.
 * Clean Architecture — Data Layer (English Keys).
 */
class NotaSettingPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSetting(): NotaSetting = NotaSetting(
        storeName    = prefs.getString(KEY_STORE_NAME,    "") ?: "",
        storeAddress = prefs.getString(KEY_STORE_ADDRESS, "") ?: "",
        wifiName     = prefs.getString(KEY_WIFI_NAME,     "") ?: "",
        wifiPassword = prefs.getString(KEY_WIFI_PASSWORD, "") ?: "",
        paperWidth   = prefs.getString(KEY_PAPER_WIDTH,  "58mm") ?: "58mm"
    )

    fun saveSetting(setting: NotaSetting) {
        prefs.edit()
            .putString(KEY_STORE_NAME,    setting.storeName)
            .putString(KEY_STORE_ADDRESS, setting.storeAddress)
            .putString(KEY_WIFI_NAME,     setting.wifiName)
            .putString(KEY_WIFI_PASSWORD, setting.wifiPassword)
            .putString(KEY_PAPER_WIDTH,   setting.paperWidth)
            .apply()
    }

    companion object {
        private const val PREFS_NAME        = "ikikasir_nota_setting"
        private const val KEY_STORE_NAME     = "store_name"
        private const val KEY_STORE_ADDRESS  = "store_address"
        private const val KEY_WIFI_NAME      = "wifi_name"
        private const val KEY_WIFI_PASSWORD  = "wifi_password"
        private const val KEY_PAPER_WIDTH    = "paper_width"
    }
}
