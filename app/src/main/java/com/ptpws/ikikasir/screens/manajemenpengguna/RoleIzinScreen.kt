package com.ptpws.ikikasir.screens.manajemenpengguna

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.commond.interfamily

@Composable
fun RoleIzinScreen() {
    var selectedRole by remember { mutableStateOf(0) }

    // State toggle akses per-role: [roleIndex][menuIndex] = Boolean
    val aksesState = remember {
        mutableStateListOf(
            // Admin
            mutableStateListOf(true, true, true, false, false, false),
            // Kasir
            mutableStateListOf(true, true, false, false, false, false),
            // Inventory
            mutableStateListOf(false, false, true, false, false, false),
        )
    }

    // Label menu & deskripsi
    val menuList = remember {
        listOf(
            "Dashboard"  to "Ringkasan performa toko",
            "Transaksi"  to "Kelola penjualan & kasir",
            "Produk"     to "Manajemen stok & katalog",
            "Laporan"    to "Analisis laba & rugi",
            "Hutang"     to "Piutang pelanggan & supplier",
            "Pengaturan" to "Konfigurasi sistem & profil",
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    )
    {

        Spacer(Modifier.height(16.dp))

        Text(
            text = "PILIH ROLE",
            fontFamily = interfamily,
            color = Color(0xFF6B7280),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                "Admin"     to Icons.Outlined.AdminPanelSettings,
                "Kasir"     to Icons.Outlined.PointOfSale,
                "Inventory" to Icons.Outlined.Inventory2
            ).forEachIndexed { index, (label, icon) ->
                val selected = selectedRole == index
                Card(
                    modifier = Modifier
                        .width(112.dp)
                        .height(110.dp)
                        .clickable {
                            selectedRole = index
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected)
                            Color(0xFF4F46E5)
                        else
                            Color(0xFFE5E7EB)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    Color(0xFFF3F4F6),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = label,
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selected)
                                Color(0xFF4F46E5)
                            else
                                Color(0xFF374151)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "AKSES MENU",
            fontFamily = interfamily,
            color = Color(0xFF6B7280),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(menuList.size) { i ->
                val (judul, deskripsi) = menuList[i]
                val enabled = aksesState[selectedRole][i]

                val menuIcon = when (judul) {
                    "Dashboard"  ->  Icons.Outlined.SpaceDashboard
                    "Transaksi"  -> Icons.Outlined.PointOfSale
                    "Produk"     -> Icons.Outlined.Inventory2
                    "Laporan"    -> Icons.Outlined.Assessment
                    "Hutang"     -> Icons.Outlined.AccountBalanceWallet
                    else         -> Icons.Outlined.Settings
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF4F46E5).copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = menuIcon,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = judul,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827),
                                fontSize = 14.sp
                            )
                            Text(
                                text = deskripsi,
                                fontFamily = interfamily,
                                color = Color(0xFF6B7280),
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = enabled,
                            onCheckedChange = { aksesState[selectedRole][i] = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4F46E5),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                }
            }
        }

        Button(
            onClick = { /* simpan */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
        ) {
            Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Simpan Perubahan",
                fontFamily = interfamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}