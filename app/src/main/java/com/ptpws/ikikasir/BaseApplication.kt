package com.ptpws.ikikasir

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = resources.configuration
        if (config.fontScale != 1.0f) {
            config.fontScale = 1.0f
            val metrics = resources.displayMetrics
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, metrics)
        }
    }
}
