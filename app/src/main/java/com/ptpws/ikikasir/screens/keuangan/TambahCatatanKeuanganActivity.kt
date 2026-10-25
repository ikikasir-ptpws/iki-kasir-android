package com.ptpws.ikikasir.screens.keuangan

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.ptpws.ikikasir.commond.BaseActivity
import com.ptpws.ikikasir.commond.GlobalCrudResultDialogHost
import com.ptpws.ikikasir.commond.GlobalUploadProgressHost
import com.ptpws.ikikasir.ui.theme.IKIKASIRTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TambahCatatanKeuanganActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val catatanId = intent.getStringExtra("CATATAN_ID") ?: intent.getStringExtra("catatanId")
        setContent {
            IKIKASIRTheme {
                Box(Modifier.fillMaxSize()) {
                    TambahCatatanKeuanganScreen(
                        catatanId = catatanId,
                        onBack = { finish() },
                        onSimpanSuccess = { finish() }
                    )
                    GlobalCrudResultDialogHost(showSuccess = false)
                    GlobalUploadProgressHost()
                }
            }
        }
    }
}
