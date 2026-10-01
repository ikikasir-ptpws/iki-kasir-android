package com.ptpws.ikikasir.feature.bluetooth.presentation.screen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterDevice
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import com.ptpws.ikikasir.feature.bluetooth.presentation.viewmodel.BluetoothPrinterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothPrinterScreen(
    navController: NavController,
    viewModel: BluetoothPrinterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Local mutable state for UI editing
    var selectedDevice by remember(state.selectedDevice) {
        mutableStateOf(state.selectedDevice)
    }
    var isAutoConnect by remember(state.setting.isAutoConnect) {
        mutableStateOf(state.setting.isAutoConnect)
    }

    // ── Permission launcher (Android 12+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.updatePermissionStatus(granted)
    }

    // ── Enable Bluetooth launcher
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshPairedDevices()
    }

    // Request permissions on first launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            viewModel.updatePermissionStatus(true)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Printer Bluetooth",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF4F46E5)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F4F6)
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
                bottom = 40.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header Card: Status Bluetooth
            item {
                BluetoothStatusCard(
                    isEnabled = state.isBluetoothEnabled,
                    onEnableBluetooth = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            !state.hasBluetoothPermission
                        ) {
                            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                        } else {
                            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            enableBluetoothLauncher.launch(intent)
                        }
                    },
                    onOpenBluetoothSettings = {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    }
                )
            }

            // ── Printer Terpilih (jika ada)
            if (selectedDevice != null) {
                item {
                    SelectedPrinterCard(
                        device = selectedDevice!!,
                        isConnected = state.connectedAddress == selectedDevice?.address,
                        isConnecting = state.isConnecting,
                        onClear = {
                            selectedDevice = null
                        }
                    )
                }
            }

            // ── Section: Perangkat Paired
            item {
                SectionLabel(text = "PERANGKAT BLUETOOTH TERSAMBUNG")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {

                        // Refresh button row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                        !state.hasBluetoothPermission
                                    ) {
                                        permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                    } else {
                                        viewModel.refreshPairedDevices()
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEEF2FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Cari Perangkat",
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = "Tampilkan ulang daftar perangkat paired",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFD1D5DB),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (state.pairedDevices.isNotEmpty()) {
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                        }

                        // Daftar device
                        if (state.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = Color(0xFF4F46E5),
                                    strokeWidth = 2.5.dp
                                )
                            }
                        } else if (!state.hasBluetoothPermission) {
                            BluetoothPermissionPlaceholder(
                                onRequestPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                    } else {
                                        viewModel.updatePermissionStatus(true)
                                    }
                                }
                            )
                        } else if (state.pairedDevices.isEmpty() && state.isBluetoothEnabled) {
                            EmptyDevicesPlaceholder()
                        } else if (!state.isBluetoothEnabled) {
                            BluetoothOffPlaceholder()
                        }
                    }
                }
            }

            // Device items
            items(state.pairedDevices) { device ->
                BluetoothDeviceItem(
                    device = device,
                    isSelected = state.connectedAddress == device.address,
                    onClick = {
                        selectedDevice = device
                        viewModel.selectDevice(device)
                    }
                )
            }

            // ── Section: Pengaturan Printer
            item {
                SectionLabel(text = "PENGATURAN PRINTER")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {

                        // Auto Connect Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFECFDF5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bluetooth,
                                        contentDescription = "Auto Connect",
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Koneksi Otomatis",
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = if (isAutoConnect) "Otomatis terhubung saat aplikasi dibuka"
                                               else "Koneksi manual setiap kali ingin mencetak",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                            Switch(
                                checked = isAutoConnect,
                                onCheckedChange = { isAutoConnect = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4F46E5)
                                )
                            )
                        }

                    }
                }
            }

            // ── Tombol Simpan
            item {
                Button(
                    onClick = {
                        val updatedSetting = BluetoothPrinterSetting(
                            id = "default",
                            savedAddress = selectedDevice?.address ?: "",
                            savedName = selectedDevice?.name ?: "",
                            isAutoConnect = isAutoConnect,
                            paperWidth = state.setting.paperWidth
                        )
                        viewModel.saveSetting(updatedSetting) { saved ->
                            if (saved) {
                                val profileRoute = com.ptpws.ikikasir.screens.navigation.AppScreen.Profil.route
                                if (!navController.popBackStack(profileRoute, inclusive = false)) {
                                    navController.navigate(profileRoute) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    ),
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan Pengaturan",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Tips
            item {
                BluetoothTipsCard()
            }
        }
    }
}

// ── Sub-composables ─────────────────────────────────────────────────────────

@Composable
private fun BluetoothStatusCard(
    isEnabled: Boolean,
    onEnableBluetooth: () -> Unit,
    onOpenBluetoothSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isEnabled)
                            listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                        else
                            listOf(Color(0xFF6B7280), Color(0xFF9CA3AF))
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Default.Bluetooth
                                         else Icons.Default.BluetoothDisabled,
                            contentDescription = "Bluetooth",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isEnabled) "Bluetooth Aktif" else "Bluetooth Tidak Aktif",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isEnabled) "Siap mencari printer" else "Aktifkan untuk mencari printer",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                OutlinedButton(
                    onClick = if (isEnabled) onOpenBluetoothSettings else onEnableBluetooth,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isEnabled) "Pair Printer" else "Aktifkan",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedPrinterCard(
    device: BluetoothPrinterDevice,
    isConnected: Boolean,
    isConnecting: Boolean,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) Color(0xFFEEF2FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isConnected) Color(0xFFADB5FF) else Color(0xFFE5E7EB)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color(0xFF4F46E5) else Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Printer",
                        tint = if (isConnected) Color.White else Color(0xFF6B7280),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Printer Dipilih",
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isConnected) Color(0xFF4F46E5) else Color(0xFF6B7280)
                    )
                    Text(
                        text = device.name,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1F2937),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            isConnecting -> "Menghubungkan..."
                            isConnected -> "${device.address} • Terhubung"
                            else -> "${device.address} • Belum terhubung"
                        },
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hapus pilihan",
                    tint = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
