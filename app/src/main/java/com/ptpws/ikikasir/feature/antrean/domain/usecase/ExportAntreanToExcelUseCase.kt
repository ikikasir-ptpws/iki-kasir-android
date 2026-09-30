package com.ptpws.ikikasir.feature.antrean.domain.usecase

import android.content.Context
import android.net.Uri
import com.ptpws.ikikasir.core.util.ExcelExportService
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import javax.inject.Inject

class ExportAntreanToExcelUseCase @Inject constructor(
    private val excelExportService: ExcelExportService
) {
    suspend operator fun invoke(
        context: Context,
        historyList: List<QueueHistory>,
        transaksiMap: Map<String, PenjualanTransaksi>,
        filterLabel: String = "Semua"
    ): Result<Uri> {
        return excelExportService.exportAntrean(
            context = context,
            historyList = historyList,
            transaksiMap = transaksiMap,
            filterLabel = filterLabel
        )
    }
}
