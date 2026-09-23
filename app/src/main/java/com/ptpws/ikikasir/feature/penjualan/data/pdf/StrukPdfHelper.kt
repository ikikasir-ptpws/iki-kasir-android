package com.ptpws.ikikasir.feature.penjualan.data.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Data Helper for drawing receipt onto Canvas and generating PDF files using R.drawable.logoikikasir and REAL data.
 */
object StrukPdfHelper {

    fun generatePdfFile(context: Context, transaksi: PenjualanTransaksi): File? {
        val pdfDoc = PdfDocument()

        // 80mm thermal receipt dimensions in points (384 pt x dynamic height)
        val width = 384
        var calculatedHeight = 580 + (transaksi.items.size * 45)
        if (transaksi.notes.isNotBlank()) calculatedHeight += 50

        val pageInfo = PdfDocument.PageInfo.Builder(width, calculatedHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        drawStrukOnCanvas(context, canvas, width, calculatedHeight, transaksi)

        pdfDoc.finishPage(page)

        val fileName = "Struk_${transaksi.transactionNumber.ifBlank { "IKIKASIR" }}_${System.currentTimeMillis()}.pdf"

        return try {
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                File(downloadsDir, fileName)
            } else {
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            }

            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDoc.close()
            null
        }
    }

    fun drawStrukOnCanvas(
        context: Context,
        canvas: Canvas,
        width: Int,
        height: Int,
        transaksi: PenjualanTransaksi
    ) {
        val paint = Paint().apply { isAntiAlias = true }

        // Background White
        canvas.drawColor(Color.WHITE)

        val margin = 20f
        var y = 30f

        val formatRupiah = { amount: Double ->
            "Rp " + NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount.toLong())
        }

