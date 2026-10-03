package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.ptpws.ikikasir.feature.bluetooth.data.BluetoothPrinterConnection
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.ptpws.ikikasir.feature.bluetooth.data.preferences.BluetoothPrinterPreferences
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.NotaSettingPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Prints the receipt directly to the saved ESC/POS Bluetooth printer.
 */
class PrintStrukUseCase {

    suspend operator fun invoke(context: Context, transaksi: PenjualanTransaksi) {
        withContext(Dispatchers.IO) {
            val appContext = context.applicationContext
            val setting = BluetoothPrinterPreferences(appContext).getSetting()
            check(setting.savedAddress.isNotBlank()) {
                "Pilih dan simpan printer Bluetooth terlebih dahulu."
            }

            val notaSetting = NotaSettingPreferences(appContext).getSetting()
            val bytesPerLine = if (notaSetting.paperWidth == BluetoothPrinterSetting.PAPER_80MM) 48 else 32
            BluetoothPrinterConnection.send(
                appContext,
                setting.savedAddress,
                encodeReceipt(transaksi, notaSetting, bytesPerLine, appContext)
            )
        }
    }

    private fun encodeReceipt(
        transaksi: PenjualanTransaksi,
        notaSetting: com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting,
        columns: Int,
        context: Context
    ): ByteArray {
        val bytes = ByteArrayOutputStream()
        val encoding = Charsets.US_ASCII
        val currency = NumberFormat.getNumberInstance(Locale("id", "ID"))
        val line = "-".repeat(columns)

        fun command(vararg values: Int) {
            bytes.write(values.map(Int::toByte).toByteArray())
        }

        fun text(value: String) {
            bytes.write(value.toByteArray(encoding))
        }

        fun feedLine(value: String = "") {
            text(value)
            command(0x0A)
        }

        fun centered(value: String) {
            val clean = value.toAscii()
            clean.chunked(columns).forEach { part ->
                command(0x1B, 0x61, 0x01)
                feedLine(part)
            }
            command(0x1B, 0x61, 0x00)
        }

        fun wrapped(value: String, indent: String = "") {
            val clean = value.toAscii()
            val available = (columns - indent.length).coerceAtLeast(1)
            val words = clean.split(Regex("\\s+")).filter(String::isNotBlank)
            var current = indent
            words.forEach { word ->
                if (current.length > indent.length && current.length + 1 + word.length > columns) {
                    feedLine(current)
                    current = indent
                }
                var remaining = word
                while (remaining.length > available) {
                    if (current.length > indent.length) {
                        feedLine(current)
                        current = indent
                    }
                    feedLine(indent + remaining.take(available))
                    remaining = remaining.drop(available)
                }
                if (current.length > indent.length) current += " "
                current += remaining
            }
            if (current.length > indent.length) feedLine(current)
        }

        fun row(left: String, right: String, lineColumns: Int = columns) {
            val cleanRight = right.toAscii().take(lineColumns)
            val leftWidth = (lineColumns - cleanRight.length - 1).coerceAtLeast(1)
            val cleanLeft = left.toAscii()
            if (cleanLeft.length > leftWidth) {
                wrapped(cleanLeft)
                feedLine(cleanRight.padStart(lineColumns))
            } else {
                feedLine(cleanLeft.padEnd(leftWidth) + " " + cleanRight)
            }
        }

        fun amount(value: Double): String = "Rp ${currency.format(value.toLong())}"

        command(0x1B, 0x40)
        command(0x1B, 0x74, 0x00)
        command(0x1D, 0x4C, 0x00, 0x00)
        command(0x1D, 0x57, (columns * 12) and 0xFF, ((columns * 12) shr 8) and 0xFF)

        // ── 0. Logo Toko (jika ada) ────────
        if (notaSetting.logoUrl.isNotBlank()) {
            val maxLogoWidth = if (columns >= 48) 384 else 256
            val logoBitmap = loadLogoBitmap(context, notaSetting.logoUrl, maxLogoWidth)
            if (logoBitmap != null) {
                printBitmap(bytes, logoBitmap)
                feedLine()
            }
        }

        command(0x1B, 0x61, 0x01)
        command(0x1B, 0x45, 0x01)
        val storeName = notaSetting.storeName.ifBlank { "IKIKASIR" }.toAscii()
        if (storeName.length <= columns / 2) command(0x1D, 0x21, 0x11)
        centered(storeName)
        command(0x1D, 0x21, 0x00)
        command(0x1B, 0x45, 0x00)
        if (notaSetting.storeAddress.isNotBlank()) centered(notaSetting.storeAddress)
        command(0x1B, 0x61, 0x00)
        feedLine(line)

        val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            .format(transaksi.createdAt.toDate())
        row("No. Nota", transaksi.transactionNumber.ifBlank { transaksi.transactionId })
        row("Waktu", date)
        row("Kasir", transaksi.createdBy.ifBlank { "Admin" })
        if (transaksi.customerName.isNotBlank()) row("Pelanggan", transaksi.customerName)
        if (transaksi.tableNumber.isNotBlank()) row("Meja", transaksi.tableNumber)
        feedLine(line)

        transaksi.items.forEach { item ->
            wrapped(item.name)
            row("${item.quantity} x ${amount(item.price)}", amount(item.subtotal))
        }
        feedLine(line)

        val subtotal = if (transaksi.subtotal > 0) transaksi.subtotal else transaksi.items.sumOf { it.subtotal }
        if (transaksi.isPpnEksklusif) {
            row("PPN", "+${transaksi.formattedPpnPercentage}")
        }
        if (transaksi.discount > 0) row("Diskon", "- ${amount(transaksi.discount)}")

        val grandTotalLabel = "TOTAL"
        val grandTotal = transaksi.total.takeIf { it > 0 } ?: (subtotal + (if (transaksi.isPpnEksklusif) transaksi.ppnAmount else 0.0) - transaksi.discount)
        val grandTotalAmount = amount(grandTotal)
        val totalTextColumns = if (columns >= 48) 2 else 1
        val totalLine = if (totalTextColumns == 2) {
            val maxAmountLength = (columns / 2) - 1
            val compactAmount = grandTotalAmount.takeLast(maxAmountLength)
            grandTotalLabel.padEnd(columns - compactAmount.length) + compactAmount
        } else {
            grandTotalLabel.padEnd(columns - grandTotalAmount.length) + grandTotalAmount
        }
        command(0x1B, 0x45, 0x01)
        if (totalTextColumns == 2) command(0x1D, 0x21, 0x01)
        feedLine(totalLine)
        if (totalTextColumns == 2) command(0x1D, 0x21, 0x00)
        command(0x1B, 0x45, 0x00)

        val paid = transaksi.paymentAmount.takeIf { it > 0 } ?: grandTotal
        row("Bayar (${transaksi.paymentMethod})", amount(paid))
        row("Kembalian", amount(transaksi.change))
        feedLine(line)

        if (transaksi.notes.isNotBlank()) {
            wrapped("Catatan: ${transaksi.notes}")
            feedLine(line)
        }
        if (notaSetting.wifiName.isNotBlank()) {
            row("WiFi", notaSetting.wifiName)
            if (notaSetting.wifiPassword.isNotBlank()) row("Sandi", notaSetting.wifiPassword)
            feedLine(line)
        }

        printQrCode(bytes, transaksi.transactionNumber.ifBlank { transaksi.transactionId })
        command(0x1B, 0x61, 0x01)
        centered(transaksi.transactionNumber.ifBlank { transaksi.transactionId })
        centered("Scan QR verifikasi struk")
        feedLine()
        centered("Terima kasih atas kunjungan Anda")
        centered("Powered by IKIKASIR")
        command(0x1B, 0x61, 0x00)
        command(0x1B, 0x64, 0x03)
        return bytes.toByteArray()
    }

