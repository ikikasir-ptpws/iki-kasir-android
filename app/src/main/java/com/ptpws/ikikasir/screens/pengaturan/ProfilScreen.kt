package com.ptpws.ikikasir.screens.pengaturan

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.NotaSettingPreferences
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import kotlinx.coroutines.launch

import androidx.hilt.navigation.compose.hiltViewModel
import com.ptpws.ikikasir.feature.pengaturan.presentation.viewmodel.ProfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    navController: NavController,
    viewModel: ProfilViewModel = hiltViewModel(),
    onEditProfil: () -> Unit = {},
    onKeamanan: () -> Unit = {},
    onAuditLog: () -> Unit = {},
    onMetodePembayaran: () -> Unit = {},
    onSettingPpn: () -> Unit = {},
    onSettingStruk: () -> Unit = {},
    onTentangAplikasi: () -> Unit = {},
    onKeluar: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val liveNotaSetting by viewModel.notaSetting.collectAsState()

    var showPpnDialog by remember { mutableStateOf(false) }
    var showStrukDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // State PPN / Pajak
    var ppnPersen by remember { mutableStateOf("11") }
    var isPpnAktif by remember { mutableStateOf(true) }
    var isPpnInklusif by remember { mutableStateOf(false) }

    // State Nota / Struk — diinisialisasi dari ViewModel (Room DB + Firestore) & SharedPreferences
    val notaPrefs = remember { NotaSettingPreferences(context) }
    val savedNota = remember { notaPrefs.getSetting() }
    var namaToko by remember { mutableStateOf(savedNota.storeName) }
    var alamatToko by remember { mutableStateOf(savedNota.storeAddress) }
    var namaWifi by remember { mutableStateOf(savedNota.wifiName) }
    var kataSandiWifi by remember { mutableStateOf(savedNota.wifiPassword) }
    var ukuranKertas by remember { mutableStateOf(savedNota.paperWidth) }

    LaunchedEffect(liveNotaSetting) {
        if (liveNotaSetting.storeName.isNotBlank() || liveNotaSetting.storeAddress.isNotBlank()) {
            namaToko = liveNotaSetting.storeName
            alamatToko = liveNotaSetting.storeAddress
            namaWifi = liveNotaSetting.wifiName
            kataSandiWifi = liveNotaSetting.wifiPassword
            ukuranKertas = liveNotaSetting.paperWidth
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profil",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = 20.sp, color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.currentDestination?.route == "profil") {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6),
                    titleContentColor = Color(0xFF111827),
                    navigationIconContentColor = Color(0xFF4F46E5)
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 86.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Card Profil
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(85.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFE5E7EB))
                                    .border(2.dp, Color(0xFFEEF2FF), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Admin User",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 6.dp, y = 6.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4F46E5))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profil",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Text(
                            text = "Admin User",
                            fontFamily = interfamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "System Administrator",
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onEditProfil,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEEF2FF)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "Edit Profil",
                                fontFamily = interfamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4F46E5)
                            )
                        }
                    }
                }
            }

            // ── Pengaturan Akun
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PENGATURAN AKUN",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            MenuAkunItem(
                                icon = Icons.Outlined.Security,
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Keamanan",
                                onClick = onKeamanan
                            )
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            MenuAkunItem(
                                icon = Icons.Outlined.History,
                                iconBackground = Color(0xFFFFEDD5),
                                iconTint = Color(0xFFC2410C),
                                label = "Audit Log",
                                onClick = onAuditLog
                            )
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            MenuAkunItem(
                                icon = Icons.Outlined.Payments,
                                iconBackground = Color(0xFFDBEAFE),
                                iconTint = Color(0xFF1D4ED8),
                                label = "Metode Pembayaran",
                                onClick = onMetodePembayaran
                            )
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            MenuAkunItem(
                                icon = Icons.Outlined.Percent,
                                iconBackground = Color(0xFFECFDF5),
                                iconTint = Color(0xFF059669),
                                label = "Setting PPN / Pajak",
                                onClick = {
                                    showPpnDialog = true
                                    onSettingPpn()
                                }
                            )
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            MenuAkunItem(
                                icon = Icons.Outlined.ReceiptLong,
                                iconBackground = Color(0xFFF3E8FF),
                                iconTint = Color(0xFF7E22CE),
                                label = "Setting Nota / Struk Pembayaran",
                                onClick = {
                                    showStrukDialog = true
                                    onSettingStruk()
                                }
                            )
                        }
                    }
                }
            }

            // ── Informasi
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "INFORMASI",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            MenuAkunItem(
                                icon = Icons.Outlined.Info,
                                iconBackground = Color(0xFFE5E7EB),
                                iconTint = Color(0xFF4B5563),
                                label = "Tentang Aplikasi",
                                onClick = onTentangAplikasi
                            )
                        }
                    }
                }
            }

            // ── Tombol Keluar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { showLogoutDialog = true }),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "Keluar",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Keluar",
                            fontFamily = interfamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }

            // ── Versi Aplikasi
            item {
                Text(
                    text = "Versi 2.4.0 • Dibuat dengan presisi",
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // ── Dialog Setting PPN / Pajak
    if (showPpnDialog) {
        Dialog(onDismissRequest = { showPpnDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Setting PPN / Pajak",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF111827)
                        )
                        IconButton(
                            onClick = { showPpnDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF3F4F6))

                    // Switch Aktifkan PPN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Status PPN / Pajak",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = if (isPpnAktif) "PPN aktif untuk setiap transaksi" else "PPN dinonaktifkan",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Switch(
                            checked = isPpnAktif,
                            onCheckedChange = { isPpnAktif = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4F46E5)
                            )
                        )
                    }

                    if (isPpnAktif) {
                        // Input Persentase PPN
                        OutlinedTextField(
                            value = ppnPersen,
                            onValueChange = { ppnPersen = it },
                            label = { Text("Persentase PPN (%)", fontFamily = interfamily) },
                            trailingIcon = { Text("%", fontFamily = interfamily, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4F46E5),
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )

                        // Skema PPN (Inklusif vs Eksklusif)
                        Text(
                            text = "Tipe Perhitungan PPN",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF111827)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !isPpnInklusif,
                                onClick = { isPpnInklusif = false },
                                label = { Text("Belum Termasuk (Eksklusif)", fontFamily = interfamily, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEEF2FF),
                                    selectedLabelColor = Color(0xFF4F46E5)
                                )
                            )
                            FilterChip(
                                selected = isPpnInklusif,
                                onClick = { isPpnInklusif = true },
                                label = { Text("Sudah Termasuk (Inklusif)", fontFamily = interfamily, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEEF2FF),
                                    selectedLabelColor = Color(0xFF4F46E5)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPpnDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Text("Batal", fontFamily = interfamily, color = Color(0xFF4B5563))
                        }
                        Button(
                            onClick = {
                                showPpnDialog = false
                                Toast.makeText(context, "Pengaturan PPN berhasil disimpan", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                        ) {
                            Text("Simpan", fontFamily = interfamily, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // ── Dialog Setting Nota / Struk Pembayaran
    if (showStrukDialog) {
        Dialog(onDismissRequest = { showStrukDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Setting Nota / Struk",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF111827)
                        )
                        IconButton(
                            onClick = { showStrukDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF3F4F6))

                    OutlinedTextField(
                        value = namaToko,
                        onValueChange = { namaToko = it },
                        label = { Text("Nama Toko / Header", fontFamily = interfamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )

                    OutlinedTextField(
                        value = alamatToko,
                        onValueChange = { alamatToko = it },
                        label = { Text("Alamat Toko", fontFamily = interfamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )

                    OutlinedTextField(
                        value = namaWifi,
                        onValueChange = { namaWifi = it },
                        label = { Text("Nama WiFi", fontFamily = interfamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )

                    OutlinedTextField(
                        value = kataSandiWifi,
                        onValueChange = { kataSandiWifi = it },
                        label = { Text("Kata Sandi WiFi", fontFamily = interfamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )

                    // Lebar Kertas Struk
                    Text(
                        text = "Lebar Kertas Thermal",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF111827)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = ukuranKertas == "58mm",
                            onClick = { ukuranKertas = "58mm" },
                            label = { Text("58 mm", fontFamily = interfamily, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEEF2FF),
                                selectedLabelColor = Color(0xFF4F46E5)
                            )
                        )
                        FilterChip(
                            selected = ukuranKertas == "80mm",
                            onClick = { ukuranKertas = "80mm" },
                            label = { Text("80 mm", fontFamily = interfamily, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEEF2FF),
                                selectedLabelColor = Color(0xFF4F46E5)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showStrukDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Text("Batal", fontFamily = interfamily, color = Color(0xFF4B5563))
                        }
                        Button(
                            onClick = {
                                val updatedSetting = NotaSetting(
                                    storeName    = namaToko,
                                    storeAddress = alamatToko,
                                    wifiName     = namaWifi,
                                    wifiPassword = kataSandiWifi,
                                    paperWidth   = ukuranKertas
                                )
                                // 1. Simpan ke SharedPreferences
                                notaPrefs.saveSetting(updatedSetting)

                                // 2. Simpan ke Room DB & Firestore via ViewModel (Clean Architecture)
                                viewModel.saveNotaSetting(updatedSetting) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Pengaturan Nota berhasil disimpan ke Database (Room & Firestore)", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Pengaturan Nota disimpan secara offline", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                showStrukDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                        ) {
                            Text("Simpan", fontFamily = interfamily, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // ── Dialog Logout
    if (showLogoutDialog) {
        Dialog(onDismissRequest = { showLogoutDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Konfirmasi Keluar",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Apakah Anda yakin ingin keluar dari akun ini? Anda harus login kembali untuk masuk.",
                        fontFamily = interfamily,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showLogoutDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "Batal",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4B5563)
                            )
                        }
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                onKeluar()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "Ya, Keluar",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Menu Item Akun (reusable)

@Composable
fun MenuAkunItem(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontFamily = interfamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111827),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(18.dp)
        )
    }
}

// ── Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilScreenPreview() {
    MaterialTheme {
        ProfilScreen(navController = rememberNavController())
    }
}