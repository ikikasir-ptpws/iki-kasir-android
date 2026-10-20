package com.ptpws.ikikasir.feature.subscription.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionApiConfig
import com.ptpws.ikikasir.feature.subscription.data.SubscriptionCheckResult

@Composable
fun ExpiredPopupDialog(
    subscriptionData: SubscriptionCheckResult?,
    isLoadingRefresh: Boolean = false,
    onRefresh: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current

    val businessName = subscriptionData?.businessName?.takeIf { it.isNotBlank() }
        ?: subscriptionData?.customerName?.takeIf { it.isNotBlank() }
        ?: if (subscriptionData?.status == "NOT_FOUND") "Belum Terdaftar" else "Toko Anda"

    val email = subscriptionData?.email.orEmpty()

    val packageName = subscriptionData?.packageName?.takeIf { it.isNotBlank() }
        ?: if (subscriptionData?.status == "NOT_FOUND") "-" else "Paket Kasir"

    val message = if (!subscriptionData?.message.isNullOrBlank()) {
        subscriptionData!!.message
    } else {
        "Masa aktif langganan aplikasi IKIKASIR untuk toko Anda telah berakhir. Silakan hubungi admin untuk melakukan perpanjangan paket."
    }

    Dialog(
        onDismissRequest = { /* Non-dismissible: Pengguna tidak bisa menutup popup tanpa perpanjangan */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ikon Warning / Jam Pasir
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = "Expired",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Judul
                Text(
                    text = "Masa Langganan Habis",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Aplikasi IKIKASIR Terkunci",
                    fontFamily = interfamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFFDC2626),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kartu Informasi Toko & Pesan
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Toko:",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = businessName,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF111827)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Paket:",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = packageName,
                                fontFamily = interfamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF4F46E5)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0xFFE5E7EB)
                        )

                        Text(
                            text = message,
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol 1: Hubungi Admin via WhatsApp
                Button(
                    onClick = {
                        openWhatsApp(context, businessName, email, packageName)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = "💬 Hubungi Admin via WhatsApp",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tombol 2: Cek Status Lagi (Refresh)
                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !isLoadingRefresh,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (isLoadingRefresh) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF4F46E5)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Memeriksa Status...",
                            fontFamily = interfamily,
                            fontSize = 13.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cek Status Lagi",
                            fontFamily = interfamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF374151)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol 3: Keluar Aplikasi
                TextButton(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Keluar Aplikasi",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

private fun openWhatsApp(
    context: Context,
    businessName: String,
    email: String,
    packageName: String
) {
    try {
        val message = "Halo Admin IKIKASIR, masa aktif langganan toko $businessName ($email) paket $packageName telah berakhir. Saya ingin melakukan perpanjangan langganan."
        val encodedText = Uri.encode(message)
        val url = "https://api.whatsapp.com/send?phone=${SubscriptionApiConfig.ADMIN_WA_NUMBER}&text=$encodedText"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback jika WhatsApp tidak terpasang
        android.util.Log.e("ExpiredPopup", "Gagal membuka WhatsApp: ${e.message}")
    }
}
