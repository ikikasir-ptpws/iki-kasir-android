package com.ptpws.ikikasir.feature.role.presentation.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.ptpws.ikikasir.commond.GlobalCrudResultDialogHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TambahRoleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roleId = intent.getStringExtra("ROLE_ID")
        val permissionsOnly = intent.getBooleanExtra("PERMISSIONS_ONLY", false)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize()) {
                    TambahRoleScreen(
                        roleId = roleId,
                        permissionsOnly = permissionsOnly,
                        onBack = { finish() },
                        onSimpanRole = { finish() }
                    )
                    GlobalCrudResultDialogHost(showSuccess = false)
                }
            }
        }
    }
}
