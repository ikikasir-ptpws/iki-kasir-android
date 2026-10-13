package com.ptpws.ikikasir.core.responsive.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ptpws.ikikasir.core.responsive.domain.model.ResponsiveDimensions

/**
 * Reusable Container Responsif Jetpack Compose untuk aplikasi Iki Kasir.
 * Menggunakan BoxWithConstraints untuk menghitung dimensi layar yang tersedia secara real-time,
 * menerapkan padding horizontal adaptif, serta mendukung scrolling vertikal untuk mencegah
 * elemen terpotong ketika font diperbesar atau keyboard terbuka.
 */
@Composable
fun ResponsiveScreenContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    enableScroll: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalPadding: Dp = 16.dp,
    content: @Composable ColumnScope.(screenWidth: Dp, screenHeight: Dp) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val horizontalPadding = ResponsiveDimensions.getAdaptiveHorizontalPadding(screenWidth)

        val columnModifier = Modifier
            .fillMaxSize()
            .then(
                if (enableScroll) {
                    Modifier.verticalScroll(rememberScrollState())
                } else {
                    Modifier
                }
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .imePadding()

        Column(
            modifier = columnModifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            content(screenWidth, screenHeight)
        }
    }
}
