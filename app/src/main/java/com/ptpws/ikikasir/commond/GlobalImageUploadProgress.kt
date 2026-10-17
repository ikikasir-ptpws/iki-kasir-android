package com.ptpws.ikikasir.commond

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Tipe upload gambar/logo untuk styling ikon dan judul kontekstual.
 */
enum class ImageUploadType {
    PRODUK,
    LOGO_STRUK,
    FOTO_PROFIL,
    GENERAL
}

/**
 * Data state untuk dialog / banner upload progress global.
 */
data class UploadProgressState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Mengunggah Gambar",
    val message: String = "Sedang memproses dan mengompresi gambar...",
    val progress: Float? = null, // null = indeterminate (berputar), 0.0f..1.0f = determinate
    val type: ImageUploadType = ImageUploadType.GENERAL,
    val isCancellable: Boolean = false,
    val onCancel: (() -> Unit)? = null
)

/**
 * Singleton Manager Global untuk memicu tampilan progress upload gambar/logo
 * dari mana saja (Composable, ViewModel, Activity, UseCase).
 */
object GlobalUploadProgress {
    private val _state = MutableStateFlow<UploadProgressState?>(null)
    val state: StateFlow<UploadProgressState?> = _state.asStateFlow()

    fun show(
        title: String = "Mengunggah Gambar",
        message: String = "Sedang memproses gambar...",
        progress: Float? = null,
        type: ImageUploadType = ImageUploadType.GENERAL,
        isCancellable: Boolean = false,
        onCancel: (() -> Unit)? = null
    ) {
        _state.value = UploadProgressState(
            title = title,
            message = message,
            progress = progress,
            type = type,
            isCancellable = isCancellable,
            onCancel = onCancel
        )
    }

    fun updateProgress(progress: Float, message: String? = null) {
        _state.update { current ->
            current?.copy(
                progress = progress.coerceIn(0f, 1f),
                message = message ?: current.message
            )
        }
    }

    fun showProductImageUpload(progress: Float? = null, message: String = "Sedang memproses foto produk...") {
        show(
            title = "Upload Foto Produk",
            message = message,
            progress = progress,
            type = ImageUploadType.PRODUK
        )
    }

    fun showLogoUpload(progress: Float? = null, message: String = "Sedang memproses logo nota / struk...") {
        show(
            title = "Upload Logo Struk",
            message = message,
            progress = progress,
            type = ImageUploadType.LOGO_STRUK
        )
    }

    fun showProfilePhotoUpload(progress: Float? = null, message: String = "Sedang memproses foto profil...") {
        show(
            title = "Upload Foto Profil",
            message = message,
            progress = progress,
            type = ImageUploadType.FOTO_PROFIL
        )
    }

    fun dismiss() {
        _state.value = null
    }
}

/**
 * Komponen Indikator Lingkaran (Circular Progress Indicator) khusus upload gambar/logo.
 * Mendukung progress berputar indeterminate maupun determinate (0-100%).
 */
