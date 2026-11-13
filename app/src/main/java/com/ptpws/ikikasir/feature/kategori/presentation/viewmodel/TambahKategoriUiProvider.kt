package com.ptpws.ikikasir.feature.kategori.presentation.viewmodel

import com.ptpws.ikikasir.feature.kategori.presentation.state.KategoriFormState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk TambahKategoriUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface TambahKategoriUiProvider {
    val formState: StateFlow<KategoriFormState>
    fun onNamaChange(nama: String)
    fun onDeskripsiChange(deskripsi: String)
    fun onIconChange(iconName: String)
    fun onColorChange(colorHex: String)
    fun onVisibilityChange(isVisible: Boolean)
    fun simpanKategori()
    fun resetSuccess()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke TambahKategoriViewModel asli.
 */
class HiltTambahKategoriUiProvider(private val viewModel: TambahKategoriViewModel) : TambahKategoriUiProvider {
    override val formState: StateFlow<KategoriFormState> get() = viewModel.formState
    override fun onNamaChange(nama: String) = viewModel.onNamaChange(nama)
    override fun onDeskripsiChange(deskripsi: String) = viewModel.onDeskripsiChange(deskripsi)
    override fun onIconChange(iconName: String) = viewModel.onIconChange(iconName)
    override fun onColorChange(colorHex: String) = viewModel.onColorChange(colorHex)
    override fun onVisibilityChange(isVisible: Boolean) = viewModel.onVisibilityChange(isVisible)
    override fun simpanKategori() = viewModel.simpanKategori()
    override fun resetSuccess() = viewModel.resetSuccess()
}
