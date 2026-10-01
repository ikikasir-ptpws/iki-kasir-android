package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.ptpws.ikikasir.feature.bluetooth.data.preferences.BluetoothPrinterPreferences
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.NotaSettingPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

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

            val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                ?: error("Bluetooth tidak tersedia di perangkat ini.")
            val adapter = bluetoothManager.adapter
                ?: error("Bluetooth tidak tersedia di perangkat ini.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                throw SecurityException("Izin Bluetooth diperlukan untuk mencetak struk.")
            }

            check(adapter.isEnabled) { "Aktifkan Bluetooth terlebih dahulu." }

            val printer = adapter.getRemoteDevice(setting.savedAddress)
            val notaSetting = NotaSettingPreferences(appContext).getSetting()
            val bytesPerLine = if (
                setting.savedName.contains("MP58", ignoreCase = true) ||
                setting.paperWidth != BluetoothPrinterSetting.PAPER_80MM
            ) 32 else 48
            val socket = connect(printer)
            try {
                socket.outputStream.use { output ->
                    output.write(encodeReceipt(transaksi, notaSetting, bytesPerLine))
                    output.flush()
                }
            } finally {
                socket.close()
            }
        }
    }

    private fun encodeReceipt(
        transaksi: PenjualanTransaksi,
        notaSetting: com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting,
        columns: Int
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

        fun row(left: String, right: String) {
            val cleanRight = right.toAscii().take(columns)
            val leftWidth = (columns - cleanRight.length - 1).coerceAtLeast(1)
            val cleanLeft = left.toAscii()
            if (cleanLeft.length > leftWidth) {
                wrapped(cleanLeft)
                feedLine(cleanRight.padStart(columns))
            } else {
                feedLine(cleanLeft.padEnd(leftWidth) + " " + cleanRight)
            }
        }

        fun amount(value: Double): String = "Rp ${currency.format(value.toLong())}"

        command(0x1B, 0x40)
        command(0x1B, 0x74, 0x00)
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
        val ppn = transaksi.ppnAmount
        if (ppn > 0) row("PPN", amount(ppn))
        if (transaksi.discount > 0) row("Diskon", "- ${amount(transaksi.discount)}")

        command(0x1B, 0x45, 0x01)
        command(0x1D, 0x21, 0x10)
        row("TOTAL", amount(transaksi.total.takeIf { it > 0 } ?: (subtotal + ppn - transaksi.discount)))
        command(0x1D, 0x21, 0x00)
        command(0x1B, 0x45, 0x00)

        val grandTotal = transaksi.total.takeIf { it > 0 } ?: (subtotal + ppn - transaksi.discount)
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
        val data = content.toAscii().toByteArray(Charsets.US_ASCII)
        if (data.isEmpty()) return

        fun command(vararg values: Int) {
            output.write(values.map(Int::toByte).toByteArray())
        }

        command(0x1B, 0x61, 0x01)
        command(0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00)
        command(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x06)
        command(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31)
        val length = data.size + 3
        command(
            0x1D, 0x28, 0x6B,
            length and 0xFF, (length shr 8) and 0xFF,
            0x31, 0x50, 0x30
        )
        output.write(data)
        command(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30)
        command(0x1B, 0x61, 0x00)
    }

    private fun String.toAscii(): String = map { character ->
        if (character.code in 32..126) character else '?'
    }.joinToString("")

    private fun connect(printer: BluetoothDevice): BluetoothSocket {
        val secureSocket = printer.createRfcommSocketToServiceRecord(SPP_UUID)
        try {
            secureSocket.connect()
            return secureSocket
        } catch (error: IOException) {
            try {
                secureSocket.close()
            } catch (closeError: IOException) {
                error.addSuppressed(closeError)
            }
            val insecureSocket = printer.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
            try {
                insecureSocket.connect()
                return insecureSocket
            } catch (fallbackError: IOException) {
                try {
                    insecureSocket.close()
                } catch (closeError: IOException) {
                    fallbackError.addSuppressed(closeError)
                }
                throw IOException(
                    "Tidak dapat terhubung ke printer. Pastikan VSC MP58C menyala, sudah dipasangkan " +
                        "di Pengaturan Bluetooth Android, dan tidak sedang terhubung ke perangkat lain.",
                    fallbackError
                )
            }
        }
    }

    private companion object {
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
