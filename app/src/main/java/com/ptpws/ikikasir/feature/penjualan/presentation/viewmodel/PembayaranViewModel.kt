package com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetCartUseCase
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.ManageCartUseCase
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.ProsesPembayaranUseCase
import com.ptpws.ikikasir.feature.penjualan.presentation.state.PembayaranState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetTaxSettingUseCase

@HiltViewModel
class PembayaranViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val prosesPembayaranUseCase: ProsesPembayaranUseCase,
    private val getTaxSettingUseCase: GetTaxSettingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PembayaranState())
    val state: StateFlow<PembayaranState> = _state.asStateFlow()

    init {
        generateOrderId()
        observeCartAndTax()
    }

    fun generateOrderId() {
        val timeSuffix = (System.currentTimeMillis() % 100000).toString().padStart(5, '0')
        val randomSuffix = (10..99).random()
        _state.update { it.copy(orderId = "#KP-2026-$timeSuffix$randomSuffix") }
    }

    private fun observeCartAndTax() {
        viewModelScope.launch {
            getTaxSettingUseCase().collect { tax ->
                _state.update { it.copy(taxSetting = tax) }
                recalculateTotal()
            }
        }

        viewModelScope.launch {
            getCartUseCase().collect { cartList ->
                val subtotal = cartList.sumOf { it.totalPrice }
                val totalItemCount = cartList.size
                val totalPcsCount = cartList.sumOf { it.quantity }

                _state.update { current ->
                    current.copy(
                        cartItems = cartList,
                        subtotal = subtotal,
                        totalItemCount = totalItemCount,
                        totalPcsCount = totalPcsCount
                    )
                }
                recalculateTotal()
            }
        }
    }

    private fun recalculateTotal() {
        _state.update { current ->
            val subtotal = current.subtotal
            val tax = current.taxSetting

            var ppnAmount = 0.0
            var grandTotal = subtotal
            var ppnLabel = ""

            if (tax.isActive && tax.percentage > 0) {
                val formattedPercent = if (tax.percentage % 1.0 == 0.0) "${tax.percentage.toInt()}%" else "${tax.percentage}%"
                if (tax.type == TaxSetting.TAX_TYPE_EXCLUSIVE) {
                    ppnAmount = subtotal * (tax.percentage / 100.0)
                    grandTotal = subtotal + ppnAmount
                    ppnLabel = "PPN $formattedPercent (Eksklusif)"
                } else {
                    ppnAmount = subtotal - (subtotal / (1 + tax.percentage / 100.0))
                    grandTotal = subtotal
                    ppnLabel = "Harga termasuk PPN $formattedPercent"
                }
            }

            val isTunai = current.metodePembayaran.equals("Tunai", ignoreCase = true)
            val uangDiterima = if (!isTunai) grandTotal else current.uangDiterima
            val kembalian = if (isTunai) (uangDiterima - grandTotal).coerceAtLeast(0.0) else 0.0

            current.copy(
                ppnAmount = ppnAmount,
                grandTotal = grandTotal,
                ppnLabel = ppnLabel,
                uangDiterima = uangDiterima,
                kembalian = kembalian
            )
        }
    }

    fun onMetodePembayaranSelect(metode: String) {
        _state.update { current ->
            val isTunai = metode.equals("Tunai", ignoreCase = true)
            val targetTotal = if (current.grandTotal > 0) current.grandTotal else current.subtotal
            val uangDiterima = if (!isTunai) targetTotal else current.uangDiterima
            val kembalian = if (isTunai) (uangDiterima - targetTotal).coerceAtLeast(0.0) else 0.0

            current.copy(
                metodePembayaran = metode,
                uangDiterima = uangDiterima,
                kembalian = kembalian
            )
        }
    }

    fun onUangDiterimaChange(rawText: String) {
        val digitsOnly = rawText.filter { it.isDigit() }
        val parsedVal = digitsOnly.toDoubleOrNull() ?: 0.0

        val formattedText = if (digitsOnly.isNotEmpty()) {
            NumberFormat.getNumberInstance(Locale("id", "ID")).format(parsedVal.toLong())
        } else ""

        _state.update { current ->
            val targetTotal = if (current.grandTotal > 0) current.grandTotal else current.subtotal
            val kembalian = (parsedVal - targetTotal).coerceAtLeast(0.0)
            current.copy(
                uangDiterimaText = formattedText,
                uangDiterima = parsedVal,
                kembalian = kembalian
            )
        }
    }

    fun onNominalQuickSelect(nominal: Double) {
        val formattedText = NumberFormat.getNumberInstance(Locale("id", "ID")).format(nominal.toLong())
        _state.update { current ->
            val targetTotal = if (current.grandTotal > 0) current.grandTotal else current.subtotal
            val kembalian = (nominal - targetTotal).coerceAtLeast(0.0)
            current.copy(
                uangDiterimaText = formattedText,
                uangDiterima = nominal,
                kembalian = kembalian
            )
        }
    }

    fun onNotesChange(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun onCustomerNameChange(name: String) {
        _state.update { it.copy(customerName = name) }
    }

    fun toggleRincianExpanded() {
        _state.update { it.copy(isRincianExpanded = !it.isRincianExpanded) }
    }

    fun toggleCetakStrukOtomatis() {
        _state.update { it.copy(isCetakStrukOtomatis = !it.isCetakStrukOtomatis) }
    }

    fun prosesPembayaran() {
        val currentState = _state.value
        val isTunai = currentState.metodePembayaran.equals("Tunai", ignoreCase = true)
        val targetTotal = if (currentState.grandTotal > 0) currentState.grandTotal else currentState.subtotal

        // Validasi: Jika Tunai & Uang yang Diterima Kurang dari Total Tagihan -> Tampilkan Failed Dialog
        if (isTunai && currentState.uangDiterima < targetTotal) {
            _state.update {
                it.copy(
                    showFailedDialog = true,
                    errorMessage = "Nominal uang yang diterima kurang dari total tagihan."
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val totalBayar = if (isTunai) currentState.uangDiterima else targetTotal

            prosesPembayaranUseCase(
                kodeTransaksi = currentState.orderId,
                items = currentState.cartItems,
                subtotal = targetTotal,
                totalBayar = totalBayar,
                metodePembayaran = currentState.metodePembayaran,
                notes = currentState.notes,
                customerName = currentState.customerName
            ).collect { result ->
                _state.update { it.copy(isLoading = false) }
                result.fold(
                    onSuccess = { transaksi ->
                        _state.update {
                            it.copy(
                                showSuccessDialog = true,
                                transaksiSukses = transaksi
                            )
                        }
                        generateOrderId()
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                showFailedDialog = true,
                                errorMessage = error.message ?: "Terjadi kesalahan saat memproses pembayaran."
                            )
                        }
                    }
                )
            }
        }
    }

    fun dismissSuccessDialog() {
        _state.update { it.copy(showSuccessDialog = false) }
        generateOrderId()
    }

    fun dismissFailedDialog() {
        _state.update { it.copy(showFailedDialog = false) }
    }
}
