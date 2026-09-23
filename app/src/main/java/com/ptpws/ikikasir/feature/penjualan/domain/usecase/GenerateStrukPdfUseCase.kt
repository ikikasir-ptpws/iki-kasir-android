package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import android.content.Context
import com.ptpws.ikikasir.feature.penjualan.data.pdf.StrukPdfHelper
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import java.io.File
import javax.inject.Inject

/**
 * UseCase for generating and saving receipt (struk) as PDF file.
 */
class GenerateStrukPdfUseCase @Inject constructor() {
    operator fun invoke(context: Context, transaksi: PenjualanTransaksi): File? {
        return StrukPdfHelper.generatePdfFile(context, transaksi)
    }
}
