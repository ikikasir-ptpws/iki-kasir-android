package com.ptpws.ikikasir.commond

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.ptpws.ikikasir.core.util.ImageCompressorHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Bottom Sheet pilihan sumber gambar: Kamera atau Galeri.
 * Digunakan secara global di seluruh fitur yang memiliki upload gambar.
 *
 * @param onImageSelected Dipanggil ketika user sudah memilih gambar,
 *                        menyertakan URI file yang sudah diproses/dikompresi.
 * @param onDismiss Dipanggil ketika dialog ditutup.
 * @param uploadType Tipe upload untuk styling ikon & label.
 * @param targetSubfolder Subfolder sementara untuk penyimpanan lokal sebelum upload.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourcePickerDialog(
    onImageSelected: (Uri) -> Unit,
    onDismiss: () -> Unit,
    uploadType: ImageUploadType = ImageUploadType.GENERAL,
    targetSubfolder: String = "temp_images",
    isUploading: Boolean = false,
    uploadProgress: Float? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State untuk file kamera sementara
    var cameraFileUri by remember { mutableStateOf<Uri?>(null) }
    var localImageUri by remember { mutableStateOf<Uri?>(null) }
    var processingImage by remember { mutableStateOf(false) }
    var progressValue by remember { mutableStateOf<Float?>(null) }
    var progressMessage by remember { mutableStateOf("") }

    // Launcher galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                processingImage = true
                progressValue = 0.1f
                progressMessage = "Membaca gambar dari galeri..."
                try {
                    val compressedFile = ImageCompressorHelper.processAndCompressImage(
                        context = context,
                        sourceUri = uri,
                        targetSubfolder = targetSubfolder,
                        onProgress = { p, msg ->
                            coroutineScope.launch {
                                progressValue = p.coerceIn(0f, 1f)
                                progressMessage = msg
                            }
                        }
                    )
                    localImageUri = Uri.fromFile(compressedFile)
                    onImageSelected(Uri.fromFile(compressedFile))
                    onDismiss()
                } catch (e: Exception) {
                    progressMessage = "Gagal memproses gambar: ${e.message}"
                    delay(1500)
                } finally {
                    processingImage = false
                    progressValue = null
                }
            }
        }
    }

    // Launcher kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val fileUri = cameraFileUri
            if (fileUri != null) {
                coroutineScope.launch {
                    processingImage = true
                    progressValue = 0.1f
                    progressMessage = "Memproses foto dari kamera..."
                    try {
                        val compressedFile = ImageCompressorHelper.processAndCompressImage(
                            context = context,
                            sourceUri = fileUri,
                            targetSubfolder = targetSubfolder,
                            onProgress = { p, msg ->
                                coroutineScope.launch {
                                    progressValue = p.coerceIn(0f, 1f)
                                    progressMessage = msg
                                }
                            }
                        )
                        localImageUri = Uri.fromFile(compressedFile)
                        onImageSelected(Uri.fromFile(compressedFile))
                        onDismiss()
                    } catch (e: Exception) {
                        progressMessage = "Gagal memproses foto: ${e.message}"
                        delay(1500)
                    } finally {
                        processingImage = false
                        progressValue = null
                    }
                }
            }
        }
    }

    fun launchCamera() {
        try {
            val (_, uri) = ImageCompressorHelper.createCameraTempFileUri(context)
            cameraFileUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "Gagal membuka kamera: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            android.widget.Toast.makeText(
                context,
                "Izin kamera diperlukan untuk mengambil foto.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    val onCameraClick = {
        if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val title = when (uploadType) {
        ImageUploadType.PRODUK -> "Pilih Foto Produk"
        ImageUploadType.LOGO_STRUK -> "Pilih Logo Toko"
        ImageUploadType.FOTO_PROFIL -> "Pilih Foto Profil"
        ImageUploadType.BUKTI_PEMBAYARAN -> "Pilih Bukti Pembayaran"
        ImageUploadType.GENERAL -> "Pilih Gambar"
    }

    Dialog(
        onDismissRequest = { if (!processingImage) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !processingImage, dismissOnClickOutside = !processingImage)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header gradient bar
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    color = Color(0xFF111827)
                )

                if (processingImage) {
                    // Progress processing
                    val currentProgress = progressValue
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        if (currentProgress != null) {
                            LinearProgressIndicator(
                                progress = { currentProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF6366F1),
                                trackColor = Color(0xFFE0E7FF)
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF6366F1),
                                trackColor = Color(0xFFE0E7FF)
                            )
                        }
                        Text(
                            text = progressMessage.ifBlank { "Memproses gambar..." },
                            fontSize = 12.sp,
                            fontFamily = interfamily,
                            color = Color(0xFF6B7280)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Opsi Kamera
                        ImageSourceOptionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CameraAlt,
                            label = "Kamera",
                            description = "Ambil foto baru",
                            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                            onClick = onCameraClick
                        )

                        // Opsi Galeri
                        ImageSourceOptionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Image,
                            label = "Galeri",
                            description = "Pilih dari galeri",
                            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                            onClick = {
                                galleryLauncher.launch("image/*")
                            }
                        )
                    }

                    // Keterangan format
                    Text(
                        text = "Format JPG, PNG, atau HEIC. Maks 2MB (Auto Kompresi)",
                        fontSize = 11.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Tombol batal (hanya jika tidak sedang proses)
                if (!processingImage) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Batal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = interfamily,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageSourceOptionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 6.dp,
        animationSpec = tween(150),
        label = "elevation"
    )

    Card(
        modifier = modifier
            .clickable {
                pressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(colors = gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interfamily,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    fontFamily = interfamily,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * Komponen gambar dengan tombol edit di atas sudut.
 * Dapat diklik untuk membuka ImageSourcePickerDialog.
 *
 * @param imageUrl URL gambar yang ditampilkan (bisa URL Firebase Storage atau URI lokal).
 * @param onPickImage Dipanggil ketika gambar baru dipilih, menyertakan URI file lokal.
 * @param modifier Modifier untuk container.
 * @param uploadType Tipe upload untuk dialog.
 * @param targetSubfolder Folder sementara untuk kompresi lokal.
 * @param placeholder Konten placeholder ketika tidak ada gambar.
 */
@Composable
fun EditableImageBox(
    imageUrl: String,
    onPickImage: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    uploadType: ImageUploadType = ImageUploadType.GENERAL,
    targetSubfolder: String = "temp_images",
    placeholder: @Composable (BoxScope.() -> Unit)? = null
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Gambar utama
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Gambar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            placeholder?.invoke(this)
        }

        // Tombol edit (pojok kanan bawah)
        IconButton(
            onClick = { showPicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(36.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                    ),
                    shape = CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Ubah Gambar",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    if (showPicker) {
        ImageSourcePickerDialog(
            onImageSelected = { uri ->
                onPickImage(uri)
                showPicker = false
            },
            onDismiss = { showPicker = false },
            uploadType = uploadType,
            targetSubfolder = targetSubfolder
        )
    }
}
