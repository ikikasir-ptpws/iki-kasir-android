package com.ptpws.ikikasir.feature.bluetooth.domain.model

/**
 * Domain Model untuk pengaturan printer Bluetooth yang tersimpan.
 * - savedAddress: MAC address printer yang terakhir dipilih/tersimpan
 * - savedName: Nama printer yang terakhir dipilih/tersimpan
 * - isAutoConnect: Otomatis terhubung ke printer saat aplikasi dibuka
 * - paperWidth: Lebar kertas printer (58mm atau 80mm)
 */
data class BluetoothPrinterSetting(
    val id: String = "default",
    val savedAddress: String = "",
    val savedName: String = "",
    val isAutoConnect: Boolean = false,
    val paperWidth: String = PAPER_58MM
) {
    companion object {
        const val PAPER_58MM = "58mm"
        const val PAPER_80MM = "80mm"
    }
}
