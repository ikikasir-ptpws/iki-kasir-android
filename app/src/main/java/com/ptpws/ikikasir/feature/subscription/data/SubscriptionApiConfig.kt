package com.ptpws.ikikasir.feature.subscription.data

object SubscriptionApiConfig {
    /**
     * Email Pelanggan Pemilik Lisensi IKIKASIR
     * Akun ini yang terdaftar di Manajemen Web IKIKASIR & pemilik project Firestore
     * Diisi otomatis setelah cek langganan berhasil.
     */
    var CLIENT_EMAIL = "ikikasir.id@gmail.com"

    /**
     * Kode Referral milik pelanggan yang sedang menggunakan aplikasi ini.
     * Format: "IKIKASIR-XXXXX" — diisi otomatis dari respons server saat cek langganan.
     * Dipakai oleh fitur Referral untuk:
     *   1. Menampilkan kode milik sendiri di halaman Program Referral
     *   2. Otomatis dikirim saat mengajukan calon client (Ajukan Calon Client)
     */
    var REFERRAL_CODE = ""

    /**
     * Nama bisnis / toko pemilik lisensi ini.
     * Diisi dari respons server, digunakan untuk personalisasi pesan share referral.
     */
    var BUSINESS_NAME = ""

    /**
     * ID Customer dari database backend manajemen.
     * Diisi saat berhasil verifikasi subscription, dipakai untuk query referral per-pelanggan.
     */
    var CUSTOMER_ID = ""

    /**
     * Nama pemilik lisensi (bukan nama toko).
     * Diisi otomatis dari respons server saat cek langganan.
     */
    var CUSTOMER_NAME = ""

    /**
     * Nama pelanggan yang mengundang lisensi ini (upline).
     * Diisi otomatis dari respons server / ringkasan referral.
     */
    var REFERRAL_UPLINE_NAME = ""

    /**
     * Kode referral milik pelanggan yang mengundang lisensi ini (upline).
     */
    var REFERRAL_UPLINE_CODE = ""

    /**
     * URL API Backend Manajemen IKIKASIR
     * Mendukung otomatis Emulator (10.0.2.2) dan HP Fisik via Wi-Fi
     */
    var BASE_URL = "http://127.0.0.1:5000/api"

    val CANDIDATE_URLS = listOf(
        "http://10.0.2.2:5000/api",         // Android Emulator
        "http://192.168.18.12:5000/api",    // HP Fisik via Wi-Fi (IP Laptop Dev)
        "http://127.0.0.1:5000/api",         // HP Fisik via USB (adb reverse)
        "http://localhost:5000/api"
    )

    // Nomor WhatsApp Admin IKIKASIR untuk konfirmasi perpanjangan langganan
    const val ADMIN_WA_NUMBER = "628132538603"
}


