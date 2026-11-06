package com.ptpws.ikikasir.feature.pengaturan.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import java.io.File

/**
 * Data layer: SharedPreferences-based repository for nota/receipt settings.
 * Clean Architecture — Data Layer (English Keys).
 */
class NotaSettingPreferences(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSetting(): NotaSetting {
        val logoUrl = prefs.getString(KEY_LOGO_URL, "") ?: ""
        val savedLocalPath = prefs.getString(KEY_LOGO_LOCAL_PATH, "") ?: ""
        val legacyLocalPath = logoUrl.takeIf {
            Uri.parse(it).scheme.equals("file", ignoreCase = true)
        }.orEmpty()
        val recoveredLocalPath = if (
            savedLocalPath.isBlank() &&
            legacyLocalPath.isBlank() &&
            logoUrl.startsWith("http", ignoreCase = true)
        ) {
            File(appContext.filesDir, "store_logo")
                .listFiles()
                ?.filter { it.isFile && it.extension.equals("png", ignoreCase = true) }
                ?.maxByOrNull(File::lastModified)
                ?.let(Uri::fromFile)
                ?.toString()
                .orEmpty()
        } else {
            ""
        }
        return NotaSetting(
            storeName = prefs.getString(KEY_STORE_NAME, "") ?: "",
            storeAddress = prefs.getString(KEY_STORE_ADDRESS, "") ?: "",
            storePhone = prefs.getString(KEY_STORE_PHONE, "") ?: "",
            wifiName = prefs.getString(KEY_WIFI_NAME, "") ?: "",
            wifiPassword = prefs.getString(KEY_WIFI_PASSWORD, "") ?: "",
            paperWidth = prefs.getString(KEY_PAPER_WIDTH, "58mm") ?: "58mm",
            logoUrl = logoUrl,
            logoLocalPath = savedLocalPath.ifBlank { legacyLocalPath.ifBlank { recoveredLocalPath } }
        )
    }

    fun saveSetting(setting: NotaSetting) {
        prefs.edit()
            .putString(KEY_STORE_NAME,    setting.storeName)
            .putString(KEY_STORE_ADDRESS, setting.storeAddress)
            .putString(KEY_STORE_PHONE,   setting.storePhone)
            .putString(KEY_WIFI_NAME,     setting.wifiName)
            .putString(KEY_WIFI_PASSWORD, setting.wifiPassword)
            .putString(KEY_PAPER_WIDTH,   setting.paperWidth)
            .putString(KEY_LOGO_URL,      setting.logoUrl)
            .putString(KEY_LOGO_LOCAL_PATH, setting.logoLocalPath)
            .apply()
    }

    companion object {
        private const val PREFS_NAME        = "ikikasir_nota_setting"
        private const val KEY_STORE_NAME     = "store_name"
        private const val KEY_STORE_ADDRESS  = "store_address"
        private const val KEY_STORE_PHONE    = "store_phone"
        private const val KEY_WIFI_NAME      = "wifi_name"
        private const val KEY_WIFI_PASSWORD  = "wifi_password"
        private const val KEY_PAPER_WIDTH    = "paper_width"
        private const val KEY_LOGO_URL       = "logo_url"
        private const val KEY_LOGO_LOCAL_PATH = "logo_local_path"
    }
}
