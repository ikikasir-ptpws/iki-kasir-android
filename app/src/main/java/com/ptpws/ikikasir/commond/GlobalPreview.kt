package com.ptpws.ikikasir.commond

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.app.ui.screen.DashboardScreen
import com.example.app.ui.screen.MenuFullScreen
import com.ptpws.ikikasir.core.responsive.presentation.components.FontAccessibilityPreviews
import com.ptpws.ikikasir.core.responsive.presentation.components.PhoneDevicePreviews
import com.ptpws.ikikasir.feature.auth.presentation.screen.LoginScreen
import com.ptpws.ikikasir.feature.bluetooth.presentation.screen.BluetoothPrinterScreen
import com.ptpws.ikikasir.feature.laporanpenjualan.presentation.screen.LaporanPenjualanScreen
import com.ptpws.ikikasir.feature.referral.presentation.screen.ReferralScreen
import com.ptpws.ikikasir.feature.role.presentation.screen.RoleIzinScreen
import com.ptpws.ikikasir.feature.role.presentation.screen.TambahRoleScreen
import com.ptpws.ikikasir.screens.keuangan.AuditLogScreen
import com.ptpws.ikikasir.screens.keuangan.CatatanKeuanganScreen
import com.ptpws.ikikasir.screens.keuangan.DetailCatatanKeuanganScreen
import com.ptpws.ikikasir.screens.keuangan.LaporanKeuanganScreen
import com.ptpws.ikikasir.screens.keuangan.MetodePembayaranScreen
import com.ptpws.ikikasir.screens.keuangan.TambahCatatanKeuanganScreen
import com.ptpws.ikikasir.screens.kategori.DaftarKategoriScreen
import com.ptpws.ikikasir.screens.kategori.TambahKategoriScreen
import com.ptpws.ikikasir.screens.manajemenpengguna.DaftarUserScreen
import com.ptpws.ikikasir.screens.manajemenpengguna.ManajemenPenggunaScreen
import com.ptpws.ikikasir.screens.manajemenpengguna.TambahPenggunaScreen
import com.ptpws.ikikasir.screens.manajemenstok.ManajemenStokScreen
import com.ptpws.ikikasir.screens.manajemenstok.StokEtalaseScreen
import com.ptpws.ikikasir.screens.manajemenstok.StokGudangScreen
import com.ptpws.ikikasir.screens.manajemenstok.UpdateStokScreen
import com.ptpws.ikikasir.screens.pengaturan.PengaturanDashboardScreen
import com.ptpws.ikikasir.screens.pengaturan.ProfilScreen
import com.ptpws.ikikasir.screens.penjualan.DetailTransaksiScreen
import com.ptpws.ikikasir.screens.penjualan.KasirCompactScreen
import com.ptpws.ikikasir.screens.penjualan.KasirScreen
import com.ptpws.ikikasir.screens.penjualan.KasirSplitScreen
import com.ptpws.ikikasir.screens.penjualan.PembayaranScreen
import com.ptpws.ikikasir.screens.penjualan.RiwayatAntreanScreen
import com.ptpws.ikikasir.screens.penjualan.RiwayatTransaksiScreen
import com.ptpws.ikikasir.screens.penjualan.WaitingListScreen
import com.ptpws.ikikasir.screens.produk.DaftarProdukScreen
import com.ptpws.ikikasir.screens.produk.DetailProdukScreen
import com.ptpws.ikikasir.screens.produk.TambahProdukScreen
import com.ptpws.ikikasir.screens.promo.ManajemenPromoScreen
import com.ptpws.ikikasir.screens.promo.TambahPromoScreen
import com.ptpws.ikikasir.screens.scanner.BarcodeScannerScreen
import com.ptpws.ikikasir.screens.splash.SplashScreen

/*
 * PREVIEW GLOBAL
 * ------------------------------------------------------------------
 * Katalog preview aplikasi; preview utama juga tersedia langsung di file tiap screen.
 *
 * Cara pakai:
 * - Buka file screen yang ingin dicek untuk melihat preview lokalnya.
 * - Katalog ini menyediakan daftar gabungan preview semua fitur.
 * - Untuk menambah screen: tambahkan preview lokal dan gunakan stub provider.
 *
 * CATATAN: @Preview tidak punya Application, jadi Hilt/Firebase/Room tidak
 * bisa dipakai. Karena itu setiap screen menerima kontrak state sempit
 * (mis. AuditLogUiProvider) dan preview memakai stub-nya.
 * ViewModel asli (yang sudah tersambung ke Firestore/Room) tidak diubah;
 * adapter Hilt hanya meneruskan panggilan ke ViewModel tersebut.
 */

