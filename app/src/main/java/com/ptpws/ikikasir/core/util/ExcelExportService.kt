package com.ptpws.ikikasir.core.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val EXCEL_MIME_TYPE =
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@Singleton
class ExcelExportService @Inject constructor() {

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
    private val fileNameDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    suspend fun exportTransaksi(
        context: Context,
        transaksiList: List<PenjualanTransaksi>,
        filterLabel: String = "Semua"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val rows = mutableListOf<List<ExcelCell>>()
            rows += listOf(ExcelCell("LAPORAN RIWAYAT TRANSAKSI", style = "Title"))
            rows += listOf(
                ExcelCell(
                    "Filter: $filterLabel  |  Dicetak: ${dateTimeFormat.format(Date())}  |  Total Data: ${transaksiList.size} transaksi",
                    style = "Subtitle"
                )
            )
            rows.add(emptyList())
            rows += listOf(
                "No", "No. Transaksi", "Tgl & Jam", "Kasir", "Pelanggan", "No. Meja",
                "Subtotal", "Diskon", "PPN", "Total", "Metode Bayar", "Status"
            ).map { ExcelCell(it, style = "Header") }

            var grandSubtotal = 0.0
            var grandDiskon = 0.0
            var grandPpn = 0.0
            var grandTotal = 0.0

            transaksiList.sortedByDescending { it.createdAt.seconds }.forEachIndexed { index, tx ->
                val total = tx.total.takeIf { it > 0 }
                    ?: (tx.subtotal - tx.discount + tx.ppnAmount).coerceAtLeast(0.0)
                val isSuccess = tx.status.equals("COMPLETED", true) || tx.status.equals("LUNAS", true)
                val rowStyle = if (index % 2 == 0) "Data" else "DataAlt"
                val numberStyle = if (index % 2 == 0) "Number" else "NumberAlt"

                rows += listOf(
                    ExcelCell((index + 1).toString(), numberStyle, "Number"),
                    ExcelCell(tx.transactionNumber, rowStyle),
                    ExcelCell(dateTimeFormat.format(tx.createdAt.toDate()), rowStyle),
                    ExcelCell(tx.createdBy.ifBlank { "-" }, rowStyle),
                    ExcelCell(tx.customerName.ifBlank { "-" }, rowStyle),
                    ExcelCell(tx.tableNumber.ifBlank { "-" }, rowStyle),
                    ExcelCell(tx.subtotal.toExcelNumber(), numberStyle, "Number"),
                    ExcelCell(tx.discount.toExcelNumber(), numberStyle, "Number"),
                    ExcelCell(tx.ppnAmount.toExcelNumber(), numberStyle, "Number"),
                    ExcelCell(total.toExcelNumber(), numberStyle, "Number"),
                    ExcelCell(tx.paymentMethod, rowStyle),
                    ExcelCell(if (isSuccess) "Berhasil" else tx.status, if (isSuccess) "Success" else "Failure")
                )

                grandSubtotal += tx.subtotal
                grandDiskon += tx.discount
                grandPpn += tx.ppnAmount
                grandTotal += total
            }

            rows.add(emptyList())
            rows += listOf(
                ExcelCell("TOTAL KESELURUHAN", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell(grandSubtotal.toExcelNumber(), "TotalValue", "Number"),
                ExcelCell(grandDiskon.toExcelNumber(), "TotalValue", "Number"),
                ExcelCell(grandPpn.toExcelNumber(), "TotalValue", "Number"),
                ExcelCell(grandTotal.toExcelNumber(), "TotalValue", "Number"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel")
            )

            saveAndShare(
                context,
                createSpreadsheetXlsx("Riwayat Transaksi", rows, listOf(8, 24, 20, 18, 20, 12, 16, 14, 14, 16, 18, 14)),
                "Transaksi_${fileNameDateFormat.format(Date())}.xlsx"
            )
        }
    }

    suspend fun exportAntrean(
        context: Context,
        historyList: List<QueueHistory>,
        transaksiMap: Map<String, PenjualanTransaksi>,
        filterLabel: String = "Semua"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val rows = mutableListOf<List<ExcelCell>>()
            rows += listOf(ExcelCell("LAPORAN RIWAYAT ANTREAN", style = "Title"))
            rows += listOf(
                ExcelCell(
                    "Filter: $filterLabel  |  Dicetak: ${dateTimeFormat.format(Date())}  |  Total Data: ${historyList.size} antrean",
                    style = "Subtitle"
                )
            )
            rows.add(emptyList())
            rows += listOf(
                "No", "ID Transaksi", "No. Antrean", "Pelanggan", "No. Meja",
                "Status", "Produk (Qty)", "Total", "Waktu Selesai"
            ).map { ExcelCell(it, style = "Header") }

            var totalSelesai = 0
            var totalBatal = 0

            historyList.sortedByDescending { it.completedAt.seconds }.forEachIndexed { index, history ->
                val transaction = transaksiMap[history.transactionId]
                val isDone = history.status.equals("DONE", true) || history.status.equals("SELESAI", true)
                val products = transaction?.items?.joinToString("; ") { "${it.name} (${it.quantity})" } ?: "-"
                val total = transaction?.let {
                    it.total.takeIf { value -> value > 0 }
                        ?: (it.subtotal - it.discount + it.ppnAmount).coerceAtLeast(0.0)
                } ?: 0.0
                val rowStyle = if (index % 2 == 0) "Data" else "DataAlt"
                val numberStyle = if (index % 2 == 0) "Number" else "NumberAlt"

                rows += listOf(
                    ExcelCell((index + 1).toString(), numberStyle, "Number"),
                    ExcelCell(history.transactionId, rowStyle),
                    ExcelCell(history.queueSequence.toString(), numberStyle, "Number"),
                    ExcelCell(history.customerName.ifBlank { "-" }, rowStyle),
                    ExcelCell(history.tableNumber.ifBlank { "-" }, rowStyle),
                    ExcelCell(if (isDone) "Selesai" else "Dibatalkan", if (isDone) "Success" else "Failure"),
                    ExcelCell(products, rowStyle),
                    ExcelCell(total.toExcelNumber(), numberStyle, "Number"),
                    ExcelCell(dateTimeFormat.format(history.completedAt.toDate()), rowStyle)
                )

                if (isDone) totalSelesai++ else totalBatal++
            }

            rows.add(emptyList())
            rows += listOf(
                ExcelCell("RINGKASAN", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("", "TotalLabel"),
                ExcelCell("Selesai: $totalSelesai", "TotalValue"),
                ExcelCell("Dibatalkan: $totalBatal", "TotalValue"),
                ExcelCell("Total: ${historyList.size}", "TotalValue"),
                ExcelCell("", "TotalLabel")
            )

            saveAndShare(
                context,
                createSpreadsheetXlsx("Riwayat Antrean", rows, listOf(8, 24, 14, 20, 12, 16, 36, 16, 20)),
                "AntreanHistory_${fileNameDateFormat.format(Date())}.xlsx"
            )
        }
    }

    private fun createSpreadsheetXlsx(
        sheetName: String,
        rows: List<List<ExcelCell>>,
        columnWidths: List<Int>
    ): ByteArray {
        val columnCount = maxOf(rows.maxOfOrNull { it.size } ?: 1, columnWidths.size)
        val lastColumn = columnName(columnCount)
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.addTextEntry(
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                    <Default Extension="xml" ContentType="application/xml"/>
                    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                    <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                    </Types>"""
            )
            zip.addTextEntry(
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>"""
            )
            zip.addTextEntry(
                "xl/workbook.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                    xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                    <sheets><sheet name="${sheetName.xmlEscape()}" sheetId="1" r:id="rId1"/></sheets>
                    </workbook>"""
            )
            zip.addTextEntry(
                "xl/_rels/workbook.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
                    </Relationships>"""
            )
            zip.addTextEntry("xl/styles.xml", createStylesXml())
            zip.addTextEntry(
                "xl/worksheets/sheet1.xml",
                createWorksheetXml(rows, columnWidths, lastColumn)
            )
        }
        return output.toByteArray()
    }

