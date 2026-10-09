package com.ptpws.ikikasir.feature.keuangan.domain.usecase

import android.content.Context
import android.net.Uri
import com.ptpws.ikikasir.core.util.ExcelExportService
import com.ptpws.ikikasir.feature.keuangan.domain.model.FinancialReportExportData
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import javax.inject.Inject

class ExportLaporanKeuanganToExcelUseCase @Inject constructor(
    private val excelExportService: ExcelExportService
) {
    suspend operator fun invoke(
        context: Context,
        reportData: FinancialReportExportData,
        transaksiList: List<PenjualanTransaksi>
    ): Result<Uri> {
        return excelExportService.exportLaporanKeuangan(
            context = context,
            reportData = reportData,
            transaksiList = transaksiList
        )
    }
}
