package com.ptpws.ikikasir.commond

import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import com.ptpws.ikikasir.core.responsive.presentation.util.DisplaySizeConfig

/**
 * BaseActivity yang memastikan semua Activity di aplikasi Iki Kasir memiliki
 * skala tampilan (Display Size) dan ukuran font yang normal serta terkunci pada
 * standar perangkat, tidak terpengaruh oleh setelan ukuran tampilan atau font di HP/Tablet.
 */
open class BaseActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val fixedContext = DisplaySizeConfig.getFixedContext(newBase)
        super.attachBaseContext(fixedContext)
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            DisplaySizeConfig.applyFixedConfiguration(overrideConfiguration, this)
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}
