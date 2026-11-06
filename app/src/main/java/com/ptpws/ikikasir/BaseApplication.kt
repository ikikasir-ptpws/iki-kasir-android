package com.ptpws.ikikasir

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.ptpws.ikikasir.core.responsive.presentation.util.DisplaySizeConfig
import com.ptpws.ikikasir.feature.referral.data.preferences.ReferralSessionManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }

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