private fun BluetoothDeviceItem(
    device: BluetoothPrinterDevice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEEF2FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF4F46E5))
        else
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) Color(0xFF4F46E5)
                            else Color(0xFFF3F4F6)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Printer",
                        tint = if (isSelected) Color.White else Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = device.name,
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF1F2937),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = device.address,
                        fontFamily = interfamily,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Dipilih",
                    tint = Color(0xFF4F46E5),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyDevicesPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.BluetoothSearching,
                contentDescription = null,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tidak ada perangkat ditemukan",
                fontFamily = interfamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = "Pair printer Anda di Pengaturan Bluetooth Android",
                fontFamily = interfamily,
                fontSize = 11.sp,
                color = Color(0xFFD1D5DB)
            )
        }
    }
}

@Composable
private fun BluetoothOffPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.BluetoothDisabled,
                contentDescription = null,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bluetooth tidak aktif",
                fontFamily = interfamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = "Aktifkan Bluetooth untuk melihat perangkat",
                fontFamily = interfamily,
                fontSize = 11.sp,
                color = Color(0xFFD1D5DB)
            )
        }
    }
}

@Composable
private fun BluetoothPermissionPlaceholder(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Izin Bluetooth diperlukan untuk menampilkan printer yang sudah dipasangkan.",
            fontFamily = interfamily,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
        OutlinedButton(onClick = onRequestPermission) {
            Text(
                text = "Berikan Izin Bluetooth",
                fontFamily = interfamily,
                color = Color(0xFF4F46E5)
            )
        }
    }
}

@Composable
private fun BluetoothTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = "Tips",
                tint = Color(0xFFD97706),
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 1.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Tips Koneksi Printer",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = "• Pastikan printer dalam mode pairing\n" +
                           "• Pair perangkat terlebih dahulu di Pengaturan Bluetooth Android\n" +
                           "• Dukung printer thermal ESC/POS Bluetooth (ex: Epson, iMin, HOIN)\n" +
                           "• Pilih lebar kertas sesuai tipe printer Anda",
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFFB45309)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = interfamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF9CA3AF)
    )
}
