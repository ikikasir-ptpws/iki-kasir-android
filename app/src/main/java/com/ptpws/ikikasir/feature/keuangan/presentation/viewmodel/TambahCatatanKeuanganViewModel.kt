package com.ptpws.ikikasir.feature.keuangan.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.commond.GlobalUploadProgress
import com.ptpws.ikikasir.commond.ImageUploadType
import com.ptpws.ikikasir.core.util.FirebaseStorageHelper
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.keuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.keuangan.domain.repository.CatatanKeuanganRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TambahCatatanUiState(
    val id: String = UUID.randomUUID().toString(),
    val isEditMode: Boolean = false,
    val jenis: String = "PEMASUKAN", // "PEMASUKAN" or "PENGELUARAN"
    val judul: String = "",
    val keterangan: String = "",
    val tanggalMillis: Long = System.currentTimeMillis(),
    val metodePembayaran: String = "Tunai", // "Tunai", "QRIS", "Kartu Debit", "Transfer"
    val nominalText: String = "",
    val buktiUri: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TambahCatatanKeuanganViewModel @Inject constructor(
    private val repository: CatatanKeuanganRepository,
    private val logActivityUseCase: LogActivityUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialId: String? = savedStateHandle.get<String>("id")?.takeIf { it.isNotBlank() && it != "{id}" }

    private val _state = MutableStateFlow(TambahCatatanUiState())
    val state: StateFlow<TambahCatatanUiState> = _state.asStateFlow()

    init {
        if (!initialId.isNullOrBlank()) {
            loadExistingCatatan(initialId)
        }
    }

    private fun loadExistingCatatan(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val existing = repository.getCatatanById(id)
            if (existing != null) {
                _state.update {
                    it.copy(
                        id = existing.id,
                        isEditMode = true,
                        jenis = existing.jenis,
                        judul = existing.judul,
                        keterangan = existing.keterangan,
                        tanggalMillis = existing.tanggalMillis,
                        metodePembayaran = existing.metodePembayaran,
                        nominalText = if (existing.nominal > 0) existing.nominal.toLong().toString() else "",
                        buktiUri = existing.buktiUri,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = "Catatan tidak ditemukan") }
            }
        }
    }

    fun onJenisChange(jenis: String) {
        _state.update { it.copy(jenis = jenis, errorMessage = null) }
    }

    fun onJudulChange(judul: String) {
        _state.update { it.copy(judul = judul, errorMessage = null) }
    }

    fun onKeteranganChange(keterangan: String) {
        _state.update { it.copy(keterangan = keterangan, errorMessage = null) }
    }

    fun onTanggalChange(tanggalMillis: Long) {
        _state.update { it.copy(tanggalMillis = tanggalMillis, errorMessage = null) }
    }

    fun onMetodePembayaranChange(metode: String) {
        _state.update { it.copy(metodePembayaran = metode, errorMessage = null) }
    }

    fun onNominalChange(text: String) {
        val digitsOnly = text.filter { it.isDigit() }
        _state.update { it.copy(nominalText = digitsOnly, errorMessage = null) }
    }

    fun onBuktiUriChange(uri: String?) {
        _state.update { it.copy(buktiUri = uri, errorMessage = null) }
    }

    fun simpanCatatan(onSuccess: () -> Unit) {
        val current = _state.value
        if (current.judul.isBlank()) {
            _state.update { it.copy(errorMessage = "Judul catatan wajib diisi.") }
            return
        }
        val nominalDouble = current.nominalText.toDoubleOrNull() ?: 0.0
        if (nominalDouble <= 0) {
            _state.update { it.copy(errorMessage = "Jumlah nominal wajib diisi dan harus lebih dari 0.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            var finalBuktiUrl = current.buktiUri

            // Jika buktiUri merupakan file/content URI lokal, unggah ke Firebase Storage terlebih dahulu
            if (!current.buktiUri.isNullOrBlank() && (current.buktiUri.startsWith("file://") || current.buktiUri.startsWith("content://"))) {
                GlobalUploadProgress.show(
                    title = "Upload Bukti Pembayaran",
                    message = "Mengunggah bukti ke Firebase Storage...",
                    type = ImageUploadType.BUKTI_PEMBAYARAN,
                    progress = 0.15f
                )
                try {
                    val localUri = Uri.parse(current.buktiUri)
                    finalBuktiUrl = FirebaseStorageHelper.uploadImageToStorage(
                        fileUri = localUri,
                        folderName = "financial_record_payment_proofs",
                        onProgress = { p, msg ->
                            GlobalUploadProgress.updateProgress(p, msg)
                        }
                    )
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Gagal mengunggah foto ke Firebase Storage: ${e.message}"
                        )
                    }
                    GlobalUploadProgress.dismiss()
                    return@launch
                } finally {
                    GlobalUploadProgress.dismiss()
                }
            }

            val item = CatatanKeuangan(
                id = current.id,
                jenis = current.jenis,
                judul = current.judul.trim(),
                keterangan = current.keterangan.trim(),
                tanggalMillis = current.tanggalMillis,
                metodePembayaran = current.metodePembayaran,
                nominal = nominalDouble,
                buktiUri = finalBuktiUrl
            )

            if (current.isEditMode) {
                repository.updateCatatan(item)
                logActivityUseCase(
                    title = "Edit Catatan Keuangan",
                    description = "Memperbarui catatan ${item.jenis}: ${item.judul}",
                    category = "KEUANGAN",
                    action = "UPDATE",
                    isWarning = false
                )
            } else {
                repository.insertCatatan(item)
                logActivityUseCase(
                    title = "Tambah Catatan Keuangan",
                    description = "Menambahkan catatan ${item.jenis}: ${item.judul}",
                    category = "KEUANGAN",
                    action = "CREATE",
                    isWarning = false
                )
            }

            _state.update { it.copy(isLoading = false, isSuccess = true) }
            onSuccess()
        }
    }
}
