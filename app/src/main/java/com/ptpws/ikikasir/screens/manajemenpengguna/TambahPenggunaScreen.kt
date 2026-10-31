package com.ptpws.ikikasir.screens.manajemenpengguna

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.ptpws.ikikasir.commond.GlobalTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import com.ptpws.ikikasir.commond.GlobalImageUploadOverlay
import com.ptpws.ikikasir.commond.GlobalUploadProgress
import com.ptpws.ikikasir.commond.ImageUploadType
import com.ptpws.ikikasir.core.util.ImageCompressorHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ptpws.ikikasir.commond.interfamily
import androidx.activity.compose.BackHandler
import com.ptpws.ikikasir.core.responsive.presentation.util.LocalWindowSizeInfo
import com.ptpws.ikikasir.feature.manajemenpengguna.presentation.viewmodel.UserViewModel
import com.ptpws.ikikasir.feature.role.presentation.viewmodel.RoleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahPenggunaScreen(
    userId: String? = null,
    onBack: () -> Unit = {},
    onSimpanPengguna: () -> Unit = {},
    viewModel: UserViewModel = hiltViewModel(),
    roleViewModel: RoleViewModel = hiltViewModel()
) {
    var isBackProcessing by remember { mutableStateOf(false) }
    val handleBack = {
        if (!isBackProcessing) {
            isBackProcessing = true
            onBack()
        }
    }
    BackHandler(enabled = !isBackProcessing, onBack = handleBack)

    val windowSizeInfo = LocalWindowSizeInfo.current
    val isCompact = windowSizeInfo.isCompactPhone

    val formState by viewModel.formState.collectAsState()

    val roleListState by roleViewModel.listState.collectAsState()
    val availableRoles = roleListState.roles
    val context = LocalContext.current
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    var kataSandiSaatIniTerlihat by remember { mutableStateOf(false) }
    var kataSandiTerlihat by remember { mutableStateOf(false) }
    var konfirmasiKataSandiTerlihat by remember { mutableStateOf(false) }
    var isSendingResetEmail by remember { mutableStateOf(false) }
    var resetEmailSuccessMessage by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(userId) {
        resetEmailSuccessMessage = null
        if (!userId.isNullOrEmpty()) {
            viewModel.loadUserById(userId)
        } else {
            // Hanya reset jika sebelumnya form memuat data pengguna edit (id tidak kosong)
            if (formState.id.isNotBlank()) {
                viewModel.resetForm()
                profileImageUri = null
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf<Float?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isUploadingImage = true
                uploadProgress = 0.15f
                GlobalUploadProgress.showProfilePhotoUpload(progress = 0.15f, message = "Memeriksa foto profil...")
                try {
                    val compressedFile = ImageCompressorHelper.processAndCompressImage(
                        context = context,
                        sourceUri = uri,
                        targetSubfolder = "user_images",
                        onProgress = { p, msg ->
                            uploadProgress = p
                            GlobalUploadProgress.updateProgress(p, msg)
                        }
                    )
                    val permanentUri = Uri.fromFile(compressedFile)
                    profileImageUri = permanentUri
                    viewModel.onPhotoUrlChange(permanentUri.toString())
                    localError = null
                } catch (e: Exception) {
                    localError = "Gagal memproses gambar: ${e.message}"
                } finally {
                    delay(150)
                    isUploadingImage = false
                    uploadProgress = null
                    GlobalUploadProgress.dismiss()
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (formState.id.isBlank()) "Tambah Pengguna" else "Edit Pengguna",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        fontSize = if (isCompact) 18.sp else 20.sp,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF3B82F6)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC),
                    titleContentColor = Color(0xFF1E293B),
                    navigationIconContentColor = Color(0xFF3B82F6)
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = if (isCompact) 12.dp else 16.dp,
                end = if (isCompact) 12.dp else 16.dp,
                top = 8.dp,
                bottom = if (isCompact) 40.dp else 56.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 16.dp)
        ) {

            // ── Upload Foto Profil
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier.clickable(enabled = !isUploadingImage) {
                            imagePickerLauncher.launch("image/*")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) 76.dp else 90.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF6FF))
                                .border(1.5.dp, Color(0xFFBFDBFE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoModel = profileImageUri ?: formState.photoUrl.ifBlank { null }
                            if (photoModel != null) {
                                AsyncImage(
                                    model = photoModel,
                                    contentDescription = "Foto Profil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PersonAddAlt,
                                    contentDescription = "Unggah Foto Profil",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(if (isCompact) 30.dp else 36.dp)
                                )
                            }

                            // Circular Progress Indicator Overlay untuk Foto Profil
                            GlobalImageUploadOverlay(
                                isUploading = isUploadingImage,
                                progress = uploadProgress,
                                shape = CircleShape,
                                size = if (isCompact) 56.dp else 66.dp,
                                type = ImageUploadType.FOTO_PROFIL,
                                title = null,
                                message = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(if (isCompact) 24.dp else 28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Ubah Foto",
                                tint = Color.White,
                                modifier = Modifier.size(if (isCompact) 12.dp else 14.dp)
                            )
                        }
                    }
                    Text(
                        text = "Unggah Foto Profil (Maks. 2MB - Auto Kompresi)",
                        fontFamily = interfamily,
                        fontSize = if (isCompact) 12.sp else 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }


            // ── Error Message Banner
            val errorMessage = localError ?: formState.error
            if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFDC2626),
                            fontFamily = interfamily,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // ── Card Form utama
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (isCompact) 12.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 16.dp)
                    ) {

                        // Nama Lengkap
                        GlobalTextField(
                            value = formState.fullName,
                            onValueChange = {
                                localError = null
                                viewModel.onFullNameChange(it)
                            },
                            placeholder = "Contoh: Budi Santoso",
                            label = "Nama Lengkap",
                            isRequired = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        // Alamat Email
                        GlobalTextField(
                            value = formState.email,
                            onValueChange = {
                                localError = null
                                viewModel.onEmailChange(it)
                            },
                            placeholder = "email@domain.com",
                            label = "Alamat Email",
                            isRequired = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        // Role
                        Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp)) {
                            Text(
                                text = "Role",
                                fontFamily = interfamily,
                                fontSize = if (isCompact) 12.sp else 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )

                            val selectedRoleName = availableRoles.find {
                                it.id.equals(formState.roleId, ignoreCase = true) || it.name.equals(formState.roleId, ignoreCase = true)
                            }?.name ?: if (formState.roleId.isNotBlank()) formState.roleId else ""

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isCompact) 44.dp else 48.dp)
                                        .clickable { roleDropdownExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = if (isCompact) 12.dp else 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (selectedRoleName.isBlank()) "Pilih role" else selectedRoleName,
                                            fontSize = if (isCompact) 12.sp else 14.sp,
                                            fontFamily = interfamily,
                                            color = if (selectedRoleName.isBlank()) Color(0xFF94A3B8) else Color(0xFF0F172A),
                                            fontWeight = if (selectedRoleName.isBlank()) FontWeight.Normal else FontWeight.Medium
                                        )
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Pilih role",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = roleDropdownExpanded,
                                    onDismissRequest = { roleDropdownExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .background(Color.White)
                                ) {
                                    if (availableRoles.isEmpty()) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Belum ada data role (Tambah role terlebih dahulu)",
                                                    fontSize = 13.sp,
                                                    fontFamily = interfamily,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            },
                                            onClick = { roleDropdownExpanded = false }
                                        )
                                    } else {
                                        availableRoles.forEach { role ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = role.name,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        fontFamily = interfamily,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                },
                                                onClick = {
                                                    localError = null
                                                    viewModel.onRoleChange(role.name)
                                                    roleDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val isEdit = formState.id.isNotBlank()
                        val isEmailChanged = isEdit && formState.initialEmail.isNotBlank() && !formState.initialEmail.trim().equals(formState.email.trim(), ignoreCase = true)



                        // Kata Sandi Saat Ini (Hanya tampil saat Edit Pengguna)
                        if (isEdit) {
                            Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Kata Sandi Saat Ini",
                                        fontFamily = interfamily,
                                        fontSize = if (isCompact) 12.sp else 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155)
                                    )
                                    if (formState.email.isNotBlank()) {
                                        Text(
                                            text = if (isSendingResetEmail) "Mengirim link..." else "Lupa kata sandi?",
                                            fontFamily = interfamily,
                                            fontSize = if (isCompact) 11.sp else 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF3B82F6),
                                            modifier = Modifier.clickable(enabled = !isSendingResetEmail) {
                                                isSendingResetEmail = true
                                                viewModel.sendPasswordResetEmail(formState.email) { success, msg ->
                                                    isSendingResetEmail = false
                                                    if (success) {
                                                        resetEmailSuccessMessage = msg
                                                        localError = null
                                                    } else {
                                                        localError = msg
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                                GlobalTextField(
                                    value = formState.currentPassword,
                                    onValueChange = {
                                        localError = null
                                        viewModel.onCurrentPasswordChange(it)
                                    },
                                    placeholder = if (isEmailChanged) "Wajib diisi jika tidak mengubah kata sandi baru" else "Wajib diisi jika ingin mengubah kata sandi",
                                    isPassword = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFF6B7280),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                if (resetEmailSuccessMessage != null) {
                                    Text(
                                        text = resetEmailSuccessMessage!!,
                                        fontFamily = interfamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                        }

                        // Kata Sandi
                        GlobalTextField(
                            value = formState.password,
                            onValueChange = {
                                localError = null
                                viewModel.onPasswordChange(it)
                            },
                            placeholder = if (isEdit) "Kosongkan jika tidak ingin mengubah sandi" else "Min. 6 karakter",
                            label = if (isEmailChanged) "Kata Sandi Baru (Opsional jika sandi saat ini diisi)" else if (isEdit) "Kata Sandi Baru (Opsional)" else "Kata Sandi",
                            isRequired = !isEdit,
                            isPassword = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        // Konfirmasi Kata Sandi
                        GlobalTextField(
                            value = formState.confirmPassword,
                            onValueChange = {
                                localError = null
                                viewModel.onConfirmPasswordChange(it)
                            },
                            placeholder = if (isEdit) "Ulangi kata sandi baru" else "Ulangi kata sandi",
                            label = if (isEdit) "Konfirmasi Kata Sandi (Opsional)" else "Konfirmasi Kata Sandi",
                            isRequired = !isEdit,
                            isPassword = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // ── Status Akun (Aktif / Nonaktif)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (isCompact) 12.dp else 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (formState.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                modifier = Modifier.size(if (isCompact) 36.dp else 40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (formState.isActive) Icons.Outlined.CheckCircle else Icons.Outlined.Block,
                                        contentDescription = null,
                                        tint = if (formState.isActive) Color(0xFF16A34A) else Color(0xFFDC2626),
                                        modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Status Akun",
                                    fontFamily = interfamily,
                                    fontSize = if (isCompact) 13.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (formState.isActive) "Akun Aktif (Dapat Login)" else "Akun Nonaktif (Akses Diblokir)",
                                    fontFamily = interfamily,
                                    fontSize = if (isCompact) 11.sp else 12.sp,
                                    color = if (formState.isActive) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }
                        }

                        Switch(
                            checked = formState.isActive,
                            onCheckedChange = { viewModel.onActiveChange(it) },
                            modifier = Modifier.scale(if (isCompact) 0.75f else 0.85f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF16A34A),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }
            }

            // ── Info Catatan Email
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(if (isCompact) 14.dp else 16.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = "Pastikan alamat email yang didaftarkan aktif untuk keperluan verifikasi dan pemulihan kata sandi di masa mendatang.",
                        fontFamily = interfamily,
                        fontSize = if (isCompact) 11.sp else 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = if (isCompact) 15.sp else 16.sp
                    )
                }
            }

            // ── Tombol Simpan Pengguna
            item {
                val isEdit = formState.id.isNotBlank()
                val isEmailChanged = isEdit && formState.initialEmail.isNotBlank() && !formState.initialEmail.trim().equals(formState.email.trim(), ignoreCase = true)
                Button(
                    onClick = {
                        if (formState.fullName.isBlank()) {
                            localError = "Nama lengkap tidak boleh kosong"
                            return@Button
                        }
                        if (formState.email.isBlank()) {
                            localError = "Alamat email tidak boleh kosong"
                            return@Button
                        }
                        if (!formState.email.contains("@")) {
                            localError = "Format email tidak valid (harus mengandung '@')"
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(formState.email.trim()).matches()) {
                            localError = "Format email tidak valid (contoh: nama@domain.com)"
                            return@Button
                        }
                        if (formState.roleId.isBlank()) {
                            localError = "Pilih role terlebih dahulu"
                            return@Button
                        }
                        if (!isEdit) {
                            if (formState.password.isBlank()) {
                                localError = "Kata sandi tidak boleh kosong"
                                return@Button
                            }
                            if (formState.password.length < 6) {
                                localError = "Kata sandi minimal 6 karakter"
                                return@Button
                            }
                            if (formState.confirmPassword.isBlank()) {
                                localError = "Konfirmasi kata sandi tidak boleh kosong"
                                return@Button
                            }
                            if (formState.password != formState.confirmPassword) {
                                localError = "Konfirmasi kata sandi tidak cocok"
                                return@Button
                            }
                        } else {
                            if (isEmailChanged && formState.password.isBlank() && formState.currentPassword.isBlank()) {
                                localError = "Mengubah email memerlukan kata sandi saat ini atau kata sandi baru untuk sinkronisasi akun login Firebase"
                                return@Button
                            }
                            if (formState.password.isNotBlank() || formState.confirmPassword.isNotBlank()) {
                                if (formState.password.isBlank()) {
                                    localError = "Kata sandi baru tidak boleh kosong"
                                    return@Button
                                }
                                if (formState.password.length < 6) {
                                    localError = "Kata sandi minimal 6 karakter"
                                    return@Button
                                }
                                if (formState.confirmPassword.isBlank()) {
                                    localError = "Konfirmasi kata sandi baru tidak boleh kosong"
                                    return@Button
                                }
                                if (formState.password != formState.confirmPassword) {
                                    localError = "Konfirmasi kata sandi tidak cocok"
                                    return@Button
                                }
                                if (formState.currentPassword.isBlank()) {
                                    localError = "Masukkan kata sandi saat ini untuk mengubah kata sandi, atau gunakan opsi 'Lupa kata sandi?' di atas"
                                    return@Button
                                }
                            }
                        }
                        localError = null
                        viewModel.saveUser {
                            onSimpanPengguna()
                        }
                    },
                    enabled = !formState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (isCompact) 12.dp else 16.dp)
                        .height(if (isCompact) 46.dp else 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEdit) Color(0xFF16A34A) else Color(0xFF2563EB)
                    )
                ) {
                    if (formState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(if (isCompact) 16.dp else 18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEdit) "Simpan Perubahan" else "Simpan Pengguna",
                            fontFamily = interfamily,
                            fontSize = if (isCompact) 14.sp else 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ── Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TambahPenggunaScreenPreview() {
    MaterialTheme {
        TambahPenggunaScreen()
    }
}

private fun saveUserImageToInternalStorage(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return uri.toString()
        val imagesDir = File(context.filesDir, "user_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val fileName = "user_img_" + System.currentTimeMillis() + ".jpg"
        val destinationFile = File(imagesDir, fileName)
        val outputStream = FileOutputStream(destinationFile)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(destinationFile).toString()
    } catch (e: Exception) {
        uri.toString()
    }
}
