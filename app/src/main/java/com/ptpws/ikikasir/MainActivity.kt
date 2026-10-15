package com.ptpws.ikikasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import com.ptpws.ikikasir.screens.manajemenpengguna.DaftarUserScreen
import com.ptpws.ikikasir.core.responsive.presentation.util.ProvideWindowSizeInfo
import com.ptpws.ikikasir.screens.navigation.AppNavHost
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme
import dagger.hilt.android.AndroidEntryPoint

import com.ptpws.ikikasir.commond.BaseActivity

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProvideWindowSizeInfo {
                IKIKASIRTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        AppNavHost()
                    }
                }
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