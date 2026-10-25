package com.ptpws.ikikasir.screens.pengaturan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.commond.GlobalTextField
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.commond.GlobalUploadProgress
import com.ptpws.ikikasir.core.util.ImageCompressorHelper
import kotlinx.coroutines.delay
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.NotaSettingPreferences
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

import androidx.hilt.navigation.compose.hiltViewModel
import com.ptpws.ikikasir.feature.pengaturan.presentation.viewmodel.ProfilViewModel
import com.ptpws.ikikasir.feature.auth.presentation.viewmodel.SecurityViewModel
import com.ptpws.ikikasir.feature.tentangaplikasi.TentangAplikasiDialog
import com.ptpws.ikikasir.screens.navigation.AppScreen
import androidx.activity.compose.BackHandler
import com.ptpws.ikikasir.screens.navigation.popBackStackSafely

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    navController: NavController,
    viewModel: ProfilViewModel = hiltViewModel(),
    securityViewModel: SecurityViewModel = hiltViewModel(),
    onEditProfil: () -> Unit = {},
    onKeamanan: () -> Unit = {},
    onAuditLog: () -> Unit = {},
    onMetodePembayaran: () -> Unit = {},
    onSettingPpn: () -> Unit = {},
    onSettingStruk: () -> Unit = {},
    onTentangAplikasi: () -> Unit = {},
    onReferral: () -> Unit = {},
    onKeluar: () -> Unit = {}
) {
    BackHandler {
        navController.popBackStackSafely(expectedRoute = "profil")
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userProfileState by viewModel.userProfileState.collectAsState()
    val securityState by securityViewModel.state.collectAsState()
    val liveNotaSetting by viewModel.notaSetting.collectAsState()
    val liveTaxSetting by viewModel.taxSetting.collectAsState()
    val livePaymentMethodSetting by viewModel.paymentMethodSetting.collectAsState()
    val liveTableSetting by viewModel.tableSetting.collectAsState()

    var showEditProfilDialog by remember { mutableStateOf(false) }
    var showPpnDialog by remember { mutableStateOf(false) }
    var showStrukDialog by remember { mutableStateOf(false) }
    var showMetodePembayaranDialog by remember { mutableStateOf(false) }
    var showMejaDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showTentangAplikasiDialog by remember { mutableStateOf(false) }

    LaunchedEffect(securityState.isSuccess) {
        if (securityState.isSuccess) {
            showSecurityDialog = false
            securityViewModel.clearForm()
        }
    }

    // State Setting Meja — diinisialisasi dari ViewModel (Room DB + Firestore) & SharedPreferences
    val tablePrefs = remember { com.ptpws.ikikasir.feature.pengaturan.data.preferences.TableSettingPreferences(context) }
    var isMejaAktif by remember { mutableStateOf(tablePrefs.isTableEnabled()) }

    // State PPN / Pajak — diinisialisasi dari ViewModel (Room DB + Firestore) & SharedPreferences
    val taxPrefs = remember { com.ptpws.ikikasir.feature.pengaturan.data.preferences.TaxSettingPreferences(context) }
    val savedTax = remember { taxPrefs.getSetting() }
    var ppnPersen by remember { mutableStateOf(savedTax.percentage.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }) }
    var isPpnAktif by remember { mutableStateOf(savedTax.isActive) }
    var isPpnInklusif by remember { mutableStateOf(savedTax.type == com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting.TAX_TYPE_INCLUSIVE) }

    // State Metode Pembayaran — diinisialisasi dari ViewModel (Room DB + Firestore) & SharedPreferences
    val paymentMethodPrefs = remember { com.ptpws.ikikasir.feature.pengaturan.data.preferences.PaymentMethodSettingPreferences(context) }
    val savedPaymentMethod = remember { paymentMethodPrefs.getSetting() }
    var isTunaiAktif by remember { mutableStateOf(savedPaymentMethod.isTunaiEnabled) }
    var isQrisAktif by remember { mutableStateOf(savedPaymentMethod.isQrisEnabled) }
    var isTransferAktif by remember { mutableStateOf(savedPaymentMethod.isTransferEnabled) }
    var isKartuKreditAktif by remember { mutableStateOf(savedPaymentMethod.isKartuKreditEnabled) }

    // State Nota / Struk — diinisialisasi dari ViewModel (Room DB + Firestore) & SharedPreferences
    val notaPrefs = remember { NotaSettingPreferences(context) }
    val savedNota = remember { notaPrefs.getSetting() }
    var namaToko by remember { mutableStateOf(savedNota.storeName) }
    var alamatToko by remember { mutableStateOf(savedNota.storeAddress) }
    var namaWifi by remember { mutableStateOf(savedNota.wifiName) }
    var kataSandiWifi by remember { mutableStateOf(savedNota.wifiPassword) }
    var ukuranKertas by remember { mutableStateOf(savedNota.paperWidth) }
    var logoUrl by remember { mutableStateOf(savedNota.logoUrl) }
    var pendingLogoUri by remember { mutableStateOf<Uri?>(null) }
    var showLogoPicker by remember { mutableStateOf(false) }
    var isSavingNota by remember { mutableStateOf(false) }

    if (showLogoPicker) {
        com.ptpws.ikikasir.commond.ImageSourcePickerDialog(
            onImageSelected = { uri ->
                pendingLogoUri = uri
                logoUrl = uri.toString()
                showLogoPicker = false
            },
            onDismiss = { showLogoPicker = false },
            uploadType = com.ptpws.ikikasir.commond.ImageUploadType.LOGO_STRUK,
            targetSubfolder = "store_logos"
        )
    }

    LaunchedEffect(liveTableSetting) {
        isMejaAktif = liveTableSetting.isTableEnabled
    }

    LaunchedEffect(liveNotaSetting) {
        if (liveNotaSetting.storeName.isNotBlank() || liveNotaSetting.storeAddress.isNotBlank() || liveNotaSetting.logoUrl.isNotBlank()) {
            namaToko = liveNotaSetting.storeName
            alamatToko = liveNotaSetting.storeAddress
            namaWifi = liveNotaSetting.wifiName
            kataSandiWifi = liveNotaSetting.wifiPassword
            ukuranKertas = liveNotaSetting.paperWidth
            logoUrl = liveNotaSetting.logoUrl
        }
    }

    LaunchedEffect(liveTaxSetting) {
        isPpnAktif = liveTaxSetting.isActive
        ppnPersen = liveTaxSetting.percentage.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
        isPpnInklusif = liveTaxSetting.type == com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting.TAX_TYPE_INCLUSIVE
    }

    LaunchedEffect(livePaymentMethodSetting) {
        isTunaiAktif = livePaymentMethodSetting.isTunaiEnabled
        isQrisAktif = livePaymentMethodSetting.isQrisEnabled
        isTransferAktif = livePaymentMethodSetting.isTransferEnabled
        isKartuKreditAktif = livePaymentMethodSetting.isKartuKreditEnabled
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
                        navController.popBackStackSafely(expectedRoute = "profil")
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
                bottom = 140.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Card Profil
            item {
                val user = userProfileState.user
                val displayName = user?.fullName?.takeIf { it.isNotBlank() } ?: "Admin User"
                val displayRole = userProfileState.roleName.takeIf { it.isNotBlank() } ?: "System Administrator"
                val userPhoto = user?.photoUrl.orEmpty()
                var avatarLoadError by remember(userPhoto) { mutableStateOf(false) }

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
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clickable { showEditProfilDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5E7EB))
                                    .border(2.5.dp, Color(0xFFEEF2FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userPhoto.isNotBlank() && !avatarLoadError) {
                                    AsyncImage(
                                        model = userPhoto,
                                        contentDescription = displayName,
                                        contentScale = ContentScale.Crop,
                                        onError = { avatarLoadError = true },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = displayName,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(52.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 2.dp, y = 2.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4F46E5))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profil",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Text(
                            text = displayName,
                            fontFamily = interfamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = displayRole,
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                onEditProfil()
                                showEditProfilDialog = true
                            },
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
                                onClick = {
                                    onKeamanan()
                                    securityViewModel.clearForm()
                                    showSecurityDialog = true
                                }
                            )

                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            MenuAkunItem(
                                icon = Icons.Outlined.Payments,
                                iconBackground = Color(0xFFDBEAFE),
                                iconTint = Color(0xFF1D4ED8),
                                label = "Metode Pembayaran",
                                onClick = {
                                    showMetodePembayaranDialog = true
                                    onMetodePembayaran()
                                }
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
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            MenuAkunItem(
                                icon = Icons.Outlined.TableBar,
                                iconBackground = Color(0xFFFEF3C7),
                                iconTint = Color(0xFFD97706),
                                label = "Setting Meja",
                                onClick = {
                                    showMejaDialog = true
                                }
                            )
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            MenuAkunItem(
                                icon = Icons.Outlined.Print,
                                iconBackground = Color(0xFFEEF2FF),
                                iconTint = Color(0xFF4F46E5),
                                label = "Printer Bluetooth",
                                onClick = {
                                    navController.navigate(AppScreen.BluetoothPrinter.route)
                                }
                            )
                            HorizontalDivider(color = Color(0xFFF3F4F6))
                            MenuAkunItem(
                                icon = Icons.Outlined.CardGiftcard,
                                iconBackground = Color(0xFFE0F2FE),
                                iconTint = Color(0xFF0284C7),
                                label = "Referral",
                                onClick = {
                                    onReferral()
                                    navController.navigate(AppScreen.Referral.route)
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
                                onClick = {
                                    showTentangAplikasiDialog = true
                                    onTentangAplikasi()
                                }
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Versi 1.0",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // ── Dialog Setting Metode Pembayaran
    if (showMetodePembayaranDialog) {
        Dialog(onDismissRequest = { showMetodePembayaranDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header (Judul & Close Button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pengaturan Metode Pembayaran",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1E293B)
                        )
                        IconButton(
                            onClick = { showMetodePembayaranDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Text(
                        text = "Aktifkan atau nonaktifkan pilihan metode pembayaran yang muncul di kasir saat checkout.",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Switch 1: Tunai
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tunai",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isTunaiAktif) "Pembayaran tunai / cash manual aktif" else "Pembayaran tunai nonaktif",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Switch(
                            checked = isTunaiAktif,
                            onCheckedChange = { isTunaiAktif = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B32D1)
                            )
                        )
                    }

                    // Switch 2: QRIS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "QRIS",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isQrisAktif) "BCA, GoPay, OVO, ShopeePay, dll. aktif" else "QRIS nonaktif",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Switch(
                            checked = isQrisAktif,
                            onCheckedChange = { isQrisAktif = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B32D1)
                            )
                        )
                    }

                    // Switch 3: Transfer Bank
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Transfer Bank",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isTransferAktif) "Transfer BCA, Mandiri, BRI, dll. aktif" else "Transfer bank nonaktif",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Switch(
                            checked = isTransferAktif,
                            onCheckedChange = { isTransferAktif = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B32D1)
                            )
                        )
                    }

                    // Switch 4: Kartu Debit / Kredit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kartu Debit / Kredit",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isKartuKreditAktif) "Pembayaran EDC Mesin Kartu aktif" else "Kartu Debit/Kredit nonaktif",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Switch(
                            checked = isKartuKreditAktif,
                            onCheckedChange = { isKartuKreditAktif = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B32D1)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons (Batal & Simpan)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showMetodePembayaranDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = "Batal",
                                fontFamily = interfamily,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                val updatedSetting = com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting(
                                    id = "default",
                                    isTunaiEnabled = isTunaiAktif,
                                    isQrisEnabled = isQrisAktif,
                                    isTransferEnabled = isTransferAktif,
                                    isKartuKreditEnabled = isKartuKreditAktif
                                )
                                paymentMethodPrefs.saveSetting(updatedSetting)

                                viewModel.savePaymentMethodSetting(updatedSetting)
                                showMetodePembayaranDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B32D1))
                        ) {
                            Text(
                                text = "Simpan",
                                fontFamily = interfamily,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
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
                        GlobalTextField(
                            value = ppnPersen,
                            onValueChange = { ppnPersen = it },
                            label = "Persentase PPN (%)",
                            trailingIcon = { Text("%", fontFamily = interfamily, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
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
                                val parsedPercent = ppnPersen.toDoubleOrNull() ?: 11.0
                                val updatedSetting = com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting(
                                    id = "default",
                                    isActive = isPpnAktif,
                                    percentage = parsedPercent,
                                    type = if (isPpnInklusif) com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting.TAX_TYPE_INCLUSIVE
                                           else com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting.TAX_TYPE_EXCLUSIVE
                                )

                                // 1. Simpan ke SharedPreferences
                                taxPrefs.saveSetting(updatedSetting)

                                // 2. Simpan ke Room DB & Firestore via ViewModel (Clean Architecture)
                                viewModel.saveTaxSetting(updatedSetting)

                                showPpnDialog = false
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
        val strukScrollState = rememberScrollState()
        Dialog(onDismissRequest = { showStrukDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(strukScrollState)
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

                    // ── Upload Logo Nota / Struk ──
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Logo Nota / Struk",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF374151)
                        )

                        if (logoUrl.isNotBlank()) {
                            val logoModel = remember(logoUrl) {
                                if (logoUrl.startsWith("data:image")) {
                                    try {
                                        android.util.Base64.decode(logoUrl.substringAfter("base64,"), android.util.Base64.DEFAULT)
                                    } catch (_: Exception) {
                                        logoUrl
                                    }
                                } else {
                                    logoUrl
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = logoModel,
                                    contentDescription = "Logo Nota",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Logo Toko Terpasang",
                                        fontFamily = interfamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF111827)
                                    )
                                    Text(
                                        text = "Akan tampil di bagian atas nota saat dicetak",
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFEEF2FF)) {
                                        IconButton(
                                            onClick = { showLogoPicker = true },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = "Ganti Logo",
                                                tint = Color(0xFF4F46E5),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFFF1F2)) {
                                        IconButton(
                                            onClick = {
                                                logoUrl = ""
                                                pendingLogoUri = null
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Hapus Logo",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showLogoPicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF9FAFB),
                                border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEEF2FF),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Image,
                                                contentDescription = null,
                                                tint = Color(0xFF4F46E5),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Upload Logo Struk",
                                            fontFamily = interfamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1F2937)
                                        )
                                        Text(
                                            text = "Ketuk untuk memilih gambar logo toko",
                                            fontFamily = interfamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Outlined.Upload,
                                        contentDescription = "Pilih Gambar",
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    GlobalTextField(
                        value = namaToko,
                        onValueChange = { namaToko = it },
                        label = "Nama Toko / Header",
                        placeholder = "Masukkan nama toko"
                    )

                    GlobalTextField(
                        value = alamatToko,
                        onValueChange = { alamatToko = it },
                        label = "Alamat Toko",
                        placeholder = "Masukkan alamat toko"
                    )

                    GlobalTextField(
                        value = namaWifi,
                        onValueChange = { namaWifi = it },
                        label = "Nama WiFi",
                        placeholder = "Masukkan nama WiFi"
                    )

                    GlobalTextField(
                        value = kataSandiWifi,
                        onValueChange = { kataSandiWifi = it },
                        label = "Kata Sandi WiFi",
                        placeholder = "Masukkan kata sandi WiFi"
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
                                isSavingNota = true
                                val updatedSetting = NotaSetting(
                                    storeName = namaToko,
                                    storeAddress = alamatToko,
                                    wifiName = namaWifi,
                                    wifiPassword = kataSandiWifi,
                                    paperWidth = ukuranKertas,
                                    logoUrl = if (pendingLogoUri == null) logoUrl else ""
                                )
                                viewModel.saveNotaSettingWithLogo(updatedSetting, pendingLogoUri) { savedSetting ->
                                    isSavingNota = false
                                    if (savedSetting != null) {
                                        notaPrefs.saveSetting(savedSetting)
                                        logoUrl = savedSetting.logoUrl
                                        pendingLogoUri = null
                                        showStrukDialog = false
                                    }
                                }
                            },
                            enabled = !isSavingNota,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                        ) {
                            if (isSavingNota) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Simpan", fontFamily = interfamily, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialog Setting Meja
    if (showMejaDialog) {
        Dialog(onDismissRequest = { showMejaDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header (Judul & Close Button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pengaturan Meja",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1E293B)
                        )
                        IconButton(
                            onClick = { showMejaDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Text(
                        text = "Aktifkan atau nonaktifkan fitur nomor meja untuk pelanggan saat transaksi di halaman pembayaran.",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Switch Nomor Meja
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Status Nomor Meja",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isMejaAktif) "Input nomor meja di halaman pembayaran aktif" else "Nomor meja nonaktif",
                                fontFamily = interfamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Switch(
                            checked = isMejaAktif,
                            onCheckedChange = { isMejaAktif = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF3B32D1)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons (Batal & Simpan)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showMejaDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = "Batal",
                                fontFamily = interfamily,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                val updatedSetting = com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting(
                                    id = "default",
                                    isTableEnabled = isMejaAktif
                                )
                                tablePrefs.setTableEnabled(isMejaAktif)

                                viewModel.saveTableSetting(updatedSetting)
                                showMejaDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B32D1))
                        ) {
                            Text(
                                text = "Simpan",
                                fontFamily = interfamily,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
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

    // ── Dialog Edit Profil
    if (showEditProfilDialog) {
        val currentUser = userProfileState.user
        var editFullName by remember(currentUser) { mutableStateOf(currentUser?.fullName.orEmpty()) }
        var editPhotoUrl by remember(currentUser) { mutableStateOf(currentUser?.photoUrl.orEmpty()) }
        var pendingProfilePhotoUri by remember(currentUser) { mutableStateOf<Uri?>(null) }
        var showProfilePhotoPicker by remember { mutableStateOf(false) }
        var isSavingProfile by remember { mutableStateOf(false) }

        if (showProfilePhotoPicker) {
            com.ptpws.ikikasir.commond.ImageSourcePickerDialog(
                onImageSelected = { uri ->
                    pendingProfilePhotoUri = uri
                    editPhotoUrl = uri.toString()
                    showProfilePhotoPicker = false
                },
                onDismiss = { showProfilePhotoPicker = false },
                uploadType = com.ptpws.ikikasir.commond.ImageUploadType.FOTO_PROFIL,
                targetSubfolder = "profile_photos"
            )
        }

        Dialog(onDismissRequest = { if (!isSavingProfile) showEditProfilDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edit Profil",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        IconButton(
                            onClick = { showEditProfilDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Photo preview & Upload button
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB))
                            .border(2.5.dp, Color(0xFFEEF2FF), CircleShape)
                            .clickable { showProfilePhotoPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (editPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = editPhotoUrl,
                                contentDescription = "Foto Profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Avatar",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showProfilePhotoPicker = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Pilih / Ubah Foto Profil", fontFamily = interfamily, fontSize = 12.sp)
                    }

                    // Field Nama Lengkap
                    GlobalTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = "Nama Lengkap",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Email Readonly
                    GlobalTextField(
                        value = currentUser?.email.orEmpty(),
                        onValueChange = {},
                        enabled = false,
                        label = "Email (Tersinkron)",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role Readonly
                    GlobalTextField(
                        value = userProfileState.roleName,
                        onValueChange = {},
                        enabled = false,
                        label = "Role / Hak Akses",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditProfilDialog = false },
                            enabled = !isSavingProfile,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal", fontFamily = interfamily, color = Color(0xFF475569))
                        }

                        Button(
                            onClick = {
                                isSavingProfile = true
                                val onComplete: (Boolean) -> Unit = { success ->
                                    isSavingProfile = false
                                    if (success) showEditProfilDialog = false
                                }
                                val pendingPhoto = pendingProfilePhotoUri
                                if (pendingPhoto == null) {
                                    viewModel.updateUserProfile(editFullName, editPhotoUrl, onComplete)
                                } else {
                                    viewModel.updateUserProfileWithPhoto(pendingPhoto, editFullName, onComplete)
                                }
                            },
                            enabled = !isSavingProfile && editFullName.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                        ) {
                            if (isSavingProfile) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Simpan", fontFamily = interfamily, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSecurityDialog) {
        ChangePasswordDialog(
            state = securityState,
            onDismiss = {
                if (!securityState.isLoading) {
                    showSecurityDialog = false
                    securityViewModel.clearForm()
                }
            },
            onCurrentPasswordChange = securityViewModel::updateCurrentPassword,
            onNewPasswordChange = securityViewModel::updateNewPassword,
            onConfirmPasswordChange = securityViewModel::updateConfirmPassword,
            onSubmit = securityViewModel::changePassword
        )
    }

    if (showTentangAplikasiDialog) {
        TentangAplikasiDialog(
            onDismiss = { showTentangAplikasiDialog = false }
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    state: com.ptpws.ikikasir.feature.auth.presentation.state.SecurityState,
    onDismiss: () -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ubah Kata Sandi",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Masukkan kata sandi saat ini, lalu tentukan kata sandi baru.",
                    fontFamily = interfamily,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                PasswordInput(
                    label = "Kata sandi saat ini",
                    value = state.currentPassword,
                    visible = showCurrentPassword,
                    enabled = !state.isLoading,
                    onValueChange = onCurrentPasswordChange,
                    onToggleVisibility = { showCurrentPassword = !showCurrentPassword }
                )
                PasswordInput(
                    label = "Kata sandi baru (minimal 6 karakter)",
                    value = state.newPassword,
                    visible = showNewPassword,
                    enabled = !state.isLoading,
                    onValueChange = onNewPasswordChange,
                    onToggleVisibility = { showNewPassword = !showNewPassword }
                )
                PasswordInput(
                    label = "Konfirmasi kata sandi baru",
                    value = state.confirmPassword,
                    visible = showConfirmPassword,
                    enabled = !state.isLoading,
                    onValueChange = onConfirmPasswordChange,
                    onToggleVisibility = { showConfirmPassword = !showConfirmPassword }
                )
                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", fontFamily = interfamily)
                    }
                    Button(
                        onClick = onSubmit,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Simpan", fontFamily = interfamily, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordInput(
    label: String,
    value: String,
    visible: Boolean,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit
) {
    GlobalTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        enabled = enabled,
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility, enabled = enabled) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (visible) "Sembunyikan kata sandi" else "Tampilkan kata sandi",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
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
