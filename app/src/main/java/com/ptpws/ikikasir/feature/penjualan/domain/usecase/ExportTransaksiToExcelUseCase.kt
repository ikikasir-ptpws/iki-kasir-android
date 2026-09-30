package com.ptpws.ikikasir.feature.penjualan.domain.usecase

import android.content.Context
import android.net.Uri
import com.ptpws.ikikasir.core.util.ExcelExportService
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import javax.inject.Inject

class ExportTransaksiToExcelUseCase @Inject constructor(
    private val excelExportService: ExcelExportService
) {
    suspend operator fun invoke(
        context: Context,
        transaksiList: List<PenjualanTransaksi>,
        filterLabel: String = "Semua"
    ): Result<Uri> {
        return excelExportService.exportTransaksi(
            context = context,
            transaksiList = transaksiList,
            filterLabel = filterLabel
        )
    }
}