        // 1. Header Logo (R.drawable.logoikikasir)
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logoikikasir)
            if (logoBitmap != null) {
                val logoWidth = 140f
                val logoHeight = (logoBitmap.height.toFloat() / logoBitmap.width.toFloat()) * logoWidth
                val logoRect = RectF((width - logoWidth) / 2f, y, (width + logoWidth) / 2f, y + logoHeight)
                canvas.drawBitmap(logoBitmap, null, logoRect, paint)
                y += logoHeight + 14f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Antrean Badge
        val antreanNo = if (transaksi.transactionNumber.isNotBlank()) {
            transaksi.transactionNumber.takeLast(4)
        } else if (transaksi.transactionId.isNotBlank()) {
            transaksi.transactionId.takeLast(4)
        } else {
            "01"
        }

        val antreanText = "Antrean #$antreanNo"
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val textWidth = paint.measureText(antreanText) + 30f
        val antreanRect = RectF((width - textWidth) / 2f, y - 14f, (width + textWidth) / 2f, y + 8f)
        paint.color = Color.parseColor("#EEF2FF")
        canvas.drawRoundRect(antreanRect, 12f, 12f, paint)

        paint.color = Color.parseColor("#4F46E5")
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(antreanText, width / 2f, y, paint)
        y += 24f

        // 2. Transaction Meta Box (REAL DATA)
        val metaBoxRect = RectF(margin, y, width - margin, y + 74f)
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(metaBoxRect, 10f, 10f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(metaBoxRect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        val metaY = y + 18f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NO. INVOICE", margin + 12f, metaY, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("KASIR", width - margin - 12f, metaY, paint)

        val metaY2 = metaY + 14f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(transaksi.transactionNumber.ifBlank { transaksi.transactionId.ifBlank { "-" } }, margin + 12f, metaY2, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(transaksi.createdBy.ifBlank { "Admin" }, width - margin - 12f, metaY2, paint)

        val metaY3 = metaY2 + 18f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 8f
        canvas.drawText("WAKTU TRANSAKSI", margin + 12f, metaY3, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("PELANGGAN", width - margin - 12f, metaY3, paint)

        val metaY4 = metaY3 + 14f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        val dateFormatted = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).format(transaksi.createdAt.toDate())
        canvas.drawText(dateFormatted, margin + 12f, metaY4, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(transaksi.customerName.ifBlank { "-" }, width - margin - 12f, metaY4, paint)

        y += 86f

        // Dashed Divider
        drawDashedLine(canvas, margin, width - margin, y)
        y += 18f

        // 3. Items Table Header
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ITEM PESANAN", margin, y, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("SUBTOTAL", width - margin, y, paint)
        y += 16f

        // Real Items List
        for (item in transaksi.items) {
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.name, margin, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(formatRupiah(item.subtotal), width - margin, y, paint)
            y += 14f

            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.parseColor("#64748B")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("${item.quantity}x ${formatRupiah(item.price)}", margin, y, paint)
            y += 20f
        }

        // 4. Catatan Pesanan (Real Data: only drawn if notes present)
        if (transaksi.notes.isNotBlank()) {
            val notesBox = RectF(margin, y, width - margin, y + 42f)
            paint.color = Color.parseColor("#F0F7FF")
            canvas.drawRoundRect(notesBox, 8f, 8f, paint)

            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.parseColor("#0284C7")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("CATATAN PESANAN", margin + 10f, y + 14f, paint)

            paint.color = Color.parseColor("#334155")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("\"${transaksi.notes}\"", margin + 10f, y + 30f, paint)
            y += 54f
        }

        // 5. Payment Summary Card (REAL DATA)
        val hasDiscount = transaksi.discount > 0
        val summaryHeight = if (hasDiscount) 140f else 120f
        val summaryBox = RectF(margin, y, width - margin, y + summaryHeight)
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(summaryBox, 12f, 12f, paint)

        var sumY = y + 18f
        val totalSubtotal = if (transaksi.subtotal > 0) transaksi.subtotal else transaksi.items.sumOf { it.subtotal }
        val grandTotal = if (transaksi.total > 0) transaksi.total else (totalSubtotal - transaksi.discount).coerceAtLeast(0.0)
        val paidAmount = if (transaksi.paymentAmount > 0) transaksi.paymentAmount else grandTotal
        val returnChange = if (transaksi.change >= 0 && transaksi.paymentAmount > 0) transaksi.change else (paidAmount - grandTotal).coerceAtLeast(0.0)

        // Subtotal
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val itemCountText = "Subtotal (${transaksi.items.sumOf { it.quantity }} Item)"
        canvas.drawText(itemCountText, margin + 12f, sumY, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(formatRupiah(totalSubtotal), width - margin - 12f, sumY, paint)
        sumY += 16f

        // Diskon if > 0
        if (hasDiscount) {
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.parseColor("#0D9488")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Diskon Promo", margin + 12f, sumY, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("-${formatRupiah(transaksi.discount)}", width - margin - 12f, sumY, paint)
            sumY += 16f
        }

        // Inner White Total Tagihan Card
        val totalBox = RectF(margin + 10f, sumY, width - margin - 10f, sumY + 44f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(totalBox, 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRoundRect(totalBox, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL TAGIHAN", margin + 20f, sumY + 14f, paint)

        paint.color = Color.parseColor("#2563EB")
        paint.textSize = 15f
        canvas.drawText(formatRupiah(grandTotal), margin + 20f, sumY + 34f, paint)

        // Payment Method Badge
        val methodText = transaksi.paymentMethod.ifBlank { "Tunai" }
        paint.textSize = 9f
        val mWidth = paint.measureText(methodText) + 20f
        val methodRect = RectF(width - margin - 20f - mWidth, sumY + 12f, width - margin - 20f, sumY + 32f)
        paint.color = Color.parseColor("#EEF2FF")
        canvas.drawRoundRect(methodRect, 10f, 10f, paint)

        paint.color = Color.parseColor("#4F46E5")
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(methodText, methodRect.centerX(), sumY + 25f, paint)

        sumY += 56f

        // Nominal Dibayar & Kembalian
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Nominal Dibayar", margin + 12f, sumY, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(formatRupiah(paidAmount), width - margin - 12f, sumY, paint)
        sumY += 16f

        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#64748B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Kembalian", margin + 12f, sumY, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.parseColor("#0D9488")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(formatRupiah(returnChange), width - margin - 12f, sumY, paint)

        y += summaryHeight + 20f

        // 6. QR Code Verification (Real Transaction ID)
        val qrContent = transaksi.transactionNumber.ifBlank { transaksi.transactionId }.ifBlank { "IKIKASIR-STRUK" }
        val qrBitmap = generateQrBitmap(qrContent, 120)
        if (qrBitmap != null) {
            val qrBox = RectF((width - 120) / 2f, y, (width + 120) / 2f, y + 120f)
            canvas.drawBitmap(qrBitmap, null, qrBox, paint)
            y += 130f

            paint.color = Color.parseColor("#64748B")
            paint.textSize = 8f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("PINDAI VERIFIKASI STRUK", width / 2f, y, paint)
            y += 24f
        }

        // 7. Footer Message
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("\"Terima kasih atas kunjungan Anda! Silakan berkunjung kembali.\"", width / 2f, y, paint)
    }

    private fun drawDashedLine(canvas: Canvas, startX: Float, endX: Float, y: Float) {
        val paint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(8f, 8f), 0f)
            style = Paint.Style.STROKE
        }
        val path = Path().apply {
            moveTo(startX, y)
            lineTo(endX, y)
        }
        canvas.drawPath(path, paint)
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
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
