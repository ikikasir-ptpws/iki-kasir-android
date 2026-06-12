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
// Daftar item bottom navigation
val bottomNavItems = listOf(
    AppScreen.Dashboard,
    AppScreen.Kasir,
    AppScreen.Produk,
    AppScreen.Riwayat,
    AppScreen.Profil
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rute-rute yang menampilkan bottom bar
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                IkiKasirBottomBar(navController = navController, currentRoute = currentRoute)
            }
        },
        containerColor = Color(0xFFF4F6FA)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppScreen.Dashboard.route) {
//                HomeScreen(
//                    onLihatSemuaMenu = {
//                        navController.navigate(AppScreen.CariMenu.route)
//                    }
//                )
            }
            composable(AppScreen.Kasir.route) {
                PlaceholderScreen("Kasir")
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
//            composable(AppScreen.CariMenu.route) {
//                MenuFullScreen(
//                    navController = navController
//                )
//            }
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
    }
}

@Composable
fun IkiKasirBottomBar(navController: NavController, currentRoute: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                BottomNavItem(
                    screen = screen,
                    isSelected = isSelected,
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
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
//    val iconRes = when (screen) {
//        AppScreen.Dashboard -> R.drawable.ic_nav_home
//        AppScreen.Kasir     -> R.drawable.ic_nav_kasir
//        AppScreen.Produk    -> R.drawable.ic_nav_produk
//        AppScreen.Riwayat   -> R.drawable.ic_nav_riwayat
//        AppScreen.Profil    -> R.drawable.ic_nav_profil
//        else                -> R.drawable.ic_nav_home
//    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFEEF2FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
//                Icon(
//                    painter = painterResource(iconRes),
//                    contentDescription = stringResource(screen.title),
//                    tint = Color(0xFF4F46E5),
//                    modifier = Modifier.size(22.dp)
//                )
            }
        } else {
//            Icon(
//                painter = painterResource(iconRes),
//                contentDescription = stringResource(screen.title),
//                tint = Color(0xFF9CA3AF),
//                modifier = Modifier.size(22.dp)
//            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(screen.title),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = interfamily,
            color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF9CA3AF)
        )
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
