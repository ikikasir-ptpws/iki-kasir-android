package com.ptpws.ikikasir.feature.laporanpenjualan.domain.usecase

import android.content.Context
import android.net.Uri
import com.ptpws.ikikasir.core.util.ExcelExportService
import com.ptpws.ikikasir.feature.laporanpenjualan.domain.model.LaporanPenjualanSummary
import javax.inject.Inject

class ExportLaporanPenjualanUseCase @Inject constructor(
    private val excelExportService: ExcelExportService
) {
    suspend operator fun invoke(
        context: Context,
        summary: LaporanPenjualanSummary
    ): Result<Uri> {
        return excelExportService.exportLaporanPenjualan(context, summary)
    }
}
