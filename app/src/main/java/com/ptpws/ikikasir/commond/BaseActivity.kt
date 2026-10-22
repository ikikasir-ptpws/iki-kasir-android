package com.ptpws.ikikasir.commond

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import com.ptpws.ikikasir.core.responsive.presentation.util.DisplaySizeConfig

/**
 * BaseActivity yang memastikan semua Activity di aplikasi Iki Kasir memiliki
 * skala tampilan (Display Size) dan ukuran font yang normal serta terkunci pada
 * standar perangkat, tidak terpengaruh oleh setelan ukuran tampilan, font, maupun mode gelap di HP/Tablet.
 */
open class BaseActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val fixedContext = DisplaySizeConfig.getFixedContext(newBase)
        super.attachBaseContext(fixedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            DisplaySizeConfig.applyFixedConfiguration(overrideConfiguration, this)
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}
