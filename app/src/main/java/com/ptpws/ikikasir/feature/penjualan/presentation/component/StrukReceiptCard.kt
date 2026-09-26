package com.ptpws.ikikasir.feature.penjualan.presentation.component

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Clean Architecture Jetpack Compose component rendering REAL transaction data.
 *
 * - Header: nama toko & alamat dari [NotaSetting] (bisa dikonfigurasi di Profil > Setting Nota)
 * - Badge Antrean: menggunakan [queueSequence] yang sama dengan nomor di Riwayat Antrean
 * - QR Code: berisi [PenjualanTransaksi.transactionId] → bisa di-scan dari Riwayat Transaksi
 * - WiFi info: dari [NotaSetting.namaWifi] & [NotaSetting.kataSandiWifi]
 * - Footer catatan & "Powered by IKIKASIR"
 */
@Composable
fun StrukReceiptCard(
    transaksi: PenjualanTransaksi,
    notaSetting: NotaSetting = NotaSetting(),
    queueSequence: Int = transaksi.queueSequence,          // nomor antrean real dari transaksi / database
    modifier: Modifier = Modifier
) {
    val formatRupiah = remember {
        { amount: Double ->
            "Rp " + NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount.toLong())
        }
    }

    val dateFormatted = remember(transaksi.createdAt) {
        SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID"))
            .format(transaksi.createdAt.toDate())
    }

    // QR code content = transactionId agar bisa di-scan di Riwayat Transaksi
    val qrContent = remember(transaksi.transactionId, transaksi.transactionNumber) {
        transaksi.transactionId.ifBlank { transaksi.transactionNumber.ifBlank { "IKIKASIR-STRUK" } }
    }
    val qrBitmap = remember(qrContent) {
        generateQrBitmap(qrContent, 256)
    }

    // Nomor antrean dari queueSequence yang diformat 2 digit (e.g. 01, 02, 12)
    val antreanLabel = remember(queueSequence) {
        "#%02d".format(queueSequence)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── 1. Header — Nama Toko & Alamat (dari NotaSetting) ────────
            Text(
                text = notaSetting.storeName.ifBlank { "IKIKASIR" },
                fontFamily = interfamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )
            if (notaSetting.storeAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notaSetting.storeAddress,
                    fontFamily = interfamily,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Antrean Badge (nomor sama dengan di Riwayat Antrean) ──────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEEF2FF)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Antrean $antreanLabel",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF4F46E5)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── 2. Transaction Meta (No. Invoice, Kasir, Waktu, Pelanggan) ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "NO. INVOICE",
                                fontFamily = interfamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = transaksi.transactionNumber.ifBlank { transaksi.transactionId.ifBlank { "-" } },
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "KASIR",
                                fontFamily = interfamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = transaksi.createdBy.ifBlank { "Admin" },
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "WAKTU TRANSAKSI",
                                fontFamily = interfamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateFormatted,
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (transaksi.tableNumber.isNotBlank()) "PELANGGAN / MEJA" else "PELANGGAN",
                                fontFamily = interfamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val displayPelangganMeja = buildString {
                                if (transaksi.customerName.isNotBlank()) append(transaksi.customerName)
                                if (transaksi.tableNumber.isNotBlank()) {
                                    if (isNotEmpty()) append(" (M: ${transaksi.tableNumber})") else append("Meja: ${transaksi.tableNumber}")
                                }
                            }.ifBlank { "-" }
                            Text(
                                text = displayPelangganMeja,
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Dashed Divider ─────────────────────────────────────────────
            DashedReceiptDivider()

            Spacer(modifier = Modifier.height(18.dp))

            // ── 3. Item Pesanan ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ITEM PESANAN",
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "SUBTOTAL",
                    fontFamily = interfamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val displayItems = transaksi.items

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (displayItems.isEmpty()) {
                    Text(
                        text = "Tidak ada item",
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                } else {
                    for (item in displayItems) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${item.quantity}x ${formatRupiah(item.price)}",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = formatRupiah(item.subtotal),
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }

            // ── 4. Catatan Pesanan (hanya tampil jika ada) ────────────────
            if (transaksi.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                    border = BorderStroke(1.dp, Color(0xFFE0F2FE))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "CATATAN PESANAN",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color(0xFF0284C7)
                            )
                        }
                        Text(
                            text = "\"${transaksi.notes}\"",
                            fontFamily = interfamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 11.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

    val context = LocalContext.current
    val taxSetting = remember {
        com.ptpws.ikikasir.feature.pengaturan.data.preferences.TaxSettingPreferences(context).getSetting()
    }

            // ── 5. Payment Summary ─────────────────────────────────────────
            val totalSubtotal = if (transaksi.subtotal > 0) transaksi.subtotal else displayItems.sumOf { it.subtotal }
            val totalDiscount = transaksi.discount
            val calculatedPpn = if (taxSetting.isActive && taxSetting.percentage > 0) totalSubtotal * (taxSetting.percentage / 100.0) else 0.0
            val ppnAmount     = if (transaksi.ppnAmount > 0) transaksi.ppnAmount else calculatedPpn
            val grandTotal    = (totalSubtotal - totalDiscount + ppnAmount).coerceAtLeast(0.0)
            val paidAmount    = if (transaksi.paymentAmount > 0 && transaksi.paymentAmount >= grandTotal) transaksi.paymentAmount else grandTotal
            val returnChange  = if (transaksi.change >= 0 && transaksi.paymentAmount > 0) transaksi.change else (paidAmount - grandTotal).coerceAtLeast(0.0)
            val itemCount     = displayItems.sumOf { it.quantity }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Subtotal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subtotal ($itemCount Item)",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = formatRupiah(totalSubtotal),
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // PPN (hanya jika aktif)
                    if (ppnAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PPN",
                                fontFamily = interfamily,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "+${formatRupiah(ppnAmount)}",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    // Diskon (hanya jika ada)
                    if (totalDiscount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Diskon Promo",
                                    fontFamily = interfamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0D9488)
                                )
                            }
                            Text(
                                text = "-${formatRupiah(totalDiscount)}",
                                fontFamily = interfamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0D9488)
                            )
                        }
                    }

                    // Total Tagihan Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL TAGIHAN",
                                    fontFamily = interfamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatRupiah(grandTotal),
                                    fontFamily = interfamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFEEF2FF)
                            ) {
                                Text(
                                    text = transaksi.paymentMethod.ifBlank { "Tunai" },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontFamily = interfamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                        }
                    }

                    // Nominal Dibayar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nominal Dibayar",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = formatRupiah(paidAmount),
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // Kembalian
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Kembalian",
                            fontFamily = interfamily,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = formatRupiah(returnChange),
                            fontFamily = interfamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0D9488)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 6. Info WiFi (dari NotaSetting) ───────────────────────────
            if (notaSetting.wifiName.isNotBlank() || notaSetting.wifiPassword.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "WiFi",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = buildString {
                                if (notaSetting.wifiName.isNotBlank()) {
                                    append(notaSetting.wifiName)
                                }
                                if (notaSetting.wifiPassword.isNotBlank()) {
                                    if (notaSetting.wifiName.isNotBlank()) append(" • ")
                                    append("Sandi: ${notaSetting.wifiPassword}")
                                }
                            },
                            fontFamily = interfamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF15803D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── 7. QR Code Verifikasi ──────────────────────────────────────
            // QR berisi transactionId → di-scan di halaman Riwayat Transaksi
            Card(
                modifier = Modifier.size(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code Verifikasi",
                            modifier = Modifier.size(110.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "PINDAI VERIFIKASI STRUK",
                        fontFamily = interfamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── 8. Footer catatan & Powered by ────────────────────────────
            Text(
                text = "\"Terima kasih atas kunjungan Anda! Silakan berkunjung kembali.\"",
                fontFamily = interfamily,
                fontStyle = FontStyle.Italic,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Powered by IKIKASIR",
                fontFamily = interfamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Jagged Paper Edge
            JaggedPaperEdge(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DashedReceiptDivider() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2.dp.toPx(),
            pathEffect = pathEffect
        )
    }
}

@Composable
private fun JaggedPaperEdge(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.height(10.dp)) {
        val width = size.width
        val height = size.height
        val toothWidth = 12.dp.toPx()
        val numTeeth = (width / toothWidth).toInt() + 1

        val path = Path().apply {
            moveTo(0f, 0f)
            for (i in 0 until numTeeth) {
                val x1 = i * toothWidth + (toothWidth / 2f)
                val x2 = (i + 1) * toothWidth
                lineTo(x1, height)
                lineTo(x2, 0f)
            }
            lineTo(width, 0f)
            close()
        }
        drawPath(path, Color(0xFFF1F5F9))
    }
}

private fun generateQrBitmap(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        for (x in 0 until w) {
            for (y in 0 until h) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
