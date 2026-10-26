package com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase

import android.content.Context
import android.net.Uri
import com.ptpws.ikikasir.core.util.ExcelExportService
import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan
import javax.inject.Inject

class ExportCatatanKeuanganToExcelUseCase @Inject constructor(
    private val excelExportService: ExcelExportService
) {
    suspend operator fun invoke(
        context: Context,
        catatanList: List<CatatanKeuangan>,
        filterLabel: String = "Semua"
    ): Result<Uri> {
        return excelExportService.exportCatatanKeuangan(context, catatanList, filterLabel)
    }
}
