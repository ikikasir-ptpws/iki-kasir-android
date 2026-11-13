package com.ptpws.ikikasir.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptpws.ikikasir.R
import com.ptpws.ikikasir.commond.interfamily

@Composable
fun SplashScreen(
    isLoading: Boolean = false,
    statusText: String? = null
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Responsif: logo mengikuti lebar layar. Rasio 270/360 = 0.75 menghasilkan ukuran
        // yang PERSIS sama dengan ukuran asli (270 dp) pada layar 360 dp, menyusut di 320 dp,
        // dan tidak melebar berlebihan di layar >= 360 dp (dibatasi maks 270 dp).
        val logoWidth = minOf(maxWidth * 0.75f, 270.dp)
        // Rasio aspek gambar asli 270 x 221 dp, dipakai agar logo tidak gepeng.
        val logoHeight = logoWidth * (221f / 270f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logoikikasir),
                contentDescription = "logo",
                modifier = Modifier.size(logoWidth, logoHeight)
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(28.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = Color(0xFF4F46E5)
                )
                if (!statusText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusText,
                        fontFamily = interfamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}
