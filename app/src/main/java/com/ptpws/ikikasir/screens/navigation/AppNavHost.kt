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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.app.ui.screen.DaftarProdukScreen
import com.ptpws.ikikasir.commond.interfamily
import com.example.app.ui.screen.DashboardScreen
import com.example.app.ui.screen.MenuFullScreen
import com.ptpws.ikikasir.screens.barangrusak.BarangRusakExpScreen
import com.ptpws.ikikasir.screens.barangrusak.TambahRusakExpActivity
import com.ptpws.ikikasir.screens.kategori.DaftarKategoriScreen
import com.ptpws.ikikasir.screens.manajemenstok.ManajemenStokScreen
import com.ptpws.ikikasir.screens.pengaturan.ProfilScreen
import com.ptpws.ikikasir.screens.penjualan.RiwayatTransaksiScreen
import com.ptpws.ikikasir.screens.kategori.TambahKategoriActivity
import com.ptpws.ikikasir.screens.keuangan.AuditLogScreen
import com.ptpws.ikikasir.screens.keuangan.LaporanKeuanganScreen
import com.ptpws.ikikasir.screens.keuangan.ManajemenHutangScreen
import com.ptpws.ikikasir.screens.manajemenpengguna.ManajemenPenggunaScreen
import com.ptpws.ikikasir.screens.pengaturan.PengaturanDashboardScreen
import com.ptpws.ikikasir.screens.penjualan.DetailTransaksiActivity
import com.ptpws.ikikasir.screens.penjualan.DetailTransaksiScreen
import com.ptpws.ikikasir.screens.penjualan.LaporanPenjualanScreen
import com.ptpws.ikikasir.screens.produk.TambahProdukActivity
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
                DashboardScreen(navController)
            }

            composable(AppScreen.Semuamenu.route) {
                MenuFullScreen(navController)
            }
            composable(AppScreen.Kasir.route) {
                KasirScreen(navController)
            }
            composable(AppScreen.Produk.route) {
                val context = LocalContext.current
                DaftarProdukScreen(
                    navController = navController,
                    onTambah = {
                        context.startActivity(Intent(context, TambahProdukActivity::class.java))
                    }
                )
            }
            composable(AppScreen.Riwayat.route) {
                val context = LocalContext.current
                RiwayatTransaksiScreen(navController, onDetailTransaksi = { context.startActivity(Intent(context, DetailTransaksiActivity::class.java)) })
            }
            composable(AppScreen.Profil.route) {
                val context = LocalContext.current
                ProfilScreen(
                    navController = navController,
                    onKeluar = {
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        auth.signOut()
                        context.startActivity(Intent(context, com.ptpws.ikikasir.feature.auth.presentation.screen.AuthActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                )
            }
            composable(AppScreen.KategoriProduk.route) {
                val context = LocalContext.current
                DaftarKategoriScreen(navController,
                    onTambahPromo = { context.startActivity(Intent(context, TambahKategoriActivity::class.java)) } )
            }
            composable(AppScreen.ManajemenStok.route) {
                ManajemenStokScreen(navController)
            }
            composable(AppScreen.BarangRusakExp.route) {
                val context = LocalContext.current
                BarangRusakExpScreen(navController, onTambahBarang = { context.startActivity(Intent(context,
                    TambahRusakExpActivity::class.java)) })
            }
            composable(AppScreen.DetailTransaksi.route) {
                DetailTransaksiScreen()
            }
            composable(AppScreen.LaporanKeuangan.route) {
                LaporanKeuanganScreen(navController
                )
            }
            composable(AppScreen.Hutang.route) {
                ManajemenHutangScreen(navController)
            }
            composable(AppScreen.AuditLog.route) {
                AuditLogScreen(navController)
            }
            composable(AppScreen.LaporanPenjualan.route) {
                LaporanPenjualanScreen(navController)
            }
            composable(AppScreen.Pengguna.route) {
                ManajemenPenggunaScreen(navController)
            }
            composable(AppScreen.PengaturanMenu.route) {
                PengaturanDashboardScreen(navController)
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
                        if (currentRoute == screen.route) return@BottomNavItem
                        
                        if (screen.route == AppScreen.Dashboard.route) {
                            navController.popBackStack(AppScreen.Dashboard.route, inclusive = false)
                        } else {
                            navController.navigate(screen.route) {
                                val startRoute = navController.graph.findStartDestination().route
                                if (startRoute != null) {
                                    popUpTo(startRoute) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
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
