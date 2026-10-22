package com.ptpws.ikikasir.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun IKIKASIRTheme(
    darkTheme: Boolean = false,
    // Dynamic color dinonaktifkan agar warna tema aplikasi konsisten & tidak terpengaruh setelan sistem
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val context = LocalContext.current
    val currentDensity = androidx.compose.ui.platform.LocalDensity.current
    val currentConfig = androidx.compose.ui.platform.LocalConfiguration.current

    // Kunci fontScale pada 1.0f dan density pada standar perangkat agar ukuran font & tampilan tetap normal
    val fixedDensity = androidx.compose.runtime.remember(context, currentDensity.density) {
        com.ptpws.ikikasir.core.responsive.presentation.util.DisplaySizeConfig.getFixedDensity(context)
    }

    val fixedConfig = androidx.compose.runtime.remember(context, currentConfig) {
        val cfg = android.content.res.Configuration(currentConfig)
        com.ptpws.ikikasir.core.responsive.presentation.util.DisplaySizeConfig.applyFixedConfiguration(cfg, context)
        cfg
    }

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalDensity provides fixedDensity,
        androidx.compose.ui.platform.LocalConfiguration provides fixedConfig
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}