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
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetPaymentMethodSettingUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetTableSettingUseCase
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.model.isAvailableOn
import com.ptpws.ikikasir.feature.promo.domain.usecase.GetActivePromosUseCase

import com.ptpws.ikikasir.feature.penjualan.domain.model.getEffectivePrice
import com.ptpws.ikikasir.feature.penjualan.domain.model.getEffectiveSubtotal
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import android.content.Context
import com.ptpws.ikikasir.feature.bluetooth.data.BluetoothPrinterConnection
import com.ptpws.ikikasir.feature.bluetooth.data.preferences.BluetoothPrinterPreferences
import com.ptpws.ikikasir.feature.bluetooth.domain.repository.BluetoothPrinterRepository
import com.ptpws.ikikasir.feature.penjualan.presentation.state.PrinterStatus

@HiltViewModel
class PembayaranViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val prosesPembayaranUseCase: ProsesPembayaranUseCase,
    private val getTaxSettingUseCase: GetTaxSettingUseCase,
    private val getPaymentMethodSettingUseCase: GetPaymentMethodSettingUseCase,
    private val getTableSettingUseCase: GetTableSettingUseCase,
    private val getActivePromosUseCase: GetActivePromosUseCase,
    private val logActivityUseCase: LogActivityUseCase,
    private val bluetoothPrinterPreferences: BluetoothPrinterPreferences,
    private val bluetoothPrinterRepository: BluetoothPrinterRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PembayaranState())
    val state: StateFlow<PembayaranState> = _state.asStateFlow()

    init {
        generateOrderId()
        val savedAutoPrint = bluetoothPrinterPreferences.isAutoPrint()
        _state.update { it.copy(isCetakStrukOtomatis = savedAutoPrint) }
        observeCartAndTax()
        observePrinterConnection()
    }

    fun generateOrderId() {
        val timeSuffix = (System.currentTimeMillis() % 100000).toString().padStart(5, '0')
        val randomSuffix = (10..99).random()
        _state.update { it.copy(orderId = "#KP-2026-$timeSuffix$randomSuffix") }
    }

    private fun observeCartAndTax() {
        viewModelScope.launch {
            getActivePromosUseCase().collect { promos ->
                _state.update { current ->
                    current.copy(
                        activePromos = promos,
                        selectedPromo = current.selectedPromo?.takeIf { selected ->
                            promos.any { it.id == selected.id }
                        }
                    )
                }
                recalculateTotal()
            }
        }

        viewModelScope.launch {
            getTableSettingUseCase().collect { tableSetting ->
                _state.update { it.copy(tableSetting = tableSetting) }
            }
        }

        viewModelScope.launch {
            getTaxSettingUseCase().collect { tax ->
                _state.update { it.copy(taxSetting = tax) }
                recalculateTotal()
            }
        }

        viewModelScope.launch {
            getPaymentMethodSettingUseCase().collect { pmSetting ->
                _state.update { current ->
                    var selected = current.metodePembayaran
                    val isCurrentActive = when (selected.lowercase()) {
                        "tunai" -> pmSetting.isTunaiEnabled
                        "qris" -> pmSetting.isQrisEnabled
                        "transfer" -> pmSetting.isTransferEnabled
                        "kartu debit", "kartu kredit" -> pmSetting.isKartuKreditEnabled
                        else -> true
                    }
                    if (!isCurrentActive) {
                        selected = when {
                            pmSetting.isTunaiEnabled -> "Tunai"
                            pmSetting.isQrisEnabled -> "QRIS"
                            pmSetting.isTransferEnabled -> "Transfer"
                            pmSetting.isKartuKreditEnabled -> "Kartu Debit"
                            else -> "Tunai"
                        }
                    }
                    current.copy(
                        paymentMethodSetting = pmSetting,
                        metodePembayaran = selected
                    )
                }
            }
        }

        viewModelScope.launch {
            getCartUseCase().collect { cartList ->
                val tax = _state.value.taxSetting
                val subtotal = if (tax.isActive && tax.percentage > 0 && tax.type == TaxSetting.TAX_TYPE_INCLUSIVE) {
                    cartList.sumOf { it.getEffectiveSubtotal(tax) }
                } else {
                    cartList.sumOf { it.totalPrice }
                }
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

    private fun observePrinterConnection() {
        viewModelScope.launch {
            BluetoothPrinterConnection.connectedAddressFlow.collect { connectedAddress ->
                val setting = bluetoothPrinterPreferences.getSetting()
                if (setting.savedAddress.isNotBlank()) {
                    if (connectedAddress == setting.savedAddress) {
                        _state.update {
                            it.copy(
                                printerStatus = PrinterStatus.SIAP,
                                isPrinterSiap = true,
                                printerName = setting.savedName
                            )
                        }
                    } else if (connectedAddress == null && _state.value.printerStatus == PrinterStatus.SIAP) {
                        _state.update {
                            it.copy(
                                printerStatus = PrinterStatus.BELUM_TERHUBUNG,
                                isPrinterSiap = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun onSelectPromo(promo: Promo?) {
        _state.update { current ->
            current.copy(
                selectedPromo = promo?.takeIf {
                    it.isAvailableOn() && current.activePromos.any { active -> active.id == it.id }
                }
            )
        }
        recalculateTotal()
    }

    private fun recalculateTotal() {
        _state.update { current ->
            val tax = current.taxSetting
            val subtotal = if (tax.isActive && tax.percentage > 0 && tax.type == TaxSetting.TAX_TYPE_INCLUSIVE) {
                current.cartItems.sumOf { it.getEffectiveSubtotal(tax) }
            } else {
                current.cartItems.sumOf { it.totalPrice }
            }
            val promo = current.selectedPromo?.takeIf { selected ->
                selected.isAvailableOn() && current.activePromos.any { it.id == selected.id }
            }

            val promoDiscountAmount = if (
                promo != null &&
                current.cartItems.isNotEmpty()
            ) {
                val eligibleSubtotal = if (promo.items.isEmpty()) {
                    subtotal
                } else {
                    val promoProductIds = promo.items.map { it.productId }.toSet()
                    if (tax.isActive && tax.percentage > 0 && tax.type == TaxSetting.TAX_TYPE_INCLUSIVE) {
                        current.cartItems.filter { it.produk.id in promoProductIds }.sumOf { it.getEffectiveSubtotal(tax) }
                    } else {
                        current.cartItems.filter { it.produk.id in promoProductIds }.sumOf { it.totalPrice }
                    }
                }
                if (eligibleSubtotal > 0) {
                    val discount = if (promo.diskonType.equals("%", ignoreCase = true)) {
                        eligibleSubtotal * (promo.nilaiDiskon / 100.0)
                    } else {
                        promo.nilaiDiskon
                    }
                    discount.coerceAtMost(subtotal)
                } else 0.0
            } else 0.0

            val discountedSubtotal = (subtotal - promoDiscountAmount).coerceAtLeast(0.0)

            var ppnAmount = 0.0
            var grandTotal = discountedSubtotal
            var ppnLabel = ""

            if (tax.isActive && tax.percentage > 0) {
                val formattedPercent = if (tax.percentage % 1.0 == 0.0) "${tax.percentage.toInt()}%" else "${tax.percentage}%"
                if (tax.type == TaxSetting.TAX_TYPE_EXCLUSIVE) {
                    ppnAmount = discountedSubtotal * (tax.percentage / 100.0)
                    grandTotal = discountedSubtotal + ppnAmount
                    ppnLabel = "PPN $formattedPercent (Eksklusif)"
                } else {
                    ppnAmount = discountedSubtotal - (discountedSubtotal / (1 + tax.percentage / 100.0))
                    grandTotal = discountedSubtotal
                    ppnLabel = "Harga termasuk PPN $formattedPercent"
                }
            }

            val isTunai = current.metodePembayaran.equals("Tunai", ignoreCase = true)
            val uangDiterima = if (!isTunai) grandTotal else current.uangDiterima
            val kembalian = if (isTunai) (uangDiterima - grandTotal).coerceAtLeast(0.0) else 0.0

            current.copy(
                subtotal = subtotal,
                selectedPromo = promo,
                promoDiscountAmount = promoDiscountAmount,
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

    fun onTableNumberChange(tableNumber: String) {
        _state.update { it.copy(tableNumber = tableNumber) }
    }

    fun toggleRincianExpanded() {
        _state.update { it.copy(isRincianExpanded = !it.isRincianExpanded) }
    }

    fun toggleCetakStrukOtomatis() {
        val newSetting = !_state.value.isCetakStrukOtomatis
        bluetoothPrinterPreferences.setAutoPrint(newSetting)
        _state.update { it.copy(isCetakStrukOtomatis = newSetting) }
    }

    fun checkPrinterStatus(context: Context) {
        val isBluetoothOn = bluetoothPrinterRepository.isBluetoothEnabled()
        if (!isBluetoothOn) {
            _state.update {
                it.copy(
                    printerStatus = PrinterStatus.BLUETOOTH_MATI,
                    isPrinterSiap = false
                )
            }
            return
        }

        val setting = bluetoothPrinterPreferences.getSetting()
        if (setting.savedAddress.isBlank()) {
            _state.update {
                it.copy(
                    printerStatus = PrinterStatus.BELUM_DIATUR,
                    isPrinterSiap = false
                )
            }
            return
        }

        if (BluetoothPrinterConnection.isConnected(setting.savedAddress)) {
            _state.update {
                it.copy(
                    printerStatus = PrinterStatus.SIAP,
                    isPrinterSiap = true,
                    printerName = setting.savedName
                )
            }
            return
        }

        // Coba hubungkan di background secara halus
        viewModelScope.launch {
            _state.update {
                it.copy(
                    printerStatus = PrinterStatus.CONNECTING,
                    isPrinterSiap = false,
                    printerName = setting.savedName
                )
            }
            try {
                BluetoothPrinterConnection.connect(context, setting.savedAddress)
                val isNowConnected = BluetoothPrinterConnection.isConnected(setting.savedAddress)
                _state.update {
                    it.copy(
                        printerStatus = if (isNowConnected) PrinterStatus.SIAP else PrinterStatus.BELUM_TERHUBUNG,
                        isPrinterSiap = isNowConnected
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        printerStatus = PrinterStatus.BELUM_TERHUBUNG,
                        isPrinterSiap = false
                    )
                }
            }
        }
    }

    fun prosesPembayaran() {
        _state.update { current ->
            if (current.selectedPromo?.let { promo ->
                    !promo.isAvailableOn() || current.activePromos.none { it.id == promo.id }
                } == true
            ) {
                current.copy(selectedPromo = null)
            } else {
                current
            }
        }
        recalculateTotal()
        val currentState = _state.value
        val isTunai = currentState.metodePembayaran.equals("Tunai", ignoreCase = true)
        val targetTotal = if (currentState.grandTotal > 0) currentState.grandTotal else currentState.subtotal

        // Validasi: Jika Tunai & Uang yang Diterima Kurang dari Total Tagihan -> Tampilkan Failed Dialog
        if (isTunai && currentState.uangDiterima < targetTotal) {
            val message = "Nominal uang yang diterima kurang dari total tagihan."
            _state.update {
                it.copy(
                    showFailedDialog = true,
                    errorMessage = message
                )
            }
            return
        }

        val isTaxInclusive = currentState.taxSetting.isActive &&
            currentState.taxSetting.percentage > 0 &&
            currentState.taxSetting.type == TaxSetting.TAX_TYPE_INCLUSIVE

        val itemsForTransaction = if (isTaxInclusive) {
            currentState.cartItems.map { item ->
                item.copy(
                    customPrice = item.getEffectivePrice(currentState.taxSetting)
                )
            }
        } else {
            currentState.cartItems
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val totalBayar = if (isTunai) currentState.uangDiterima else targetTotal
            val tax = currentState.taxSetting
            val ppnPercentage = if (tax.isActive && tax.percentage > 0) tax.percentage else 0.0
            val ppnType = if (tax.isActive && tax.percentage > 0) tax.type else ""

            prosesPembayaranUseCase(
                kodeTransaksi = currentState.orderId,
                items = itemsForTransaction,
                subtotal = currentState.subtotal,
                ppnPercentage = ppnPercentage,
                ppnAmount = currentState.ppnAmount,
                ppnType = ppnType,
                totalBayar = totalBayar,
                metodePembayaran = currentState.metodePembayaran,
                discount = currentState.promoDiscountAmount,
                notes = currentState.notes,
                customerName = currentState.customerName,
                tableNumber = currentState.tableNumber,
                isTaxInclusive = isTaxInclusive
            ).collect { result ->
                _state.update { it.copy(isLoading = false) }
                result.fold(
                    onSuccess = { transaksi ->
                        logActivityUseCase(
                            title = "Transaksi Baru: ${currentState.orderId}",
                            description = "Penjualan ${currentState.totalItemCount} item (${currentState.totalPcsCount} pcs) via ${currentState.metodePembayaran} sebesar Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(targetTotal.toLong())}.",
                            category = "TRANSACTION",
                            action = "CREATE",
                            isWarning = false
                        )
                        _state.update {
                            it.copy(
                                showSuccessDialog = true,
                                transaksiSukses = transaksi
                            )
                        }
                        generateOrderId()
                    },
                    onFailure = { error ->
                        val message = error.message ?: "Terjadi kesalahan saat memproses pembayaran."
                        logActivityUseCase(
                            title = "Pembatalan Transaksi: ${currentState.orderId}",
                            description = "Alasan: Gagal memproses transaksi. ${error.message ?: ""}",
                            category = "TRANSACTION",
                            action = "CANCEL",
                            isWarning = true
                        )
                        _state.update {
                            it.copy(
                                showFailedDialog = true,
                                errorMessage = message
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
