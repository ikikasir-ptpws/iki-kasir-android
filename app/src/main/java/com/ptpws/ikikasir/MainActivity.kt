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
import com.ikikasir.app.ui.screen.LoginScreen
import com.ptpws.ikikasir.screens.SplashScreen
import com.ptpws.ikikasir.screens.manajemenstok.ManajemenStokScreen
import com.ptpws.ikikasir.screens.navigation.AppNavHost
import com.ptpws.ikikasir.ui.screens.kasir.KasirScreen
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IKIKASIRTheme {
//                AppNavHost()
                KasirScreen()
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