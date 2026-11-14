package com.ptpws.ikikasir.feature.penjualan.presentation.viewmodel

import android.content.Context
import com.ptpws.ikikasir.feature.penjualan.presentation.state.PembayaranState
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk PembayaranUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface PembayaranUiProvider {
    val state: StateFlow<PembayaranState>
    fun onSelectPromo(promo: Promo?)
    fun onMetodePembayaranSelect(metode: String)
    fun onUangDiterimaChange(rawText: String)
    fun onCustomerNameChange(name: String)
    fun onTableNumberChange(tableNumber: String)
    fun onNotesChange(notes: String)
    fun onBuktiPembayaranChange(uri: String?)
    fun toggleRincianExpanded()
    fun toggleCetakStrukOtomatis()
    fun checkPrinterStatus(context: Context)
    fun prosesPembayaran()
    fun dismissSuccessDialog()
    fun dismissFailedDialog()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke PembayaranViewModel asli.
 */
class HiltPembayaranUiProvider(private val viewModel: PembayaranViewModel) : PembayaranUiProvider {
    override val state: StateFlow<PembayaranState> get() = viewModel.state
    override fun onSelectPromo(promo: Promo?) = viewModel.onSelectPromo(promo)
    override fun onMetodePembayaranSelect(metode: String) = viewModel.onMetodePembayaranSelect(metode)
    override fun onUangDiterimaChange(rawText: String) = viewModel.onUangDiterimaChange(rawText)
    override fun onCustomerNameChange(name: String) = viewModel.onCustomerNameChange(name)
    override fun onTableNumberChange(tableNumber: String) = viewModel.onTableNumberChange(tableNumber)
    override fun onNotesChange(notes: String) = viewModel.onNotesChange(notes)
    override fun onBuktiPembayaranChange(uri: String?) = viewModel.onBuktiPembayaranChange(uri)
    override fun toggleRincianExpanded() = viewModel.toggleRincianExpanded()
    override fun toggleCetakStrukOtomatis() = viewModel.toggleCetakStrukOtomatis()
    override fun checkPrinterStatus(context: Context) = viewModel.checkPrinterStatus(context)
    override fun prosesPembayaran() = viewModel.prosesPembayaran()
    override fun dismissSuccessDialog() = viewModel.dismissSuccessDialog()
    override fun dismissFailedDialog() = viewModel.dismissFailedDialog()
}
