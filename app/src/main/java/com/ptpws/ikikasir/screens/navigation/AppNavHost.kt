package com.ptpws.ikikasir.screens.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.example.app.ui.screen.DashboardScreen
import com.ptpws.ikikasir.ui.screens.kasir.KasirScreen

// Daftar item bottom navigation


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rute-rute yang menampilkan bottom bar
    val showBottomBar = currentRoute in bottomNavItems.map { it.route } && currentRoute != AppScreen.Kasir.route

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = AppScreen.Dashboard.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(AppScreen.Dashboard.route) {
                DashboardScreen()
            }
            composable(AppScreen.Kasir.route) {
                KasirScreen(navController)
            }
            composable(AppScreen.Produk.route) {
                PlaceholderScreen("Produk")
            }
            composable(AppScreen.Riwayat.route) {
                PlaceholderScreen("Riwayat")
            }
            composable(AppScreen.Profil.route) {
                PlaceholderScreen("Profil")
            }
            composable(AppScreen.KategoriProduk.route) {
                PlaceholderScreen("Kategori Produk")
            }
            composable(AppScreen.ManajemenStok.route) {
                PlaceholderScreen("Manajemen Stok")
            }
            composable(AppScreen.BarangRusakExp.route) {
                PlaceholderScreen("Barang Rusak & Exp")
            }
            composable(AppScreen.Transaksi.route) {
                PlaceholderScreen("Transaksi")
            }
            composable(AppScreen.LaporanKeuangan.route) {
                PlaceholderScreen("Laporan Keuangan")
            }
            composable(AppScreen.Hutang.route) {
                PlaceholderScreen("Hutang")
            }
            composable(AppScreen.AuditLog.route) {
                PlaceholderScreen("Audit Log")
            }
            composable(AppScreen.LaporanPenjualan.route) {
                PlaceholderScreen("Laporan Penjualan")
            }
            composable(AppScreen.Pengguna.route) {
                PlaceholderScreen("Pengguna")
            }
            composable(AppScreen.PengaturanMenu.route) {
                PlaceholderScreen("Pengaturan Menu")
            }
        }

        AnimatedVisibility(
            visible = showBottomBar,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            IkiKasirBottomBar(navController = navController, currentRoute = currentRoute)
        }
    }
}


val bottomNavItems = listOf(
    AppScreen.Dashboard,
    AppScreen.Produk,
    AppScreen.Kasir,
    AppScreen.Riwayat,
    AppScreen.Profil
)


@Composable
fun IkiKasirBottomBar(navController: NavController, currentRoute: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp) // floating effect
    ) {
        // White pill background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(32.dp),
                    clip = false
                )
                .background(Color.White, RoundedCornerShape(32.dp))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val isCenter = screen == AppScreen.Kasir

                BottomNavItem(
                    screen = screen,
                    isSelected = isSelected,
                    isCenter = isCenter,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    screen: AppScreen,
    isSelected: Boolean,
    isCenter: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val icon = when (screen) {
        AppScreen.Dashboard -> Icons.Outlined.Home
        AppScreen.Produk    -> Icons.Outlined.Inventory2
        AppScreen.Riwayat   -> Icons.Outlined.History
        AppScreen.Kasir     -> Icons.Outlined.PointOfSale
        AppScreen.Profil    -> Icons.Outlined.Person
        else                -> Icons.Outlined.Home
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .then(if (!isCenter) Modifier.clip(RoundedCornerShape(12.dp)) else Modifier)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        when {
            isCenter -> {
                Box(
                    modifier = Modifier
                        .offset(y = (-16).dp)
                        .size(60.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .background(Color(0xFF4F46E5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(screen.title),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            isSelected -> {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEEF2FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(screen.title),
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(screen.title),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = interfamily,
                    color = Color(0xFF4F46E5)
                )
            }

            else -> {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(screen.title),
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(screen.title),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = interfamily,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interfamily,
            color = Color(0xFF374151)
        )
    }
}
