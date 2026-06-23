package com.ptpws.ikikasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.app.ui.screen.DaftarProdukScreen
import com.example.app.ui.screen.DashboardScreen
import com.example.app.ui.screen.MenuFullScreen
import com.example.app.ui.screen.TambahProdukScreen
import com.ikikasir.app.ui.screen.LoginScreen
import com.ptpws.ikikasir.screens.SplashScreen
import com.ptpws.ikikasir.screens.keuangan.AuditLogScreen
import com.ptpws.ikikasir.screens.keuangan.ManajemenHutangScreen
import com.ptpws.ikikasir.screens.keuangan.MetodePembayaranScreen
import com.ptpws.ikikasir.screens.keuangan.component.BayarHutangScreen
import com.ptpws.ikikasir.screens.keuangan.component.CatatHutangScreen
import com.ptpws.ikikasir.screens.manajemenpengguna.ManajemenPenggunaScreen
import com.ptpws.ikikasir.screens.manajemenpengguna.TambahPenggunaScreen
import com.ptpws.ikikasir.screens.manajemenstok.ManajemenStokScreen
import com.ptpws.ikikasir.screens.manajemenstok.component.UpdateStokScreen
import com.ptpws.ikikasir.screens.navigation.AppNavHost
import com.ptpws.ikikasir.screens.pengaturan.PengaturanDashboardScreen
import com.ptpws.ikikasir.screens.pengaturan.ProfilScreen
import com.ptpws.ikikasir.screens.penjualan.DetailTransaksiScreen
import com.ptpws.ikikasir.screens.penjualan.LaporanPenjualanScreen
import com.ptpws.ikikasir.screens.penjualan.RiwayatTransaksiScreen
import com.ptpws.ikikasir.screens.produk.TambahKategoriScreen
import com.ptpws.ikikasir.screens.promo.ManajemenPromoScreen
import com.ptpws.ikikasir.screens.promo.TambahPromoScreen
import com.ptpws.ikikasir.ui.screens.kasir.KasirScreen
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IKIKASIRTheme {
//                AppNavHost()
                ProfilScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IKIKASIRTheme {
        Greeting("Android")
    }
}