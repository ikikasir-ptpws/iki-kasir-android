package com.ptpws.ikikasir.core.responsive.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.screens.navigation.AppScreen

private val PrimaryRoyalBlue = Color(0xFF3B32D1)

/**
 * Navigation Rail untuk Tablet Menengah (600dp - 1199dp).
 * Menampilkan ikon vertikal ramping di sisi kiri layar.
 */
@Composable
fun IkiKasirNavigationRail(
    visibleScreens: List<AppScreen>,
    currentRoute: String?,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = Color.White,
        contentColor = Color(0xFF0F172A),
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconapk),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        visibleScreens.forEach { screen ->
            val isSelected = if (screen == AppScreen.Produk) {
                currentRoute?.startsWith(AppScreen.Produk.baseRoute) == true
            } else {
                currentRoute == screen.route
            }

            val icon = getScreenIcon(screen)

            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(screen.title),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(screen.title),
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = PrimaryRoyalBlue,
                    selectedTextColor = PrimaryRoyalBlue,
                    indicatorColor = Color(0xFFEEF2FF),
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF64748B)
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

/**
 * Mapping Icon dari AppScreen
 */
private fun getScreenIcon(screen: AppScreen): ImageVector {
    return when (screen) {
        AppScreen.Dashboard -> Icons.Outlined.Home
        AppScreen.Produk -> Icons.Outlined.Inventory2
        AppScreen.Riwayat -> Icons.Outlined.History
        AppScreen.Kasir -> Icons.Outlined.PointOfSale
        AppScreen.Profil -> Icons.Outlined.Person
        else -> Icons.Outlined.Home
    }
}
