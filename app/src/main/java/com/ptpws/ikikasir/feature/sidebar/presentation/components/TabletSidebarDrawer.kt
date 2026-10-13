package com.ptpws.ikikasir.feature.sidebar.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.sidebar.presentation.viewmodel.SidebarViewModel

private val PrimaryRoyalBlue = Color(0xFF3B32D1)
private val BorderColor = Color(0xFFE2E8F0)
private val DividerColor = Color(0xFFF1F5F9)

/**
 * Komponen Sidebar Utama untuk Tablet dalam orientasi Landscape.
 * Mengelompokkan seluruh menu POS ke dalam sidebar vertikal dengan dukungan
 * sub-menu accordion yang dapat di-expand/collapse secara interaktif.
 *
 * @param currentRoute Rute destinasi yang sedang aktif saat ini.
 * @param onNavigate Lambda navigasi saat sebuah menu atau sub-menu diklik.
 * @param viewModel ViewModel sidebar berarsitektur MVVM Clean Architecture.
 */
@Composable
fun TabletSidebarDrawer(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SidebarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Selalu sinkronkan rute aktif agar parent menu terbuka secara otomatis
    LaunchedEffect(currentRoute) {
        viewModel.updateCurrentRoute(currentRoute)
    }

    Surface(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight(),
        color = Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp, vertical = 16.dp)
            ) {
                // 1. Header: Logo Aplikasi & Nama POS
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iconapk),
                        contentDescription = "Logo Iki Kasir",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column {
                        Text(
                            text = "IKI KASIR",
                            fontFamily = interfamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "POS Terminal",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                HorizontalDivider(
                    color = DividerColor,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // 2. User Profile Strip
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigate(com.ptpws.ikikasir.screens.navigation.AppScreen.Profil.route) },
                    color = Color(0xFFF8FAFC)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        var avatarError by remember(uiState.userPhotoUrl) { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!uiState.userPhotoUrl.isNullOrBlank() && !avatarError) {
                                AsyncImage(
                                    model = uiState.userPhotoUrl,
                                    contentDescription = uiState.userName,
                                    contentScale = ContentScale.Crop,
                                    onError = { avatarError = true },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = uiState.userName.firstOrNull()?.uppercase() ?: "U",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryRoyalBlue,
                                    fontFamily = interfamily
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.userName,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = interfamily,
                                color = Color(0xFF1E293B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryRoyalBlue.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = uiState.userRole,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = interfamily,
                                    color = PrimaryRoyalBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Scrollable List of Menus and Sub-menus
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.menuItems.forEach { menuItem ->
                        val isExpanded = menuItem.id in uiState.expandedGroupIds
                        val isChildActive = menuItem.containsRoute(currentRoute)
                        val isDirectSelected = menuItem.route != null && (
                                currentRoute == menuItem.route ||
                                currentRoute?.startsWith(menuItem.route) == true
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Baris Menu Utama
                            SidebarMenuItemRow(
                                item = menuItem,
                                isExpanded = isExpanded,
                                isChildActive = isChildActive && !isDirectSelected,
                                isSelected = isDirectSelected,
                                onClick = {
                                    if (menuItem.isExpandable) {
                                        viewModel.toggleGroup(menuItem.id)
                                    } else if (menuItem.route != null) {
                                        onNavigate(menuItem.route)
                                    }
                                }
                            )

                            // Daftar Sub-Menu dengan Animasi Buka/Tutup Smooth
                            if (menuItem.isExpandable) {
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(animationSpec = tween(220)) +
                                            fadeIn(animationSpec = tween(200)),
                                    exit = shrinkVertically(animationSpec = tween(220)) +
                                            fadeOut(animationSpec = tween(150))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp, bottom = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        menuItem.subItems.forEach { subItem ->
                                            val isSubSelected = isSubRouteActive(subItem.route, currentRoute)
                                            SidebarSubMenuItemRow(
                                                subItem = subItem,
                                                isSelected = isSubSelected,
                                                onClick = { onNavigate(subItem.route) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Footer: Versi & Hak Cipta
                HorizontalDivider(
                    color = DividerColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "v1.0.0 • IKI KASIR POS",
                    fontSize = 11.sp,
                    fontFamily = interfamily,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Garis pembatas vertikal di sisi kanan drawer
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = BorderColor
            )
        }
    }
}

/**
 * Logika pengecekan apakah sub-menu saat ini sedang aktif berdasarkan rute saat ini.
 */
private fun isSubRouteActive(subRoute: String, currentRoute: String?): Boolean {
    if (currentRoute == null) return false
    val cleanSub = subRoute.substringBefore("?")
    val cleanCurrent = currentRoute.substringBefore("?")

    if (cleanCurrent == cleanSub) return true

    // Khusus produk: pastikan tidak bentrok dengan kategori_produk
    if (cleanSub == "produk" && cleanCurrent.startsWith("produk") && !cleanCurrent.startsWith("kategori")) {
        return true
    }

    // Khusus manajemen stok: rute update_stok juga merupakan bagian dari manajemen stok
    if (cleanSub == "manajemen_stok" && cleanCurrent.startsWith("update_stok")) {
        return true
    }

    // Khusus transaksi: detailtransaksi merupakan bagian dari transaksi
    if (cleanSub == "riwayat" && (cleanCurrent == "riwayat" || cleanCurrent == "detailtransaksi")) {
        return true
    }

    // Khusus laporan penjualan
    if (cleanSub == "laporan_penjualan" && cleanCurrent.startsWith("laporan_penjualan")) {
        return true
    }

    return false
}
