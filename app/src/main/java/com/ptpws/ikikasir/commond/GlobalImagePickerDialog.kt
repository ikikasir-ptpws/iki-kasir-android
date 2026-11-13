package com.ptpws.ikikasir.commond

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.ptpws.ikikasir.core.util.ImageCompressorHelper
import kotlinx.coroutines.launch

/**
 * State manager untuk mengontrol Dialog Pilihan Upload Gambar/Logo (Kamera & Galeri)
 * serta mengurusi peluncuran kamera (ActivityResultContracts.TakePicture) dan
 * galeri (ActivityResultContracts.GetContent) secara otomatis dan seragam.
 */
class GlobalImagePickerState(
    val showDialog: Boolean,
    val openPicker: () -> Unit,
    val closePicker: () -> Unit,
    val launchCamera: () -> Unit,
    val launchGallery: () -> Unit
)

@Composable
fun rememberGlobalImagePicker(
    context: Context = LocalContext.current,
    onImageProcessed: (Uri) -> Unit,
    targetSubfolder: String = "general_uploads",
    uploadType: ImageUploadType = ImageUploadType.GENERAL,
    progressMessage: String = "Memproses gambar..."
): GlobalImagePickerState {
    var showDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // 1. Launcher Kamera (TakePicture)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val uri = tempCameraUri!!
            coroutineScope.launch {
                GlobalUploadProgress.show(
                    title = "Upload Gambar",
                    message = progressMessage,
                    type = uploadType,
                    progress = 0.15f
                )
                try {
                    val compressedFile = ImageCompressorHelper.processAndCompressImage(
                        context = context,
                        sourceUri = uri,
                        targetSubfolder = targetSubfolder,
                        onProgress = { p, msg ->
                            GlobalUploadProgress.updateProgress(p, msg)
                        }
                    )
                    onImageProcessed(Uri.fromFile(compressedFile))
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal memproses foto: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    GlobalUploadProgress.dismiss()
                }
            }
        }
    }

    // 2. Launcher Izin Kamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                val (_, uri) = ImageCompressorHelper.createCameraTempFileUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto.", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Launcher Galeri (GetContent)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                GlobalUploadProgress.show(
                    title = "Upload Gambar",
                    message = progressMessage,
                    type = uploadType,
                    progress = 0.15f
                )
                try {
                    val compressedFile = ImageCompressorHelper.processAndCompressImage(
                        context = context,
                        sourceUri = uri,
                        targetSubfolder = targetSubfolder,
                        onProgress = { p, msg ->
                            GlobalUploadProgress.updateProgress(p, msg)
                        }
                    )
                    onImageProcessed(Uri.fromFile(compressedFile))
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal memproses gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    GlobalUploadProgress.dismiss()
                }
            }
        }
    }

    val launchCamera: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                val (_, uri) = ImageCompressorHelper.createCameraTempFileUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val launchGallery: () -> Unit = {
        galleryLauncher.launch("image/*")
    }

    return remember(showDialog) {
        GlobalImagePickerState(
            showDialog = showDialog,
            openPicker = { showDialog = true },
            closePicker = { showDialog = false },
            launchCamera = launchCamera,
            launchGallery = launchGallery
        )
    }
}

/**
 * Dialog Pop-Up Pilihan Sumber Upload Gambar/Logo Global (Sesuai Desain Gambar).
 */
@Composable
fun GlobalImagePickerDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onCameraSelect: () -> Unit,
    onGallerySelect: () -> Unit,
    title: String = "Pilih Bukti Pembayaran",
    description: String? = null
) {
    if (!show) return

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Handle Bar Indicator
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF6366F1))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    // Responsif: judul dipotong maksimal 2 baris agar kartu pilihan tetap muat di HP 320dp
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Row: Dua Kartu Pilihan Utama (Kamera & Galeri)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // OPSI 1: KAMERA (Kartu Biru/Indigo)
                    VerticalImageOptionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.PhotoCamera,
                        backgroundColor = Color(0xFF5B5CF0),
                        title = "Kamera",
                        subtitle = "Ambil foto baru",
                        onClick = {
                            onDismissRequest()
                            onCameraSelect()
                        }
                    )

                    // OPSI 2: GALERI (Kartu Hijau)
                    VerticalImageOptionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.PhotoLibrary,
                        backgroundColor = Color(0xFF05A66B),
                        title = "Galeri",
                        subtitle = "Pilih dari galeri",
                        onClick = {
                            onDismissRequest()
                            onGallerySelect()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Format & Size Subcaption
                Text(
                    text = "Format JPG, PNG, atau HEIC. Maks 2MB (Auto Kompresi)",
                    fontSize = 11.sp,
                    fontFamily = interfamily,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol Batal
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Batal",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalImageOptionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    backgroundColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            // Responsif: tinggi minimum 148dp (sama seperti sebelumnya) tapi boleh bertambah saat font diperbesar
            .heightIn(min = 148.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circle Translucent Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontFamily = interfamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontFamily = interfamily,
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.88f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
