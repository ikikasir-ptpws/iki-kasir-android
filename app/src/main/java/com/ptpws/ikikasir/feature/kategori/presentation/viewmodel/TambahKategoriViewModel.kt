package com.ptpws.ikikasir.feature.kategori.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.domain.usecase.InsertKategoriUseCase
import com.ptpws.ikikasir.feature.kategori.domain.usecase.UpdateKategoriUseCase
import com.ptpws.ikikasir.feature.kategori.presentation.state.KategoriFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TambahKategoriViewModel @Inject constructor(
    private val insertKategoriUseCase: InsertKategoriUseCase,
    private val updateKategoriUseCase: UpdateKategoriUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(KategoriFormState())
    val formState: StateFlow<KategoriFormState> = _formState.asStateFlow()

    init {
        val kategoriId = savedStateHandle.get<String>("kategoriId")
        val kategoriNama = savedStateHandle.get<String>("kategoriNama")
        val kategoriDeskripsi = savedStateHandle.get<String>("kategoriDeskripsi")
        val kategoriIcon = savedStateHandle.get<String>("kategoriIcon")
        val kategoriColor = savedStateHandle.get<String>("kategoriColor")

        if (!kategoriId.isNullOrBlank()) {
            _formState.update {
                it.copy(
                    id = kategoriId,
                    nama = kategoriNama ?: "",
                    deskripsi = kategoriDeskripsi ?: "",
                    iconName = kategoriIcon ?: "BakeryDining",
                    colorHex = kategoriColor ?: "#4F46E5",
                    isEditMode = true
                )
            }
        }
    }

    fun initFromKategori(kategori: Kategori) {
        _formState.update {
            it.copy(
                id = kategori.id,
                nama = kategori.nama,
                deskripsi = kategori.deskripsi,
                iconName = kategori.iconName,
                colorHex = kategori.colorHex,
                isVisibleInCashier = kategori.isVisibleInCashier,
                productCount = kategori.productCount,
                lowStockCount = kategori.lowStockCount,
                outOfStockCount = kategori.outOfStockCount,
                isEditMode = true
            )
        }
    }

    fun onNamaChange(nama: String) {
        _formState.update { it.copy(nama = nama, errorMessage = null) }
    }

    fun onDeskripsiChange(deskripsi: String) {
        _formState.update { it.copy(deskripsi = deskripsi) }
    }

    fun onIconChange(iconName: String) {
        _formState.update { it.copy(iconName = iconName) }
    }

    fun onColorChange(colorHex: String) {
        _formState.update { it.copy(colorHex = colorHex) }
    }

    fun onVisibilityChange(isVisible: Boolean) {
        _formState.update { it.copy(isVisibleInCashier = isVisible) }
    }

    fun simpanKategori() {
        val currentState = _formState.value
        val namaTrimmed = currentState.nama.trim()

        if (namaTrimmed.isBlank()) {
            _formState.update { it.copy(errorMessage = "Nama kategori tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMessage = null) }

            val combinedIconUrl = currentState.iconName + "|" + currentState.colorHex

            val kategori = Kategori(
                id = currentState.id,
                name = namaTrimmed,
                description = currentState.deskripsi.trim(),
                iconUrl = combinedIconUrl,
                isVisibleInCashier = currentState.isVisibleInCashier,
                productCount = currentState.productCount,
                lowStockCount = currentState.lowStockCount,
                outOfStockCount = currentState.outOfStockCount,
                updatedAt = Timestamp.now()
            )

            val flow = if (currentState.isEditMode) {
                updateKategoriUseCase(kategori)
            } else {
                insertKategoriUseCase(kategori)
            }

            flow.collect { result ->
                if (result.isSuccess) {
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                } else {
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Gagal menyimpan kategori"
                        )
                    }
                }
            }
        }
    }

    fun resetSuccess() {
        _formState.update { it.copy(isSuccess = false) }
    }
}