@Composable
fun GlobalImageUploadCircleIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    size: Dp = 76.dp,
    strokeWidth: Dp = 6.dp,
    primaryColor: Color = Color(0xFF4F46E5),
    trackColor: Color = Color(0xFFEEF2FF),
    title: String? = null,
    message: String? = null,
    type: ImageUploadType = ImageUploadType.GENERAL,
    isDarkTheme: Boolean = false
) {
    val animatedProgress = progress?.let {
        animateFloatAsState(
            targetValue = it.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "UploadProgressAnimation"
        ).value
    }

    val infiniteTransition = rememberInfiniteTransition(label = "UploadGlowAnimation")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val defaultIcon = when (type) {
        ImageUploadType.PRODUK -> Icons.Outlined.Image
        ImageUploadType.LOGO_STRUK -> Icons.Outlined.Storefront
        ImageUploadType.FOTO_PROFIL -> Icons.Outlined.Upload
        ImageUploadType.GENERAL -> Icons.Outlined.Upload
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Subtle Outer Pulse Ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = if (isDarkTheme) 0.12f else 0.08f))
            )

            // Background Track Circle
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = trackColor,
                strokeWidth = strokeWidth,
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Round
            )

            // Active Progress Indicator
            if (animatedProgress != null) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = primaryColor,
                    strokeWidth = strokeWidth,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round
                )

                // Center Content: Percentage or Done Check
                if (animatedProgress >= 1f) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selesai",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(size * 0.42f)
                    )
                } else {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        fontSize = (size.value * 0.22f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = if (isDarkTheme) Color.White else primaryColor
                    )
                }
            } else {
                // Indeterminate Animated Spinner
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = primaryColor,
                    strokeWidth = strokeWidth,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round
                )

                // Center Icon Pulsing
                Icon(
                    imageVector = defaultIcon,
                    contentDescription = "Mengunggah",
                    tint = primaryColor,
                    modifier = Modifier
                        .size(size * 0.38f)
                        .scale(pulseScale)
                )
            }
        }

        // Title and Message Labels
        if (title != null || message != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interfamily,
                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )
                }
                if (message != null) {
                    Text(
                        text = message,
                        fontSize = 11.5.sp,
                        fontFamily = interfamily,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Overlay Progress Bar Indicator Circle yang dapat dipasang langsung
 * di atas container/Card foto produk atau logo toko (in-place overlay).
 */
@Composable
fun GlobalImageUploadOverlay(
    isUploading: Boolean,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    title: String? = "Mengunggah...",
    message: String? = null,
    shape: Shape = RoundedCornerShape(16.dp),
    size: Dp = 68.dp,
    type: ImageUploadType = ImageUploadType.GENERAL
) {
    AnimatedVisibility(
        visible = isUploading,
        enter = fadeIn(animationSpec = tween(250)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(Color.White.copy(alpha = 0.92f))
                .border(1.dp, Color(0xFFE2E8F0), shape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            GlobalImageUploadCircleIndicator(
                progress = progress,
                size = size,
                strokeWidth = 5.dp,
                title = title,
                message = message,
                type = type,
                primaryColor = Color(0xFF4F46E5),
                trackColor = Color(0xFFEEF2FF)
            )
        }
    }
}

/**
 * Dialog Progress Modal yang menampilkan Circular Indicator untuk Upload Gambar/Logo.
 */
@Composable
fun GlobalImageUploadDialog(
    show: Boolean,
    onDismissRequest: () -> Unit = {},
    progress: Float? = null,
    title: String = "Mengunggah Gambar",
    message: String = "Sedang memproses dan mengompresi gambar...",
    type: ImageUploadType = ImageUploadType.GENERAL,
    isCancellable: Boolean = false,
    onCancel: (() -> Unit)? = null
) {
    if (!show) return

    Dialog(
        onDismissRequest = {
            if (isCancellable) {
                onCancel?.invoke()
                onDismissRequest()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = isCancellable,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row (if cancellable)
                if (isCancellable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                onCancel?.invoke()
                                onDismissRequest()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Batal",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Main Circular Indicator
                GlobalImageUploadCircleIndicator(
                    progress = progress,
                    size = 86.dp,
                    strokeWidth = 6.5.dp,
                    type = type,
                    primaryColor = Color(0xFF4F46E5),
                    trackColor = Color(0xFFEEF2FF)
                )

                // Title & Description
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interfamily,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = message,
                        fontSize = 12.5.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }

                // Determinate Linear Bar (Subtle Secondary Progress)
                if (progress != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF4F46E5),
                            trackColor = Color(0xFFEEF2FF)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Proses upload",
                                fontSize = 11.sp,
                                fontFamily = interfamily,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = interfamily,
                                color = Color(0xFF4F46E5)
                            )
                        }
                    }
                }

                // Informational Warning Note
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mohon jangan keluar dari aplikasi selama proses upload berlangsung.",
                        fontSize = 11.sp,
                        fontFamily = interfamily,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                // Optional Cancel Button
                if (isCancellable) {
                    OutlinedButton(
                        onClick = {
                            onCancel?.invoke()
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF64748B)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = "Batalkan",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Host Composable yang mendengarkan [GlobalUploadProgress.state]
 * dan secara otomatis memunculkan dialog lingkaran indikator saat dipanggil.
 * Pasang di root Scaffold / AppNavHost / BaseActivity.
 */
@Composable
fun GlobalUploadProgressHost() {
    val uploadState by GlobalUploadProgress.state.collectAsState()
    val current = uploadState ?: return

    GlobalImageUploadDialog(
        show = true,
        onDismissRequest = { GlobalUploadProgress.dismiss() },
        progress = current.progress,
        title = current.title,
        message = current.message,
        type = current.type,
        isCancellable = current.isCancellable,
        onCancel = {
            current.onCancel?.invoke()
            GlobalUploadProgress.dismiss()
        }
    )
}
