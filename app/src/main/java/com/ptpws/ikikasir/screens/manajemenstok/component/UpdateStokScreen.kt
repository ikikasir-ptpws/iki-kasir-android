package com.ptpws.ikikasir.screens.manajemenstok.component

import androidx.compose.runtime.Composable
import com.ptpws.ikikasir.feature.manajemenstok.presentation.viewmodel.UpdateStokViewModel
import com.ptpws.ikikasir.screens.manajemenstok.UpdateStokScreen as MainUpdateStokScreen

@Composable
fun UpdateStokScreen(
    onBack: () -> Unit = {},
    onSimpanPerubahan: () -> Unit = {},
    viewModel: UpdateStokViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    MainUpdateStokScreen(
        onBack = onBack,
        onSuccessUpdate = onSimpanPerubahan,
        viewModel = viewModel
    )
}