// ---- Dashboard ----
@PhoneDevicePreviews
@FontAccessibilityPreviews
@Composable
private fun DashboardScreenPreview() {
    MaterialTheme {
        DashboardScreen(
            navController = rememberNavController(),
            viewModel = PreviewDashboardProvider()
        )
    }
}

// ---- Menu Semua Menu ----
@PhoneDevicePreviews
@Composable
private fun MenuFullScreenPreview() {
    MaterialTheme {
        MenuFullScreen(
            navController = rememberNavController(),
            viewModel = rememberPreviewSessionProvider()
        )
    }
}

// ---- Kategori ----
@PhoneDevicePreviews
@Composable
private fun DaftarKategoriScreenPreview() {
    MaterialTheme {
        DaftarKategoriScreen(
            navController = rememberNavController(),
            viewModel = PreviewKategoriProvider(),
            produkViewModel = PreviewProdukProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun TambahKategoriScreenPreview() {
    MaterialTheme {
        TambahKategoriScreen(viewModel = PreviewTambahKategoriProvider())
    }
}

// ---- Produk ----
@PhoneDevicePreviews
@Composable
private fun DaftarProdukScreenPreview() {
    MaterialTheme {
        DaftarProdukScreen(
            navController = rememberNavController(),
            viewModel = PreviewProdukProvider(),
            kategoriViewModel = PreviewKategoriProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun DetailProdukScreenPreview() {
    MaterialTheme {
        DetailProdukScreen(viewModel = PreviewDetailProdukProvider())
    }
}

@PhoneDevicePreviews
@Composable
private fun TambahProdukScreenPreview() {
    MaterialTheme {
        TambahProdukScreen(
            viewModel = PreviewTambahProdukProvider(),
            kategoriViewModel = PreviewKategoriProvider()
        )
    }
}

// ---- Kasir ----
@PhoneDevicePreviews
@FontAccessibilityPreviews
@Composable
private fun KasirScreenPreview() {
    MaterialTheme {
        KasirScreen(
            navController = rememberNavController(),
            viewModel = PreviewKasirProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun KasirCompactScreenPreview() {
    MaterialTheme {
        KasirCompactScreen(
            state = previewKasirState,
            viewModel = PreviewKasirProvider(),
            navController = rememberNavController(),
            onBayar = {},
            triggerBarcodeScanner = {}
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun KasirSplitScreenPreview() {
    MaterialTheme {
        KasirSplitScreen(
            state = previewKasirState,
            viewModel = PreviewKasirProvider(),
            navController = rememberNavController(),
            onBayar = {},
            onScanBarcode = {}
        )
    }
}

// ---- Pembayaran ----
@PhoneDevicePreviews
@FontAccessibilityPreviews
@Composable
private fun PembayaranScreenPreview() {
    MaterialTheme {
        PembayaranScreen(
            navController = rememberNavController(),
            viewModel = PreviewPembayaranProvider()
        )
    }
}

// ---- Transaksi ----
@PhoneDevicePreviews
@Composable
private fun RiwayatTransaksiScreenPreview() {
    MaterialTheme {
        RiwayatTransaksiScreen(
            navController = rememberNavController(),
            viewModel = PreviewRiwayatTransaksiProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun DetailTransaksiScreenPreview() {
    MaterialTheme {
        DetailTransaksiScreen(viewModel = PreviewDetailTransaksiProvider())
    }
}

// ---- Antrean ----
@PhoneDevicePreviews
@Composable
private fun WaitingListScreenPreview() {
    MaterialTheme {
        WaitingListScreen(
            navController = rememberNavController(),
            viewModel = PreviewAntreanProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun RiwayatAntreanScreenPreview() {
    MaterialTheme {
        RiwayatAntreanScreen(
            navController = rememberNavController(),
            viewModel = PreviewQueueHistoryProvider()
        )
    }
}

// ---- Stok ----
@PhoneDevicePreviews
@Composable
private fun ManajemenStokScreenPreview() {
    MaterialTheme {
        ManajemenStokScreen(
            navController = rememberNavController(),
            viewModel = PreviewManajemenStokProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun UpdateStokScreenPreview() {
    MaterialTheme {
        UpdateStokScreen(viewModel = PreviewUpdateStokProvider())
    }
}

@PhoneDevicePreviews
@Composable
private fun StokEtalaseScreenPreview() {
    MaterialTheme {
        StokEtalaseScreen()
    }
}

@PhoneDevicePreviews
@Composable
private fun StokGudangScreenPreview() {
    MaterialTheme {
        StokGudangScreen()
    }
}

// ---- Promo ----
@PhoneDevicePreviews
@Composable
private fun ManajemenPromoScreenPreview() {
    MaterialTheme {
        ManajemenPromoScreen(viewModel = PreviewPromoProvider())
    }
}

@PhoneDevicePreviews
@Composable
private fun TambahPromoScreenPreview() {
    MaterialTheme {
        TambahPromoScreen(viewModel = PreviewTambahPromoProvider())
    }
}

// ---- Keuangan ----
@PhoneDevicePreviews
@Composable
private fun CatatanKeuanganScreenPreview() {
    MaterialTheme {
        CatatanKeuanganScreen(
            navController = rememberNavController(),
            viewModel = PreviewCatatanKeuanganProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun DetailCatatanKeuanganScreenPreview() {
    MaterialTheme {
        DetailCatatanKeuanganScreen(
            navController = rememberNavController(),
            viewModel = PreviewDetailCatatanKeuanganProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun TambahCatatanKeuanganScreenPreview() {
    MaterialTheme {
        TambahCatatanKeuanganScreen(
            navController = rememberNavController(),
            viewModel = PreviewTambahCatatanKeuanganProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun LaporanKeuanganScreenPreview() {
    MaterialTheme {
        LaporanKeuanganScreen(
            navController = rememberNavController(),
            viewModel = PreviewLaporanKeuanganProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun LaporanPenjualanScreenPreview() {
    MaterialTheme {
        LaporanPenjualanScreen(
            navController = rememberNavController(),
            viewModel = PreviewLaporanPenjualanProvider()
        )
    }
}

// ---- Audit Log ----
@PhoneDevicePreviews
@FontAccessibilityPreviews
@Composable
private fun AuditLogScreenPreview() {
    MaterialTheme {
        AuditLogScreen(
            navController = rememberNavController(),
            viewModel = PreviewAuditLogProvider()
        )
    }
}

// ---- Referral ----
@PhoneDevicePreviews
@Composable
private fun ReferralScreenPreview() {
    MaterialTheme {
        ReferralScreen(
            navController = rememberNavController(),
            viewModel = PreviewReferralProvider()
        )
    }
}

// ---- Role ----
@PhoneDevicePreviews
@Composable
private fun RoleIzinScreenPreview() {
    MaterialTheme {
        RoleIzinScreen(viewModel = PreviewRoleListProvider())
    }
}

@PhoneDevicePreviews
@Composable
private fun TambahRoleScreenPreview() {
    MaterialTheme {
        TambahRoleScreen(viewModel = PreviewRoleFormProvider())
    }
}

// ---- Profil & Pengaturan ----
@PhoneDevicePreviews
@Composable
private fun ProfilScreenPreview() {
    MaterialTheme {
        ProfilScreen(
            navController = rememberNavController(),
            viewModel = PreviewProfilProvider(),
            securityViewModel = PreviewSecurityProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun PengaturanDashboardScreenPreview() {
    MaterialTheme {
        PengaturanDashboardScreen(navController = rememberNavController())
    }
}

@PhoneDevicePreviews
@Composable
private fun MetodePembayaranScreenPreview() {
    MaterialTheme {
        MetodePembayaranScreen()
    }
}

// ---- Manajemen Pengguna ----
@PhoneDevicePreviews
@Composable
private fun ManajemenPenggunaScreenPreview() {
    MaterialTheme {
        ManajemenPenggunaScreen(
            navController = rememberNavController(),
            userViewModel = PreviewUserListProvider(),
            roleViewModel = PreviewRoleListProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun DaftarUserScreenPreview() {
    MaterialTheme {
        DaftarUserScreen(
            viewModel = PreviewUserListProvider(),
            roleViewModel = PreviewRoleListProvider()
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun TambahPenggunaScreenPreview() {
    MaterialTheme {
        TambahPenggunaScreen(
            viewModel = PreviewUserFormProvider(),
            roleViewModel = PreviewRoleListProvider()
        )
    }
}

// ---- Printer Bluetooth ----
@PhoneDevicePreviews
@Composable
private fun BluetoothPrinterScreenPreview() {
    MaterialTheme {
        BluetoothPrinterScreen(
            navController = rememberNavController(),
            viewModel = PreviewBluetoothPrinterProvider()
        )
    }
}

// ---- Auth & Splash ----
@PhoneDevicePreviews
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}

@PhoneDevicePreviews
@Composable
private fun BarcodeScannerScreenPreview() {
    MaterialTheme {
        BarcodeScannerScreen(
            onBarcodeScanned = {},
            onClose = {}
        )
    }
}

@PhoneDevicePreviews
@Composable
private fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen(statusText = "Menyiapkan aplikasi...")
    }
}
