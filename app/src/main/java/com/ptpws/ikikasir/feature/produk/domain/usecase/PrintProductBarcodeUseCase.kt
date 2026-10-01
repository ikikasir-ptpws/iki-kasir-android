package com.ptpws.ikikasir.feature.produk.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.ptpws.ikikasir.feature.bluetooth.data.BluetoothPrinterConnection
import com.ptpws.ikikasir.feature.bluetooth.data.preferences.BluetoothPrinterPreferences
import com.ptpws.ikikasir.feature.bluetooth.domain.model.BluetoothPrinterSetting
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.NotaSettingPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class PrintProductBarcodeUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val printerPreferences: BluetoothPrinterPreferences
) {
    suspend operator fun invoke(productName: String, barcode: String) {
        require(barcode.isNotBlank()) { "Produk belum memiliki barcode." }

        val printer = printerPreferences.getSetting()
        check(printer.savedAddress.isNotBlank()) {
            "Pilih dan simpan printer Bluetooth terlebih dahulu."
        }

        val printData = withContext(Dispatchers.Default) {
            val paperWidth = if (
                NotaSettingPreferences(context).getSetting().paperWidth ==
                BluetoothPrinterSetting.PAPER_80MM
            ) 576 else 384
            val barcodeBitmap = createBitmap(
                barcode,
                BarcodeFormat.CODE_128,
                paperWidth,
                112,
                margin = 1
            )
            val qrSize = if (paperWidth == 576) 240 else 200
            val qrBitmap = createBitmap(
                barcode,
                BarcodeFormat.QR_CODE,
                qrSize,
                qrSize,
                margin = 4
            )
            encodePrintData(productName, barcode, barcodeBitmap, qrBitmap)
        }

        BluetoothPrinterConnection.send(context, printer.savedAddress, printData)
    }

    private fun createBitmap(
        content: String,
        format: BarcodeFormat,
        width: Int,
        height: Int,
        margin: Int
    ): Bitmap {
        val matrix = MultiFormatWriter().encode(
            content,
            format,
            width,
            height,
            mapOf(EncodeHintType.MARGIN to margin)
        )
        return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).also { bitmap ->
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }
        }
    }

    private fun encodePrintData(
        productName: String,
        barcode: String,
        barcodeBitmap: Bitmap,
        qrBitmap: Bitmap
    ): ByteArray {
        val output = ByteArrayOutputStream()

        fun command(vararg bytes: Int) {
            output.write(bytes.map(Int::toByte).toByteArray())
        }

        fun centeredText(value: String) {
            command(0x1B, 0x61, 0x01)
            output.write(value.toPrinterAscii().toByteArray(Charsets.US_ASCII))
            command(0x0A)
        }

        fun feedLines(count: Int) {
            repeat(count) { command(0x0A) }
        }

        command(0x1B, 0x40)
        command(0x1B, 0x45, 0x01)
        centeredText(productName)
        command(0x1B, 0x45, 0x00)
        centeredText("BARCODE")
        printBitmap(output, barcodeBitmap)
        centeredText(barcode)
        feedLines(2)
        centeredText("QR CODE")
        printBitmap(output, qrBitmap)
        command(0x1B, 0x61, 0x00)
        feedLines(3)
        return output.toByteArray()
    }

    private fun printBitmap(output: ByteArrayOutputStream, bitmap: Bitmap) {
        val bytesPerRow = (bitmap.width + 7) / 8
        output.write(
            byteArrayOf(
                0x1B, 0x61, 0x01,
                0x1D, 0x76, 0x30, 0x00,
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
    }

    private fun String.toPrinterAscii(): String = map { character ->
        if (character.code in 32..126) character else '?'
    }.joinToString("")
}
