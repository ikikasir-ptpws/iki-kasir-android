package com.ptpws.ikikasir.feature.catatankeuangan.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.commond.GlobalUploadProgress
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.core.storage.domain.model.PendingImageTarget
import com.ptpws.ikikasir.core.storage.domain.usecase.QueuePendingImageUseCase
import com.ptpws.ikikasir.core.storage.domain.usecase.UploadFinancialRecordProofUseCase
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase.GetCatatanByIdUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase.InsertCatatanKeuanganUseCase
import com.ptpws.ikikasir.feature.catatankeuangan.domain.usecase.UpdateCatatanKeuanganUseCase
import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.usecase.GetPaymentMethodSettingUseCase
import com.ptpws.ikikasir.feature.produk.presentation.viewmodel.formatInputPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class TambahCatatanFormState(
    val catatanId: String? = null,
    val isEditMode: Boolean = false,
    val type: String = "PEMASUKAN", // "PEMASUKAN" or "PENGELUARAN"
    val title: String = "",
    val date: String = "",
    val paymentMethod: String = "Tunai",
    val amountText: String = "",
    val note: String = "",
    val proofImageUri: Uri? = null,
    val proofImageUrl: String = "",
    val paymentMethodSetting: PaymentMethodSetting = PaymentMethodSetting(),
    val isLoading: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TambahCatatanKeuanganViewModel @Inject constructor(
    private val getCatatanByIdUseCase: GetCatatanByIdUseCase,
    private val insertCatatanKeuanganUseCase: InsertCatatanKeuanganUseCase,
    private val updateCatatanKeuanganUseCase: UpdateCatatanKeuanganUseCase,
    private val getPaymentMethodSettingUseCase: GetPaymentMethodSettingUseCase,
    private val logActivityUseCase: LogActivityUseCase,
    private val uploadFinancialRecordProofUseCase: UploadFinancialRecordProofUseCase,
    private val queuePendingImageUseCase: QueuePendingImageUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(TambahCatatanFormState())
    val state: StateFlow<TambahCatatanFormState> = _state.asStateFlow()

    init {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        _state.update { it.copy(date = formatter.format(Date())) }
        loadPaymentMethodSetting()
    }

    private fun loadPaymentMethodSetting() {
        viewModelScope.launch {
            getPaymentMethodSettingUseCase().collect { pmSetting ->
                _state.update { it.copy(paymentMethodSetting = pmSetting) }
            }
        }
    }

    fun loadCatatanForEdit(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val existing = getCatatanByIdUseCase(id).firstOrNull()
            if (existing != null) {
                val formattedAmount = formatInputPrice(existing.amount.toString())

                _state.update {
                    it.copy(
                        catatanId = existing.id,
                        isEditMode = true,
                        type = existing.type,
                        title = existing.title,
                        date = existing.date,
                        paymentMethod = existing.paymentMethod.ifBlank { "Tunai" },
                        amountText = formattedAmount,
                        note = existing.note,
                        proofImageUrl = existing.proofImageUrl,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTypeChange(type: String) {
        _state.update { currentState ->
            currentState.copy(type = type)
        }
    }

    fun onTitleChange(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun onDateChange(date: String) {
        _state.update { it.copy(date = date) }
    }

    fun onPaymentMethodChange(method: String) {
        _state.update { it.copy(paymentMethod = method) }
    }

    fun onAmountTextChange(amountText: String) {
        val formatted = formatInputPrice(amountText)
        _state.update { it.copy(amountText = formatted) }
    }

    fun onNoteChange(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun onProofImageSelected(uri: Uri) {
        _state.update { it.copy(proofImageUri = uri) }
    }

    fun removeProofImage() {
        _state.update { it.copy(proofImageUri = null, proofImageUrl = "") }
    }

    fun simpanCatatan() {
        val form = _state.value
        if (form.title.isBlank()) {
            val message = "Judul / keterangan catatan tidak boleh kosong"
            GlobalCrudResultDialog.failure(message)
            _state.update { it.copy(errorMessage = message) }
            return
        }

        val rawAmount = form.amountText.replace(".", "").replace(",", "").toDoubleOrNull() ?: 0.0
        if (rawAmount <= 0) {
            val message = "Jumlah nominal harus lebih dari 0"
            GlobalCrudResultDialog.failure(message)
            _state.update { it.copy(errorMessage = message) }
            return
        }

        val isEdit = form.isEditMode && !form.catatanId.isNullOrBlank()
        val targetId = form.catatanId ?: UUID.randomUUID().toString()

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            var finalProofUrl = form.proofImageUrl
            var proofNeedsQueue = false
            val localProofUri = form.proofImageUri
            if (!form.paymentMethod.equals("Tunai", ignoreCase = true) && localProofUri != null) {
                if (networkMonitor.isConnected()) {
                    GlobalUploadProgress.showBuktiPembayaranUpload(progress = 0f)
                    val uploadResult = try {
                        uploadFinancialRecordProofUseCase(
                            localFilePathOrUri = localProofUri.toString(),
                            recordId = targetId,
                            onProgress = { progress ->
                                GlobalUploadProgress.updateProgress(progress, "Mengunggah bukti pembayaran...")
                            }
                        )
                    } catch (error: Exception) {
                        Result.failure(error)
                    } finally {
                        GlobalUploadProgress.dismiss()
                    }
                    val downloadUrl = uploadResult.getOrNull()
                    if (downloadUrl == null) {
                        val message = uploadResult.exceptionOrNull()?.message
                            ?: "Gagal mengunggah bukti pembayaran."
                        GlobalCrudResultDialog.failure(message)
                        _state.update { it.copy(isLoading = false, errorMessage = message) }
                        return@launch
                    }
                    finalProofUrl = downloadUrl
                } else {
                    finalProofUrl = localProofUri.toString()
                    proofNeedsQueue = true
                }
            } else if (form.paymentMethod.equals("Tunai", ignoreCase = true)) {
                finalProofUrl = ""
            }

            val item = CatatanKeuangan(
                id = targetId,
                title = form.title,
                type = form.type,
                amount = rawAmount,
                date = form.date,
                note = form.note,
                paymentMethod = form.paymentMethod,
                proofImageUrl = finalProofUrl,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                isSynced = false
            )

            val flow = if (isEdit) updateCatatanKeuanganUseCase(item) else insertCatatanKeuanganUseCase(item)
            flow.collect { result ->
                result.onSuccess {
                    if (proofNeedsQueue && localProofUri != null) {
                        try {
                            queuePendingImageUseCase(
                                localFilePathOrUri = localProofUri.toString(),
                                target = PendingImageTarget.FINANCIAL_RECORD,
                                documentId = targetId
                            )
                        } catch (error: Exception) {
                            val message = error.message ?: "Gagal menyiapkan upload bukti pembayaran."
                            GlobalCrudResultDialog.failure(message)
                            _state.update { it.copy(isLoading = false, errorMessage = message) }
                            return@collect
                        }
                    }
                    val labelType = if (form.type == "PEMASUKAN") "Pemasukan" else "Pengeluaran"
                    val actionName = if (isEdit) "UPDATE" else "CREATE"
                    val actionTitle = if (isEdit) "Perubahan Catatan: ${form.title}" else "Catatan Baru: ${form.title}"
                    GlobalCrudResultDialog.success(
                        if (isEdit) "Catatan $labelType \"${form.title}\" berhasil diperbarui."
                        else "Catatan $labelType \"${form.title}\" berhasil ditambahkan."
                    )
                    logActivityUseCase(
                        title = actionTitle,
                        description = "Catatan $labelType (${form.paymentMethod}) sebesar Rp ${rawAmount.toLong()} tersimpan.",
                        category = "KEUANGAN",
                        action = actionName,
                        isWarning = false
                    )
                    _state.update { it.copy(isLoading = false, isSavedSuccess = true) }
                }.onFailure { err ->
                    val message = err.message ?: "Gagal menyimpan catatan."
                    GlobalCrudResultDialog.failure(message)
                    _state.update { it.copy(isLoading = false, errorMessage = "Gagal menyimpan: $message") }
                }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(userMessage = null, errorMessage = null) }
    }
}
