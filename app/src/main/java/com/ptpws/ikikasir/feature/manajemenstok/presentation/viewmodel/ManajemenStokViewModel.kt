package com.ptpws.ikikasir.feature.manajemenstok.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.kategori.domain.usecase.GetKategoriUseCase
import com.ptpws.ikikasir.feature.manajemenstok.presentation.state.ManajemenStokState
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
class ManajemenStokViewModel @Inject constructor(
    private val getProdukUseCase: GetProdukUseCase,
    private val updateProdukUseCase: UpdateProdukUseCase,
    private val getKategoriUseCase: GetKategoriUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ManajemenStokState())
    val state: StateFlow<ManajemenStokState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Observe Produk List
            launch {
                getProdukUseCase().collect { produkList ->
                    val totalUnit = produkList.sumOf { it.stock }
                    val restockDibutuhkan = produkList.count { it.stock <= it.lowStockThreshold }
                    _state.update { current ->
                        current.copy(
                            produkList = produkList,
                            totalUnitKeseluruhan = totalUnit,
                            jumlahRestockDibutuhkan = restockDibutuhkan,
                            isLoading = false
                        )
                    }
                }
            }

            // Observe Kategori List
            launch {
                getKategoriUseCase().collect { kategoriList ->
                    _state.update { it.copy(kategoriList = kategoriList) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onCategoryFilterChange(categoryId: String?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun toggleFilterKritisOnly() {
        _state.update { it.copy(isFilterKritisOnly = !it.isFilterKritisOnly) }
    }

    fun openRestockDialog(produk: Produk) {
        _state.update { 
            it.copy(
                selectedRestockProduk = produk,
                restockTambahJumlahText = ""
            ) 
        }
    }

    fun closeRestockDialog() {
        _state.update { 
            it.copy(
                selectedRestockProduk = null,
                restockTambahJumlahText = ""
            ) 
        }
    }

    fun onRestockJumlahChange(jumlah: String) {
        _state.update { it.copy(restockTambahJumlahText = jumlah) }
    }

    fun simpanRestock() {
        val currentProduk = _state.value.selectedRestockProduk ?: return
        val addedStock = _state.value.restockTambahJumlahText.toIntOrNull() ?: 0

        if (addedStock <= 0) {
            _state.update { it.copy(errorMessage = "Jumlah restock harus lebih dari 0") }
            return
        }

        val updatedProduk = currentProduk.copy(
            stock = currentProduk.stock + addedStock
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            updateProdukUseCase(updatedProduk).collect { result ->
                if (result.isSuccess) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            selectedRestockProduk = null,
                            restockTambahJumlahText = "",
                            isSuccessRestock = true
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

    fun clearSuccessRestock() {
        _state.update { it.copy(isSuccessRestock = false) }
    }
}
