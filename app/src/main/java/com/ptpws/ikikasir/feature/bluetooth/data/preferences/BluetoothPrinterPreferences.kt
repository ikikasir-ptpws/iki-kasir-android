package com.ptpws.ikikasir.feature.bluetooth.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting

class BluetoothPrinterPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("BluetoothPrinterPrefs", Context.MODE_PRIVATE)

    fun getSetting(): BluetoothPrinterSetting {
        return BluetoothPrinterSetting(
            id = "default",
            savedAddress = prefs.getString("savedAddress", "") ?: "",
            savedName = prefs.getString("savedName", "") ?: "",
            isAutoConnect = prefs.getBoolean("isAutoConnect", false),
            paperWidth = prefs.getString("paperWidth", BluetoothPrinterSetting.PAPER_58MM)
                ?: BluetoothPrinterSetting.PAPER_58MM
        )
    }

    fun saveSetting(setting: BluetoothPrinterSetting) {
        prefs.edit()
            .putString("savedAddress", setting.savedAddress)
            .putString("savedName", setting.savedName)
            .putBoolean("isAutoConnect", setting.isAutoConnect)
            .putString("paperWidth", setting.paperWidth)
            .apply()
    }

    fun getSavedAddress(): String = prefs.getString("savedAddress", "") ?: ""
    fun getSavedName(): String = prefs.getString("savedName", "") ?: ""

    fun isAutoPrint(): Boolean = prefs.getBoolean("isAutoPrint", false)
    fun setAutoPrint(enabled: Boolean) {
        prefs.edit().putBoolean("isAutoPrint", enabled).apply()
    }
}
