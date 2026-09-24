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

@HiltViewModel
class PembayaranViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val prosesPembayaranUseCase: ProsesPembayaranUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PembayaranState())
    val state: StateFlow<PembayaranState> = _state.asStateFlow()

    init {
        generateOrderId()
        observeCart()
    }

    fun generateOrderId() {
        val timeSuffix = (System.currentTimeMillis() % 100000).toString().padStart(5, '0')
        val randomSuffix = (10..99).random()
        _state.update { it.copy(orderId = "#KP-2026-$timeSuffix$randomSuffix") }
    }

    private fun observeCart() {
        viewModelScope.launch {
            getCartUseCase().collect { cartList ->
                val subtotal = cartList.sumOf { it.totalPrice }
                val totalItemCount = cartList.size
                val totalPcsCount = cartList.sumOf { it.quantity }

                _state.update { current ->
                    val isTunai = current.metodePembayaran.equals("Tunai", ignoreCase = true)
                    val uangDiterima = if (!isTunai) subtotal else current.uangDiterima
                    val kembalian = if (isTunai) (uangDiterima - subtotal).coerceAtLeast(0.0) else 0.0

                    current.copy(
                        cartItems = cartList,
                        subtotal = subtotal,
                        totalItemCount = totalItemCount,
                        totalPcsCount = totalPcsCount,
                        uangDiterima = uangDiterima,
                        kembalian = kembalian
                    )
                }
            }
        }
    }

    fun onMetodePembayaranSelect(metode: String) {
        _state.update { current ->
            val isTunai = metode.equals("Tunai", ignoreCase = true)
            val uangDiterima = if (!isTunai) current.subtotal else current.uangDiterima
            val kembalian = if (isTunai) (uangDiterima - current.subtotal).coerceAtLeast(0.0) else 0.0

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
            val kembalian = (parsedVal - current.subtotal).coerceAtLeast(0.0)
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
            val kembalian = (nominal - current.subtotal).coerceAtLeast(0.0)
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

        // Validasi: Jika Tunai & Uang yang Diterima Kurang dari Subtotal -> Tampilkan Failed Dialog
        if (isTunai && currentState.uangDiterima < currentState.subtotal) {
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

            val totalBayar = if (isTunai) currentState.uangDiterima else currentState.subtotal

            prosesPembayaranUseCase(
                kodeTransaksi = currentState.orderId,
                items = currentState.cartItems,
                subtotal = currentState.subtotal,
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
