package com.ptpws.ikikasir

import android.app.Application
import android.content.res.Configuration
import com.ptpws.ikikasir.core.responsive.presentation.util.DisplaySizeConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DisplaySizeConfig.applyToResources(resources)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        DisplaySizeConfig.applyToResources(resources)
    }
}