    private fun printQrCode(output: ByteArrayOutputStream, content: String) {
        val cleanContent = content.toAscii()
        if (cleanContent.isBlank()) return

        val matrix = QRCodeWriter().encode(cleanContent, BarcodeFormat.QR_CODE, 256, 256)
        val widthBytes = (matrix.width + 7) / 8
        val height = matrix.height
        output.write(byteArrayOf(0x1B, 0x61, 0x01))
        output.write(
            byteArrayOf(
                0x1D, 0x76, 0x30, 0x00,
                widthBytes.toByte(), (widthBytes shr 8).toByte(),
                height.toByte(), (height shr 8).toByte()
            )
        )
        for (y in 0 until height) {
            for (byteIndex in 0 until widthBytes) {
                var value = 0
                for (bit in 0..7) {
                    val x = byteIndex * 8 + bit
                    if (x < matrix.width && matrix.get(x, y)) {
                        value = value or (0x80 shr bit)
                    }
                }
                output.write(value)
            }
        }
        output.write(byteArrayOf(0x1B, 0x61, 0x00))
    }

    private fun String.toAscii(): String = map { character ->
        if (character.code in 32..126) character else '?'
    }.joinToString("")

    private fun loadLogoBitmap(context: Context, logoUrl: String, maxWidth: Int): Bitmap? {
        if (logoUrl.isBlank()) return null
        return try {
            val bitmap = if (logoUrl.startsWith("data:image") || logoUrl.startsWith("data:application")) {
                val base64Data = logoUrl.substringAfter("base64,")
                val decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } else {
                val uri = Uri.parse(logoUrl)
                if (uri.scheme == "file" || uri.scheme == null) {
                    val path = uri.path ?: logoUrl
                    BitmapFactory.decodeFile(path)
                } else {
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                }
            } ?: return null

            if (bitmap.width <= maxWidth) {
                bitmap
            } else {
                val ratio = maxWidth.toFloat() / bitmap.width
                val targetHeight = (bitmap.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(bitmap, maxWidth, targetHeight, true)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun printBitmap(output: ByteArrayOutputStream, bitmap: Bitmap) {
        val bytesPerRow = (bitmap.width + 7) / 8
        output.write(
            byteArrayOf(
                0x1B, 0x61, 0x01, // Center
                0x1D, 0x76, 0x30, 0x00, // GS v 0 0
                bytesPerRow.toByte(), (bytesPerRow shr 8).toByte(),
                bitmap.height.toByte(), (bitmap.height shr 8).toByte()
            )
        )
        for (y in 0 until bitmap.height) {
            for (byteIndex in 0 until bytesPerRow) {
                var value = 0
                for (bit in 0..7) {
                    val x = byteIndex * 8 + bit
                    if (x < bitmap.width &&
                        android.graphics.Color.red(bitmap.getPixel(x, y)) < 128
                    ) {
                        value = value or (0x80 shr bit)
                    }
                }
                output.write(value)
            }
        }
        output.write(byteArrayOf(0x1B, 0x61, 0x00)) // Reset to Left
    }
}
