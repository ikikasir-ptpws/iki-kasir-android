package com.ptpws.ikikasir.feature.pengaturan.domain.model

import com.google.firebase.Timestamp

/**
 * Domain model for store/receipt settings that appear on printed receipts.
 * Clean Architecture — Domain Layer.
 * English field naming for Firestore & Room consistency.
 */
data class NotaSetting(
    val id: String = "default_nota_setting",
    val storeName: String = "",
    val storeAddress: String = "",
    val wifiName: String = "",
    val wifiPassword: String = "",
    val paperWidth: String = "58mm", // "58mm" or "80mm"
    val updatedAt: Timestamp = Timestamp.now(),
    val isSynced: Boolean = true
) {
    // Backward compatibility helper properties if referenced elsewhere
    val namaToko: String get() = storeName
    val alamatToko: String get() = storeAddress
    val namaWifi: String get() = wifiName
    val kataSandiWifi: String get() = wifiPassword
    val lebarKertas: String get() = paperWidth
}
