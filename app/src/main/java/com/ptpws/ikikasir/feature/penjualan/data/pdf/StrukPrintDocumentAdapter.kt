package com.ptpws.ikikasir.feature.penjualan.data.pdf

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import java.io.FileOutputStream

/**
 * PrintDocumentAdapter implementation for sending receipt directly to Android PrintManager.
 */
class StrukPrintDocumentAdapter(
    private val context: Context,
    private val transaksi: PenjualanTransaksi
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder("Struk_${transaksi.transactionNumber.ifBlank { "IKIKASIR" }}.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()

        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        val pdfDoc = PdfDocument()
        val width = 384
        var calculatedHeight = 650 + (transaksi.items.size * 45)
        if (transaksi.notes.isNotBlank()) calculatedHeight += 50

        val pageInfo = PdfDocument.PageInfo.Builder(width, calculatedHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)

        StrukPdfHelper.drawStrukOnCanvas(context, page.canvas, width, calculatedHeight, transaksi)

        pdfDoc.finishPage(page)

        try {
            destination?.fileDescriptor?.let { fd ->
                FileOutputStream(fd).use { out ->
                    pdfDoc.writeTo(out)
                }
            }
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            e.printStackTrace()
            callback?.onWriteFailed(e.message)
        } finally {
            pdfDoc.close()
        }
    }
}
