package com.ptpws.ikikasir.feature.subscription.data

object SubscriptionApiConfig {
    /**
     * Email Pelanggan Pemilik Lisensi IKIKASIR
     * Akun ini yang terdaftar di Manajemen Web IKIKASIR & pemilik project Firestore
     */
    var CLIENT_EMAIL = "ikikasir.id@gmail.com"

    /**
     * URL API Backend Manajemen IKIKASIR
     * Mendukung otomatis Emulator (10.0.2.2) dan HP Fisik via Wi-Fi (192.168.18.121)
     */
    var BASE_URL = "http://10.0.2.2:5000/api"

    val CANDIDATE_URLS = listOf(
        "http://10.0.2.2:5000/api",         // Android Emulator
        "http://192.168.18.121:5000/api",    // HP Fisik via Wi-Fi (IP Laptop Dev)
        "http://127.0.0.1:5000/api",         // HP Fisik via USB (adb reverse)
        "http://localhost:5000/api"
    )

    // Nomor WhatsApp Admin IKIKASIR untuk konfirmasi perpanjangan langganan
    const val ADMIN_WA_NUMBER = "6285669660865"
}

