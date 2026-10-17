package com.ptpws.ikikasir.screens.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.screens.produk.DaftarProdukScreen
import com.ptpws.ikikasir.screens.produk.DetailProdukScreen
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.commond.GlobalCrudResultDialogHost
import com.ptpws.ikikasir.commond.GlobalUploadProgressHost
import com.example.app.ui.screen.DashboardScreen
import com.example.app.ui.screen.DashboardViewModel
import com.example.app.ui.screen.MenuFullScreen
import com.ptpws.ikikasir.core.responsive.presentation.components.IkiKasirNavigationRail
import com.ptpws.ikikasir.feature.sidebar.presentation.components.TabletSidebarDrawer
import com.ptpws.ikikasir.core.responsive.presentation.util.LocalWindowSizeInfo
import com.ptpws.ikikasir.screens.kategori.DaftarKategoriScreen
import com.ptpws.ikikasir.screens.manajemenstok.ManajemenStokScreen
import com.ptpws.ikikasir.screens.manajemenstok.UpdateStokScreen

import com.ptpws.ikikasir.feature.bluetooth.presentation.screen.BluetoothPrinterScreen
import com.ptpws.ikikasir.feature.bluetooth.presentation.viewmodel.BluetoothPrinterViewModel
import com.ptpws.ikikasir.screens.pengaturan.ProfilScreen
import com.ptpws.ikikasir.screens.penjualan.RiwayatTransaksiScreen
import com.ptpws.ikikasir.screens.kategori.TambahKategoriActivity
import com.ptpws.ikikasir.screens.keuangan.AuditLogScreen
import com.ptpws.ikikasir.screens.keuangan.LaporanKeuanganScreen
import com.ptpws.ikikasir.feature.laporanpenjualan.presentation.screen.LaporanPenjualanScreen
import com.ptpws.ikikasir.screens.manajemenpengguna.ManajemenPenggunaScreen
import com.ptpws.ikikasir.screens.manajemenpengguna.TambahPenggunaActivity
import com.ptpws.ikikasir.screens.pengaturan.PengaturanDashboardScreen
import com.ptpws.ikikasir.screens.penjualan.DetailTransaksiActivity
import com.ptpws.ikikasir.screens.penjualan.DetailTransaksiScreen
import com.ptpws.ikikasir.screens.produk.TambahProdukActivity
import com.ptpws.ikikasir.screens.penjualan.KasirScreen
import com.ptpws.ikikasir.screens.penjualan.PembayaranScreen
import com.ptpws.ikikasir.screens.penjualan.WaitingListScreen
import com.ptpws.ikikasir.screens.penjualan.RiwayatAntreanScreen
import com.ptpws.ikikasir.screens.promo.ManajemenPromoScreen
import com.ptpws.ikikasir.screens.promo.TambahPromoActivity

