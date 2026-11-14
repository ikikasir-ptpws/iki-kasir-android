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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptpws.ikikasir.commond.GlobalImagePickerDialog
import com.ptpws.ikikasir.commond.GlobalTextField
import com.ptpws.ikikasir.commond.GlobalUploadProgress
import com.ptpws.ikikasir.commond.ImageUploadType
import com.ptpws.ikikasir.commond.SyncStatusBadge
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.commond.rememberGlobalImagePicker
import kotlinx.coroutines.delay
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.NotaSettingPreferences
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.PaymentMethodSettingPreferences
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.TableSettingPreferences
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.TaxSettingPreferences
import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

import androidx.hilt.navigation.compose.hiltViewModel
import com.ptpws.ikikasir.feature.pengaturan.presentation.viewmodel.ProfilUiProvider
import com.ptpws.ikikasir.feature.pengaturan.presentation.viewmodel.HiltProfilUiProvider
import com.ptpws.ikikasir.feature.auth.presentation.viewmodel.HiltSecurityUiProvider
import com.ptpws.ikikasir.feature.pengaturan.presentation.viewmodel.ProfilViewModel
import com.ptpws.ikikasir.feature.auth.presentation.viewmodel.SecurityUiProvider
import com.ptpws.ikikasir.feature.auth.presentation.viewmodel.SecurityViewModel
import com.ptpws.ikikasir.feature.tentangaplikasi.TentangAplikasiDialog
import com.ptpws.ikikasir.screens.navigation.AppScreen
import androidx.activity.compose.BackHandler
import com.ptpws.ikikasir.screens.navigation.popBackStackSafely

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    navController: NavController,
    viewModel: ProfilUiProvider = HiltProfilUiProvider(hiltViewModel<ProfilViewModel>()),
    securityViewModel: SecurityUiProvider = HiltSecurityUiProvider(hiltViewModel<SecurityViewModel>()),
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
    val isOnline by viewModel.isOnline.collectAsState()

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

    val tablePrefs = remember { TableSettingPreferences(context) }
    val taxPrefs = remember { TaxSettingPreferences(context) }
    val paymentMethodPrefs = remember { PaymentMethodSettingPreferences(context) }
    val notaPrefs = remember { NotaSettingPreferences(context) }

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
                        if (user?.isSynced == false) {
                            SyncStatusBadge()
                        }

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
                                badgeLabel = if (isOnline) null else "Perlu online",
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
                                badgeLabel = if (!livePaymentMethodSetting.isSynced) "Pending" else null,
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
                                badgeLabel = if (!liveTaxSetting.isSynced) "Pending" else null,
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
                                badgeLabel = if (!liveNotaSetting.isSynced) "Pending" else null,
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
                                badgeLabel = if (!liveTableSetting.isSynced) "Pending" else null,
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
        val currentSetting = if (livePaymentMethodSetting != PaymentMethodSetting()) {
            livePaymentMethodSetting
        } else {
            paymentMethodPrefs.getSetting()
        }
        MetodePembayaranDialog(
            initialSetting = currentSetting,
            onDismiss = { showMetodePembayaranDialog = false },
            onSave = { updatedSetting ->
                paymentMethodPrefs.saveSetting(updatedSetting)
                viewModel.savePaymentMethodSetting(updatedSetting)
                showMetodePembayaranDialog = false
            }
        )
    }

    // ── Dialog Setting PPN / Pajak
    if (showPpnDialog) {
        val currentSetting = if (liveTaxSetting != TaxSetting()) {
            liveTaxSetting
        } else {
            taxPrefs.getSetting()
        }
        TaxSettingDialog(
            initialSetting = currentSetting,
            onDismiss = { showPpnDialog = false },
            onSave = { updatedSetting ->
                taxPrefs.saveSetting(updatedSetting)
                viewModel.saveTaxSetting(updatedSetting)
                showPpnDialog = false
            }
        )
    }

    // ── Dialog Setting Nota / Struk Pembayaran
    if (showStrukDialog) {
        val currentSetting = if (liveNotaSetting.storeName.isNotBlank() ||
            liveNotaSetting.storeAddress.isNotBlank() ||
            liveNotaSetting.storePhone.isNotBlank() ||
            liveNotaSetting.logoUrl.isNotBlank() ||
            liveNotaSetting.wifiName.isNotBlank()
        ) {
            liveNotaSetting
        } else {
            val pref = notaPrefs.getSetting()
            if (pref.storeName.isNotBlank() || pref.storeAddress.isNotBlank() || pref.storePhone.isNotBlank() || pref.logoUrl.isNotBlank() || pref.wifiName.isNotBlank()) {
                pref
            } else {
                liveNotaSetting
            }
        }
        NotaSettingDialog(
            initialSetting = currentSetting,
            onDismiss = { showStrukDialog = false },
            onSave = { updatedSetting ->
                notaPrefs.saveSetting(updatedSetting)
                viewModel.saveNotaSetting(updatedSetting)
                showStrukDialog = false
            }
        )
    }

    // ── Dialog Setting Meja
    if (showMejaDialog) {
        val currentSetting = if (liveTableSetting != TableSetting()) {
            liveTableSetting
        } else {
            tablePrefs.getSetting()
        }
        TableSettingDialog(
            initialSetting = currentSetting,
            onDismiss = { showMejaDialog = false },
            onSave = { updatedSetting ->
                tablePrefs.setTableEnabled(updatedSetting.isTableEnabled)
                viewModel.saveTableSetting(updatedSetting)
                showMejaDialog = false
            }
        )
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
        var isSavingProfile by remember { mutableStateOf(false) }

        val profileImagePickerState = rememberGlobalImagePicker(
            onImageProcessed = { permanentUri ->
                editPhotoUrl = permanentUri.toString()
            },
            targetSubfolder = "user_images",
            uploadType = ImageUploadType.FOTO_PROFIL,
            progressMessage = "Memeriksa foto profil..."
        )

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
                        if (currentUser?.isSynced == false) SyncStatusBadge()
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
                            .clickable { profileImagePickerState.openPicker() },
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
                        onClick = { profileImagePickerState.openPicker() },
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
                                .heightIn(min = 46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal", fontFamily = interfamily, color = Color(0xFF475569))
                        }

                        Button(
                            onClick = {
                                isSavingProfile = true
                                viewModel.updateUserProfile(editFullName, editPhotoUrl) {
                                    isSavingProfile = false
                                    showEditProfilDialog = false
                                }
                            },
                            enabled = !isSavingProfile && editFullName.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp),
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

            // Dialog Pilihan Sumber Foto Profil
            GlobalImagePickerDialog(
                show = profileImagePickerState.showDialog,
                onDismissRequest = profileImagePickerState.closePicker,
                onCameraSelect = profileImagePickerState.launchCamera,
                onGallerySelect = profileImagePickerState.launchGallery,
                title = "Pilih Foto Profil",
                description = "Pilih metode pengambilan foto profil:"
            )
        }
    }

    if (showSecurityDialog) {
        ChangePasswordDialog(
            state = securityState,
            isOnline = isOnline,
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
private fun MetodePembayaranDialog(
    initialSetting: PaymentMethodSetting,
    onDismiss: () -> Unit,
    onSave: (PaymentMethodSetting) -> Unit
) {
    var isTunaiAktif by remember(initialSetting) { mutableStateOf(initialSetting.isTunaiEnabled) }
    var isQrisAktif by remember(initialSetting) { mutableStateOf(initialSetting.isQrisEnabled) }
    var isTransferAktif by remember(initialSetting) { mutableStateOf(initialSetting.isTransferEnabled) }
    var isKartuKreditAktif by remember(initialSetting) { mutableStateOf(initialSetting.isKartuKreditEnabled) }
    val dialogScrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
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
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.85f)
                    .verticalScroll(dialogScrollState)
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pengaturan Metode Pembayaran",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!initialSetting.isSynced) SyncStatusBadge()
                    }
                    IconButton(
                        onClick = onDismiss,
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
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
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
                            val updatedSetting = initialSetting.copy(
                                isTunaiEnabled = isTunaiAktif,
                                isQrisEnabled = isQrisAktif,
                                isTransferEnabled = isTransferAktif,
                                isKartuKreditEnabled = isKartuKreditAktif
                            )
                            onSave(updatedSetting)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
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

@Composable
private fun TaxSettingDialog(
    initialSetting: TaxSetting,
    onDismiss: () -> Unit,
    onSave: (TaxSetting) -> Unit
) {
    var isPpnAktif by remember(initialSetting) { mutableStateOf(initialSetting.isActive) }
    var ppnPersen by remember(initialSetting) {
        mutableStateOf(
            initialSetting.percentage.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            }
        )
    }
    var isPpnInklusif by remember(initialSetting) {
        mutableStateOf(initialSetting.type == TaxSetting.TAX_TYPE_INCLUSIVE)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    // Responsif: isi dialog bisa digulir agar tidak terpotong di layar pendek / font besar
                    .verticalScroll(rememberScrollState()),
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
                    if (!initialSetting.isSynced) SyncStatusBadge()
                    IconButton(
                        onClick = onDismiss,
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
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text("Batal", fontFamily = interfamily, color = Color(0xFF4B5563))
                    }
                    Button(
                        onClick = {
                            val parsedPercent = ppnPersen.toDoubleOrNull() ?: 11.0
                            val updatedSetting = initialSetting.copy(
                                isActive = isPpnAktif,
                                percentage = parsedPercent,
                                type = if (isPpnInklusif) TaxSetting.TAX_TYPE_INCLUSIVE
                                       else TaxSetting.TAX_TYPE_EXCLUSIVE
                            )
                            onSave(updatedSetting)
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

@Composable
private fun NotaSettingDialog(
    initialSetting: NotaSetting,
    onDismiss: () -> Unit,
    onSave: (NotaSetting) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val strukScrollState = rememberScrollState()

    var namaToko by remember(initialSetting) { mutableStateOf(initialSetting.storeName) }
    var alamatToko by remember(initialSetting) { mutableStateOf(initialSetting.storeAddress) }
    var nomorHpToko by remember(initialSetting) { mutableStateOf(initialSetting.storePhone) }
    var namaWifi by remember(initialSetting) { mutableStateOf(initialSetting.wifiName) }
    var kataSandiWifi by remember(initialSetting) { mutableStateOf(initialSetting.wifiPassword) }
    var ukuranKertas by remember(initialSetting) { mutableStateOf(if (initialSetting.paperWidth.isNotBlank()) initialSetting.paperWidth else "58mm") }
    var logoUrl by remember(initialSetting) { mutableStateOf(initialSetting.logoUrl) }
    var logoLocalPath by remember(initialSetting) {
        mutableStateOf(
            initialSetting.logoLocalPath.ifBlank {
                initialSetting.logoUrl.takeIf {
                    Uri.parse(it).scheme.equals("file", ignoreCase = true)
                }.orEmpty()
            }
        )
    }

    val logoPickerState = rememberGlobalImagePicker(
        onImageProcessed = { permanentUri ->
            coroutineScope.launch {
                GlobalUploadProgress.showLogoUpload(progress = 0.15f, message = "Menyesuaikan format logo...")
                var resizedLogoFile: File? = null
                try {
                    val compressedBitmap = BitmapFactory.decodeFile(permanentUri.path)
                        ?: throw IllegalStateException("Gagal memproses gambar logo")
                    val maxDimension = 800
                    val scale = minOf(
                        maxDimension.toFloat() / compressedBitmap.width,
                        maxDimension.toFloat() / compressedBitmap.height,
                        1f
                    )
                    val targetWidth = (compressedBitmap.width * scale).toInt().coerceAtLeast(1)
                    val targetHeight = (compressedBitmap.height * scale).toInt().coerceAtLeast(1)
                    val scaledBitmap = if (scale < 1f) {
                        Bitmap.createScaledBitmap(compressedBitmap, targetWidth, targetHeight, true)
                    } else {
                        compressedBitmap
                    }

                    GlobalUploadProgress.updateProgress(0.25f, "Menyimpan logo di perangkat...")
                    val logoDirectory = File(context.filesDir, "store_logo")
                    if (!logoDirectory.exists() && !logoDirectory.mkdirs()) {
                        throw IllegalStateException("Gagal membuat penyimpanan logo lokal")
                    }
                    resizedLogoFile = File(logoDirectory, "store_logo_${System.currentTimeMillis()}.png")
                    FileOutputStream(resizedLogoFile).use { output ->
                        check(scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                            "Gagal menyimpan gambar logo"
                        }
                    }
                    val localLogoUri = Uri.fromFile(resizedLogoFile).toString()
                    logoUrl = localLogoUri
                    logoLocalPath = localLogoUri
                    GlobalUploadProgress.updateProgress(1f, "Logo tersimpan dan akan disinkronkan saat online.")
                    delay(200)
                } catch (e: Exception) {
                    Toast.makeText(context, "Terjadi kesalahan saat memproses logo: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    GlobalUploadProgress.dismiss()
                }
            }
        },
        targetSubfolder = "store_logo",
        uploadType = ImageUploadType.LOGO_STRUK,
        progressMessage = "Memeriksa ukuran logo..."
    )

    Dialog(onDismissRequest = onDismiss) {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Setting Nota / Struk",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF111827)
                        )
                        if (!initialSetting.isSynced) SyncStatusBadge()
                    }
                    IconButton(
                        onClick = onDismiss,
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

                // Upload Logo Nota / Struk
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

                    if (logoUrl.isNotBlank() || logoLocalPath.isNotBlank()) {
                        val displayLogo = if (
                            logoUrl.startsWith("http", ignoreCase = true) ||
                            logoUrl.startsWith("gs://", ignoreCase = true)
                        ) logoUrl else logoLocalPath.ifBlank { logoUrl }
                        val logoModel = remember(displayLogo) {
                            if (displayLogo.startsWith("data:image")) {
                                try {
                                    android.util.Base64.decode(displayLogo.substringAfter("base64,"), android.util.Base64.DEFAULT)
                                } catch (_: Exception) {
                                    displayLogo
                                }
                            } else {
                                displayLogo
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
                                        onClick = { logoPickerState.openPicker() },
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
                                            logoLocalPath = ""
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
                                .clickable { logoPickerState.openPicker() },
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
                    value = nomorHpToko,
                    onValueChange = { nomorHpToko = it },
                    label = "Nomor HP Toko",
                    placeholder = "Masukkan nomor HP toko",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
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
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text("Batal", fontFamily = interfamily, color = Color(0xFF4B5563))
                    }
                    Button(
                        onClick = {
                            val updatedSetting = initialSetting.copy(
                                storeName    = namaToko,
                                storeAddress = alamatToko,
                                storePhone   = nomorHpToko,
                                wifiName     = namaWifi,
                                wifiPassword = kataSandiWifi,
                                paperWidth   = ukuranKertas,
                                logoUrl      = logoUrl,
                                logoLocalPath = logoLocalPath
                            )
                            onSave(updatedSetting)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text("Simpan", fontFamily = interfamily, color = Color.White)
                    }
                }
            }

            // Dialog Pilihan Sumber Logo Struk
            GlobalImagePickerDialog(
                show = logoPickerState.showDialog,
                onDismissRequest = logoPickerState.closePicker,
                onCameraSelect = logoPickerState.launchCamera,
                onGallerySelect = logoPickerState.launchGallery,
                title = "Pilih Logo Struk",
                description = "Pilih metode pengambilan gambar logo toko:"
            )
        }
    }
}

@Composable
private fun TableSettingDialog(
    initialSetting: TableSetting,
    onDismiss: () -> Unit,
    onSave: (TableSetting) -> Unit
) {
    var isMejaAktif by remember(initialSetting) { mutableStateOf(initialSetting.isTableEnabled) }

    Dialog(onDismissRequest = onDismiss) {
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
                    .padding(22.dp)
                    // Responsif: isi dialog bisa digulir agar tidak terpotong di layar pendek / font besar
                    .verticalScroll(rememberScrollState()),
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
                    if (!initialSetting.isSynced) SyncStatusBadge()
                    IconButton(
                        onClick = onDismiss,
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
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
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
                            val updatedSetting = initialSetting.copy(
                                isTableEnabled = isMejaAktif
                            )
                            onSave(updatedSetting)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
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

@Composable
private fun ChangePasswordDialog(
    state: com.ptpws.ikikasir.feature.auth.presentation.state.SecurityState,
    isOnline: Boolean,
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
                    .padding(22.dp)
                    // Responsif: isi dialog bisa digulir agar tidak terpotong di layar pendek / font besar
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ubah Kata Sandi",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                    if (!isOnline) SyncStatusBadge(label = "Perlu online")
                }
                Text(
                    text = if (isOnline) {
                        "Masukkan kata sandi saat ini, lalu tentukan kata sandi baru."
                    } else {
                        "Perubahan kata sandi perlu koneksi internet dan tidak dapat disimpan sebagai pending."
                    },
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
    badgeLabel: String? = null,
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
            modifier = Modifier.weight(1f),
            // Responsif: label menu panjang dibungkus maksimal 2 baris agar badge & chevron tetap terlihat
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        badgeLabel?.let { labelText ->
            if (labelText == "Pending") {
                SyncStatusBadge(label = labelText)
            } else {
                Text(
                    text = labelText,
                    fontFamily = interfamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309),
                    modifier = Modifier
                        .background(Color(0xFFFFF7ED), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
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
        ProfilScreen(
            navController = rememberNavController(),
            viewModel = com.ptpws.ikikasir.commond.PreviewProfilProvider(),
            securityViewModel = com.ptpws.ikikasir.commond.PreviewSecurityProvider()
        )
    }
}