    private fun createWorksheetXml(
        rows: List<List<ExcelCell>>,
        columnWidths: List<Int>,
        lastColumn: String
    ): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        append("""<dimension ref="A1:$lastColumn${rows.size}"/>""")
        append("""<sheetViews><sheetView workbookViewId="0"><pane ySplit="4" topLeftCell="A5" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews>""")
        append("<cols>")
        columnWidths.forEachIndexed { index, width ->
            append("""<col min="${index + 1}" max="${index + 1}" width="$width" customWidth="1"/>""")
        }
        append("</cols><sheetData>")
        rows.forEachIndexed { rowIndex, row ->
            append("""<row r="${rowIndex + 1}">""")
            row.forEachIndexed { columnIndex, cell ->
                val cellRef = "${columnName(columnIndex + 1)}${rowIndex + 1}"
                val styleIndex = cell.style.toStyleIndex()
                if (cell.type == "Number") {
                    val numericValue = cell.value.toDoubleOrNull() ?: 0.0
                    append("""<c r="$cellRef" s="$styleIndex"><v>$numericValue</v></c>""")
                } else {
                    append("""<c r="$cellRef" s="$styleIndex" t="inlineStr"><is><t xml:space="preserve">${cell.value.xmlEscape()}</t></is></c>""")
                }
            }
            append("</row>")
        }
        append("</sheetData>")
        append("""<mergeCells count="2"><mergeCell ref="A1:${lastColumn}1"/><mergeCell ref="A2:${lastColumn}2"/></mergeCells>""")
        append("</worksheet>")
    }

    private fun createStylesXml(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
        <fonts count="3">
        <font><sz val="10"/><name val="Calibri"/></font>
        <font><b/><sz val="16"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
        <font><b/><sz val="10"/><name val="Calibri"/></font>
        </fonts>
        <fills count="10">
        <fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FF4338CA"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFDBEAFE"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FF1E3A8A"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFEFF6FF"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFDCFCE7"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFFEE2E2"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FF166534"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FF4338CA"/><bgColor indexed="64"/></patternFill></fill>
        </fills>
        <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
        <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
        <cellXfs count="12">
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
        <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center" vertical="center"/></xf>
        <xf numFmtId="0" fontId="0" fillId="3" borderId="0" xfId="0"/>
        <xf numFmtId="0" fontId="1" fillId="4" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center"/></xf>
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
        <xf numFmtId="0" fontId="0" fillId="5" borderId="0" xfId="0"/>
        <xf numFmtId="4" fontId="0" fillId="0" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="right"/></xf>
        <xf numFmtId="4" fontId="0" fillId="5" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="right"/></xf>
        <xf numFmtId="0" fontId="2" fillId="6" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center"/></xf>
        <xf numFmtId="0" fontId="2" fillId="7" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center"/></xf>
        <xf numFmtId="0" fontId="1" fillId="8" borderId="0" xfId="0"/>
        <xf numFmtId="0" fontId="1" fillId="9" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="right"/></xf>
        </cellXfs>
        </styleSheet>
    """.trimIndent()

    private fun String.toStyleIndex(): Int = when (this) {
        "Title" -> 1
        "Subtitle" -> 2
        "Header" -> 3
        "DataAlt" -> 5
        "Number" -> 6
        "NumberAlt" -> 7
        "Success" -> 8
        "Failure" -> 9
        "TotalLabel" -> 10
        "TotalValue" -> 11
        else -> 4
    }

    private fun columnName(column: Int): String {
        var value = column
        val name = StringBuilder()
        while (value > 0) {
            val remainder = (value - 1) % 26
            name.insert(0, ('A'.code + remainder).toChar())
            value = (value - 1) / 26
        }
        return name.toString()
    }

    private fun ZipOutputStream.addTextEntry(path: String, content: String) {
        putNextEntry(ZipEntry(path))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }
    private fun saveAndShare(context: Context, content: ByteArray, fileName: String): Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, EXCEL_MIME_TYPE)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/IkiKasir")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Tidak dapat membuat file di folder Download/IkiKasir.")

            try {
                val output = resolver.openOutputStream(uri, "w")
                    ?: throw IOException("Tidak dapat menulis file ekspor.")
                output.use { it.write(content) }
                val published = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                if (resolver.update(uri, published, null, null) == 0) {
                    throw IOException("File ekspor gagal disimpan ke folder Download.")
                }
                return uri
            } catch (error: Exception) {
                resolver.delete(uri, null, null)
                throw error
            }
        }

        val directory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir,
            "IkiKasir/Export"
        )
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Tidak dapat membuat folder ekspor.")
        }
        val file = File(directory, fileName)
        FileOutputStream(file).use { it.write(content) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun Double.toExcelNumber(): String =
        if (isFinite()) toString() else "0"

    private fun String.xmlEscape(): String = buildString(length) {
        this@xmlEscape.forEach { char ->
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> if (char == '\t' || char == '\n' || char == '\r' || char >= ' ') append(char)
            }
        }
    }

    private data class ExcelCell(
        val value: String,
        val style: String = "Data",
        val type: String = "String"
    )
}
