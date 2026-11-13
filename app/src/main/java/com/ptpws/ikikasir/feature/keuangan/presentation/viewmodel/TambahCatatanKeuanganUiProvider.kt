package com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk TambahCatatanKeuanganUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface TambahCatatanKeuanganUiProvider {
    val state: StateFlow<TambahCatatanUiState>
    fun onJenisChange(jenis: String)
    fun onJudulChange(judul: String)
    fun onKeteranganChange(keterangan: String)
    fun onTanggalChange(selectedDateMillis: Long)
    fun onMetodePembayaranChange(metode: String)
    fun onNominalChange(text: String)
    fun onBuktiUriChange(uri: String?)
    fun simpanCatatan(onSuccess: (isEditMode: Boolean) -> Unit, onFailure: (errorMessage: String) -> Unit)
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke TambahCatatanKeuanganViewModel asli.
 */
class HiltTambahCatatanKeuanganUiProvider(private val viewModel: TambahCatatanKeuanganViewModel) : TambahCatatanKeuanganUiProvider {
    override val state: StateFlow<TambahCatatanUiState> get() = viewModel.state
    override fun onJenisChange(jenis: String) = viewModel.onJenisChange(jenis)
    override fun onJudulChange(judul: String) = viewModel.onJudulChange(judul)
    override fun onKeteranganChange(keterangan: String) = viewModel.onKeteranganChange(keterangan)
    override fun onTanggalChange(selectedDateMillis: Long) = viewModel.onTanggalChange(selectedDateMillis)
    override fun onMetodePembayaranChange(metode: String) = viewModel.onMetodePembayaranChange(metode)
    override fun onNominalChange(text: String) = viewModel.onNominalChange(text)
    override fun onBuktiUriChange(uri: String?) = viewModel.onBuktiUriChange(uri)
    override fun simpanCatatan(onSuccess: (isEditMode: Boolean) -> Unit, onFailure: (errorMessage: String) -> Unit) = viewModel.simpanCatatan(onSuccess)
}
