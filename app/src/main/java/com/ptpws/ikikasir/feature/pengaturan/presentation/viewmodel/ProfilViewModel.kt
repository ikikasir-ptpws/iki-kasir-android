package com.ptpws.ikikasir.feature.pengaturan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetNotaSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.SaveNotaSettingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val getNotaSettingUseCase: GetNotaSettingUseCase,
    private val saveNotaSettingUseCase: SaveNotaSettingUseCase
) : ViewModel() {

    private val _notaSetting = MutableStateFlow(NotaSetting())
    val notaSetting: StateFlow<NotaSetting> = _notaSetting.asStateFlow()

    init {
        loadNotaSetting()
    }

    fun loadNotaSetting() {
        viewModelScope.launch {
            getNotaSettingUseCase().collect { setting ->
                _notaSetting.value = setting
            }
        }
    }

    fun saveNotaSetting(setting: NotaSetting, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            saveNotaSettingUseCase(setting).collect { result ->
                result.fold(
                    onSuccess = {
                        _notaSetting.value = setting
                        onComplete(true)
                    },
                    onFailure = {
                        onComplete(false)
                    }
                )
            }
        }
    }
}
