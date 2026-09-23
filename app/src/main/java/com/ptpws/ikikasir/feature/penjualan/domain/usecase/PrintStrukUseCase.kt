package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import android.content.Context
import android.print.PrintManager
import com.ptpws.ikikasir.feature.penjualan.data.pdf.StrukPrintDocumentAdapter
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import javax.inject.Inject

/**
 * UseCase for launching Android PrintManager printing flow for thermal / wifi / system printers.
 */
class PrintStrukUseCase @Inject constructor() {
    operator fun invoke(context: Context, transaksi: PenjualanTransaksi) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "Struk_${transaksi.transactionNumber.ifBlank { "IKIKASIR" }}"
        printManager.print(
            jobName,
            StrukPrintDocumentAdapter(context, transaksi),
            null
        )
    }
}