// Daftar item bottom navigation


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val bluetoothPrinterViewModel: BluetoothPrinterViewModel = hiltViewModel()
    val context = LocalContext.current
    val sessionState by dashboardViewModel.sessionState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(sessionState.user, sessionState.isLoading) {
        if (!sessionState.isLoading && sessionState.user == null) {
            context.startActivity(Intent(context, com.ptpws.ikikasir.feature.auth.presentation.screen.AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    LaunchedEffect(currentRoute) {
        // Safety guard: if backstack ever becomes empty from spammed back presses, automatically recover to Dashboard
        if (currentRoute == null && navController.currentBackStackEntry == null) {
            navController.navigate(AppScreen.Dashboard.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasBluetoothPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        bluetoothPrinterViewModel.updatePermissionStatus(hasBluetoothPermission)
    }

    val windowSizeInfo = LocalWindowSizeInfo.current
    val isWideScreen = windowSizeInfo.shouldUseNavigationRail || windowSizeInfo.shouldUsePermanentDrawer

    val showProduk = dashboardViewModel.isAllowed("Produk")
    val showKasir = dashboardViewModel.isAllowed("Kasir")
    val showRiwayat = dashboardViewModel.isAllowed("Transaksi") || dashboardViewModel.isAllowed("Riwayat Antrean") || dashboardViewModel.isAllowed("Antrean")

    val visibleNavItems = remember(showProduk, showKasir, showRiwayat) {
        mutableListOf<AppScreen>().apply {
            add(AppScreen.Dashboard)
            if (showProduk) add(AppScreen.Produk)
            if (showKasir) add(AppScreen.Kasir)
            if (showRiwayat) add(AppScreen.Riwayat)
            add(AppScreen.Profil)
        }
    }

    val onNavigateToRoute: (String) -> Unit = { targetRoute ->
        val cleanRoute = targetRoute.substringBefore("?")
        if (cleanRoute == AppScreen.Dashboard.route) {
            val popped = navController.popBackStack(AppScreen.Dashboard.route, inclusive = false)
            if (!popped) {
                navController.navigate(AppScreen.Dashboard.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else if (cleanRoute == AppScreen.Produk.baseRoute) {
            navController.navigate(AppScreen.Produk.baseRoute) {
                val startRoute = navController.graph.findStartDestination().route
                if (startRoute != null) {
                    popUpTo(startRoute) {
                        saveState = true
                    }
                }
                launchSingleTop = true
                restoreState = false
            }
        } else {
            navController.navigate(targetRoute) {
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

    val onNavigateToScreen: (AppScreen) -> Unit = { screen ->
        onNavigateToRoute(screen.route)
    }

    // Rute-rute yang menampilkan bottom bar pada mode ponsel
    val showBottomBar = (currentRoute in bottomNavItems.map { it.route } ||
            currentRoute?.startsWith(AppScreen.Produk.baseRoute) == true) &&
            currentRoute != AppScreen.Kasir.route &&
            currentRoute != AppScreen.Pembayaran.route

    val navHostContent: @Composable () -> Unit = {
        NavHost(
            navController = navController,
            startDestination = AppScreen.Dashboard.route,
            modifier = Modifier.fillMaxSize()
        ) {
                composable(AppScreen.Dashboard.route) {
                    DashboardScreen(navController, dashboardViewModel)
                }

            composable(AppScreen.Semuamenu.route) {
                MenuFullScreen(navController, dashboardViewModel)
            }
            composable(AppScreen.Kasir.route) {
                KasirScreen(
                    navController = navController,
                    onBayar = {
                        if (navController.currentDestination?.route?.substringBefore("?") == AppScreen.Kasir.route) {
                            navController.navigate(AppScreen.Pembayaran.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
            composable(AppScreen.Pembayaran.route) {
                PembayaranScreen(navController = navController)
            }
            composable(
                route = AppScreen.Produk.route,
                arguments = AppScreen.Produk.navArguments
            ) { backStackEntry ->
                val context = LocalContext.current
                val categoryIdArg = backStackEntry.arguments?.getString("categoryId")
                DaftarProdukScreen(
                    navController = navController,
                    initialCategoryId = categoryIdArg,
                    onTambah = {
                        context.startActivity(Intent(context, TambahProdukActivity::class.java))
                    }
                )
            }
            composable(
                route = AppScreen.DetailProduk.route,
                arguments = listOf(androidx.navigation.navArgument("produkId") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val produkId = backStackEntry.arguments?.getString("produkId")
                DetailProdukScreen(
                    onBack = { navController.popBackStackSafely(AppScreen.DetailProduk.route) },
                    produkId = produkId
                )
            }
            composable(AppScreen.Riwayat.route) {
                val context = LocalContext.current
                RiwayatTransaksiScreen(
                    navController = navController,
                    onDetailTransaksi = { txId ->
                        val intent = Intent(context, DetailTransaksiActivity::class.java).apply {
                            putExtra("TRANSACTION_ID", txId)
                        }
                        context.startActivity(intent)
                    }
                )
            }
            composable(AppScreen.Profil.route) {
                val context = LocalContext.current
                val authViewModel: com.ptpws.ikikasir.feature.auth.presentation.viewmodel.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                val authState by authViewModel.authState.collectAsState()

                LaunchedEffect(authState) {
                    if (authState is com.ptpws.ikikasir.feature.auth.presentation.viewmodel.AuthState.LoggedOut) {
                        context.startActivity(Intent(context, com.ptpws.ikikasir.feature.auth.presentation.screen.AuthActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                }

                ProfilScreen(
                    navController = navController,
                    onKeluar = {
                        authViewModel.logout()
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
            composable(
                route = AppScreen.UpdateStok.route,
                arguments = AppScreen.UpdateStok.navArguments
            ) {
                UpdateStokScreen(
                    onBack = { navController.popBackStackSafely(AppScreen.UpdateStok.route) },
                    onSuccessUpdate = { navController.popBackStackSafely(AppScreen.UpdateStok.route) }
                )
            }

            composable(AppScreen.DetailTransaksi.route) {
                DetailTransaksiScreen(
                    onBack = { navController.popBackStackSafely(AppScreen.DetailTransaksi.route) }
                )
            }
            composable(AppScreen.LaporanKeuangan.route) {
                LaporanKeuanganScreen(
                    navController = navController,
                    onBack = { navController.popBackStackSafely(AppScreen.LaporanKeuangan.route) }
                )
            }
            composable(AppScreen.AuditLog.route) {
                AuditLogScreen(navController)
            }
            composable(AppScreen.WaitingList.route) {
                WaitingListScreen(navController)
            }
            composable(AppScreen.RiwayatAntrean.route) {
                RiwayatAntreanScreen(navController)
            }
            composable(AppScreen.Promo.route) {
                val context = LocalContext.current
                ManajemenPromoScreen(
                    onBack = { navController.popBackStackSafely(AppScreen.Promo.route) },
                    onTambahPromo = {
                        context.startActivity(Intent(context, TambahPromoActivity::class.java))
                    },
                    onEditPromo = { promoId ->
                        val intent = Intent(context, TambahPromoActivity::class.java).apply {
                            putExtra("PROMO_ID", promoId)
                        }
                        context.startActivity(intent)
                    }
                )
            }
            composable(
                route = AppScreen.LaporanPenjualan.route,
                arguments = AppScreen.LaporanPenjualan.navArguments
            ) {
                LaporanPenjualanScreen(
                    navController = navController,
                    onBack = { navController.popBackStackSafely(AppScreen.LaporanPenjualan.route) }
                )
            }
            composable(AppScreen.Pengguna.route) {
                val context = LocalContext.current
                ManajemenPenggunaScreen(
                    navController = navController,
                    onTambahUser = {
                        context.startActivity(Intent(context, TambahPenggunaActivity::class.java))
                    }
                )
            }
            composable(AppScreen.PengaturanMenu.route) {
                PengaturanDashboardScreen(navController)
            }
            composable(AppScreen.BluetoothPrinter.route) {
                BluetoothPrinterScreen(navController)
            }
        }
    }

    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (windowSizeInfo.shouldUsePermanentDrawer) {
                TabletSidebarDrawer(
                    currentRoute = currentRoute,
                    onNavigate = onNavigateToRoute
                )
            } else {
                IkiKasirNavigationRail(
                    visibleScreens = visibleNavItems,
                    currentRoute = currentRoute,
                    onNavigate = onNavigateToScreen
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                navHostContent()
                GlobalCrudResultDialogHost()
                GlobalUploadProgressHost()
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            navHostContent()

            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                IkiKasirBottomBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    viewModel = dashboardViewModel
                )
            }

            GlobalCrudResultDialogHost()
            GlobalUploadProgressHost()
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
fun IkiKasirBottomBar(
    navController: NavController,
    currentRoute: String?,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val showProduk = viewModel.isAllowed("Produk")
    val showKasir = viewModel.isAllowed("Kasir")
    val showRiwayat = viewModel.isAllowed("Transaksi") || viewModel.isAllowed("Riwayat Antrean") || viewModel.isAllowed("Antrean")

    val visibleNavItems = remember(showProduk, showKasir, showRiwayat) {
        mutableListOf<AppScreen>().apply {
            add(AppScreen.Dashboard)
            if (showProduk) add(AppScreen.Produk)
            if (showKasir) add(AppScreen.Kasir)
            if (showRiwayat) add(AppScreen.Riwayat)
            add(AppScreen.Profil)
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isSmallScreen = screenWidth <= 340.dp
    val isTablet = screenWidth >= 600.dp

    // Penyesuaian margin dan dimensi responsive:
    // Pada HP: floating bar membentang seimbang dengan margin samping 14-18dp
    // Pada Tablet Portrait: floating bar membentang elegan (margin 24dp) dan membatasi lebar maksimal pada layar ekstra lebar (>840dp)
    val horizontalMargin = when {
        isSmallScreen -> 10.dp
        screenWidth <= 380.dp -> 14.dp
        screenWidth > 840.dp -> ((screenWidth - 840.dp) / 2) + 24.dp
        isTablet -> 24.dp
        else -> 18.dp
    }
    val windowSizeInfo = LocalWindowSizeInfo.current
    val isFontEnlarged = windowSizeInfo.isFontEnlarged

    val bottomMargin = when {
        isSmallScreen -> 10.dp
        isTablet -> 20.dp
        else -> 14.dp
    }
    val barHeight = when {
        isSmallScreen -> 74.dp
        isTablet -> 88.dp
        else -> 82.dp
    }
    val cornerRadius = when {
        isSmallScreen -> 28.dp
        isTablet -> 36.dp
        else -> 32.dp
    }
    val contentHorizontalPadding = when {
        isSmallScreen -> 10.dp
        isTablet -> 24.dp
        else -> 14.dp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = horizontalMargin, end = horizontalMargin, bottom = bottomMargin) // floating effect
    ) {
        // White pill background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    clip = false
                )
                .background(Color.White, RoundedCornerShape(cornerRadius))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .padding(horizontal = contentHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            visibleNavItems.forEach { screen ->
                // Untuk Produk, isSelected jika currentRoute diawali dengan "produk"
                val isSelected = if (screen == AppScreen.Produk) {
                    currentRoute?.startsWith(AppScreen.Produk.baseRoute) == true
                } else {
                    currentRoute == screen.route
                }
                val isCenter = screen == AppScreen.Kasir

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BottomNavItem(
                        screen = screen,
                        isSelected = isSelected,
                        isCenter = isCenter,
                        isSmallScreen = isSmallScreen,
                        isTablet = isTablet,
                        isFontEnlarged = isFontEnlarged,
                        onClick = {
                            if (isSelected && screen != AppScreen.Kasir) return@BottomNavItem

                            if (screen.route == AppScreen.Dashboard.route) {
                                val popped = navController.popBackStack(AppScreen.Dashboard.route, inclusive = false)
                                if (!popped) {
                                    navController.navigate(AppScreen.Dashboard.route) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } else if (screen == AppScreen.Produk) {
                                navController.navigate(AppScreen.Produk.baseRoute) {
                                    val startRoute = navController.graph.findStartDestination().route
                                    if (startRoute != null) {
                                        popUpTo(startRoute) {
                                            saveState = true
                                        }
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
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
}

@Composable
fun BottomNavItem(
    screen: AppScreen,
    isSelected: Boolean,
    isCenter: Boolean,
    isSmallScreen: Boolean = false,
    isTablet: Boolean = false,
    isFontEnlarged: Boolean = false,
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

    if (isCenter) {
        val fabSize = when {
            isSmallScreen -> 58.dp
            isTablet -> 70.dp
            else -> 64.dp
        }
        val fabIconSize = when {
            isSmallScreen -> 26.dp
            isTablet -> 32.dp
            else -> 28.dp
        }
        val fabOffset = when {
            isSmallScreen -> (-14).dp
            isTablet -> (-18).dp
            else -> (-16).dp
        }
        Box(
            modifier = Modifier
                .offset(y = fabOffset)
                .size(fabSize)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .background(Color(0xFF4F46E5), CircleShape)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.35f))
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(screen.title),
                tint = Color.White,
                modifier = Modifier.size(fabIconSize)
            )
        }
    } else {
        val circleSize = when {
            isSmallScreen -> 38.dp
            isTablet -> 48.dp
            else -> 42.dp
        }
        val iconSize = when {
            isSmallScreen -> 21.dp
            isTablet -> 26.dp
            else -> 23.dp
        }
        val fontSize = when {
            isSmallScreen -> 10.sp
            isTablet -> 13.sp
            else -> 11.5.sp
        }

        val animatedBoxSize by animateDpAsState(
            targetValue = if (isSelected) circleSize else iconSize,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "nav_box_size"
        )
        val animatedBgColor by animateColorAsState(
            targetValue = if (isSelected) Color(0xFFEEF2FF) else Color.Transparent,
            animationSpec = tween(durationMillis = 200),
            label = "nav_bg"
        )
        val animatedContentColor by animateColorAsState(
            targetValue = if (isSelected) Color(0xFF4F46E5) else Color(0xFF9CA3AF),
            animationSpec = tween(durationMillis = 200),
            label = "nav_content"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false, radius = 30.dp, color = Color(0xFF4F46E5).copy(alpha = 0.2f))
                ) { onClick() }
                .padding(vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(animatedBoxSize)
                    .background(
                        color = animatedBgColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(screen.title),
                    tint = animatedContentColor,
                    modifier = Modifier.size(iconSize)
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = stringResource(screen.title),
                fontSize = fontSize,
                lineHeight = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontFamily = interfamily,
                color = animatedContentColor,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
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
