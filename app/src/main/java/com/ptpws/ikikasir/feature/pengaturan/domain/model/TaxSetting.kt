package com.ptpws.ikikasir.feature.pengaturan.domain.model

import com.google.firebase.Timestamp

/**
 * Domain Model untuk Pengaturan PPN / Pajak.
 * - id: Key identifier ("default")
 * - isActive: Boolean (Status PPN / Pajak aktif/nonaktif)
 * - percentage: Double (Persentase PPN, misal 11.0)
 * - type: String ("EXCLUSIVE" / "INCLUSIVE") -> Belum Termasuk (Eksklusif) / Sudah Termasuk (Inklusif)
 * - updatedAt: Timestamp
 */
data class TaxSetting(
    val id: String = "default",
    val isActive: Boolean = true,
    val percentage: Double = 11.0,
    val type: String = TAX_TYPE_EXCLUSIVE,
    val updatedAt: Timestamp = Timestamp.now()
) {
    companion object {
        const val TAX_TYPE_EXCLUSIVE = "EXCLUSIVE" // Belum Termasuk (Eksklusif)
        const val TAX_TYPE_INCLUSIVE = "INCLUSIVE" // Sudah Termasuk (Inklusif)
    }
}
