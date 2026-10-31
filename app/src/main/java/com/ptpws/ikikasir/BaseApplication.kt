package com.ptpws.ikikasir

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.ptpws.ikikasir.core.responsive.presentation.util.DisplaySizeConfig
import com.ptpws.ikikasir.feature.referral.data.preferences.ReferralSessionManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        DisplaySizeConfig.applyToResources(resources)
        // Token referral global pelanggan dimuat dari penyimpanan lokal
        // sehingga tetap tersedia walau server sedang tidak dapat dijangkau.
        ReferralSessionManager.init(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        DisplaySizeConfig.applyToResources(resources)
    }
}
