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

import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetTaxSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.SaveTaxSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetPaymentMethodSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.SavePaymentMethodSettingUseCase

@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val getNotaSettingUseCase: GetNotaSettingUseCase,
    private val saveNotaSettingUseCase: SaveNotaSettingUseCase,
    private val getTaxSettingUseCase: GetTaxSettingUseCase,
    private val saveTaxSettingUseCase: SaveTaxSettingUseCase,
    private val getPaymentMethodSettingUseCase: GetPaymentMethodSettingUseCase,
    private val savePaymentMethodSettingUseCase: SavePaymentMethodSettingUseCase
) : ViewModel() {

    private val _notaSetting = MutableStateFlow(NotaSetting())
    val notaSetting: StateFlow<NotaSetting> = _notaSetting.asStateFlow()

    private val _taxSetting = MutableStateFlow(TaxSetting())
    val taxSetting: StateFlow<TaxSetting> = _taxSetting.asStateFlow()

    private val _paymentMethodSetting = MutableStateFlow(PaymentMethodSetting())
    val paymentMethodSetting: StateFlow<PaymentMethodSetting> = _paymentMethodSetting.asStateFlow()

    init {
        loadNotaSetting()
        loadTaxSetting()
        loadPaymentMethodSetting()
    }

    fun loadNotaSetting() {
        viewModelScope.launch {
            getNotaSettingUseCase().collect { setting ->
                _notaSetting.value = setting
            }
        }
    }

    fun loadTaxSetting() {
        viewModelScope.launch {
            getTaxSettingUseCase().collect { setting ->
                _taxSetting.value = setting
            }
        }
    }

    fun loadPaymentMethodSetting() {
        viewModelScope.launch {
            getPaymentMethodSettingUseCase().collect { setting ->
                _paymentMethodSetting.value = setting
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

    fun saveTaxSetting(setting: TaxSetting, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            saveTaxSettingUseCase(setting).collect { result ->
                result.fold(
                    onSuccess = {
                        _taxSetting.value = setting
                        onComplete(true)
                    },
                    onFailure = {
                        onComplete(false)
                    }
                )
            }
        }
    }

    fun savePaymentMethodSetting(setting: PaymentMethodSetting, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = savePaymentMethodSettingUseCase(setting)
            result.fold(
                onSuccess = {
                    _paymentMethodSetting.value = setting
                    onComplete(true)
                },
                onFailure = {
                    onComplete(false)
                }
            )
        }
    }
}
