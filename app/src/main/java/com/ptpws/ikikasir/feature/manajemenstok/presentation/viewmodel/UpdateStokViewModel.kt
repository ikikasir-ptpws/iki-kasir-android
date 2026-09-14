package com.ptpws.ikikasir.feature.manajemenstok.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.manajemenstok.presentation.state.UpdateStokState
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.usecase.GetProdukUseCase
import com.ptpws.ikikasir.feature.produk.domain.usecase.UpdateProdukUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateStokViewModel @Inject constructor(
    private val getProdukUseCase: GetProdukUseCase,
    private val updateProdukUseCase: UpdateProdukUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(UpdateStokState())
    val state: StateFlow<UpdateStokState> = _state.asStateFlow()

    private val initialProdukId: String? = savedStateHandle.get<String>("produkId")

    init {
        loadProdukList()
    }

    private fun loadProdukList() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getProdukUseCase().collect { list ->
                val selected = if (initialProdukId != null) {
                    list.find { it.id == initialProdukId } ?: list.firstOrNull()
                } else {
                    list.firstOrNull()
                }
                _state.update {
                    it.copy(
                        produkList = list,
                        selectedProduk = selected,
                        newStock = selected?.stock ?: 0,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectProduk(produk: Produk) {
        _state.update {
            it.copy(
                selectedProduk = produk,
                newStock = produk.stock,
                showProductDropdown = false
            )
        }
    }

    fun toggleProductDropdown(show: Boolean? = null) {
        _state.update {
            it.copy(showProductDropdown = show ?: !it.showProductDropdown)
        }
    }

    fun incrementStok() {
        _state.update {
            it.copy(newStock = it.newStock + 1)
        }
    }

    fun decrementStok() {
        _state.update {
            it.copy(newStock = (it.newStock - 1).coerceAtLeast(0))
        }
    }

    fun setNewStock(stock: Int) {
        _state.update {
            it.copy(newStock = stock.coerceAtLeast(0))
        }
    }

    fun simpanPerubahan() {
        val currentProduk = _state.value.selectedProduk ?: run {
            _state.update { it.copy(errorMessage = "Pilih produk terlebih dahulu") }
            return
        }
        val targetStock = _state.value.newStock.coerceAtLeast(0)

        val updatedProduk = currentProduk.copy(
            stock = targetStock
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            updateProdukUseCase(updatedProduk).collect { result ->
                if (result.isSuccess) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Gagal memperbarui stok"
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _state.update { it.copy(isSuccess = false) }
    }
}
