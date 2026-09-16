package com.ptpws.ikikasir.feature.manajemenstok.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.MovementType
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.StockMovement
import com.ptpws.ikikasir.feature.manajemenstok.domain.usecase.SaveStokAdjustmentUseCase
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UpdateStokViewModel @Inject constructor(
    private val getProdukUseCase: GetProdukUseCase,
    private val updateProdukUseCase: UpdateProdukUseCase,
    private val saveStokAdjustmentUseCase: SaveStokAdjustmentUseCase,
    private val firebaseAuth: FirebaseAuth,
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
        val stokSebelum = currentProduk.stock
        val targetStock = _state.value.newStock.coerceAtLeast(0)

        val updatedProduk = currentProduk.copy(stock = targetStock)

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            updateProdukUseCase(updatedProduk).collect { result ->
                if (result.isSuccess) {
                    val userUid = firebaseAuth.currentUser?.uid ?: ""
                    val kasirNama = firebaseAuth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                        ?: firebaseAuth.currentUser?.email?.substringBefore("@")
                        ?: "Kasir"

                    val (tipe, amount) = when {
                        targetStock >= stokSebelum -> MovementType.IN to (targetStock - stokSebelum)
                        else -> MovementType.OUT to (stokSebelum - targetStock)
                    }

                    // Save movement history according to stock_movements schema (using productId as movementId to update existing document)
                    val movement = StockMovement(
                        movementId = currentProduk.id,
                        productId = currentProduk.id,
                        productName = currentProduk.name,
                        barcode = currentProduk.barcode,
                        type = tipe,
                        quantity = amount,
                        stockBefore = stokSebelum,
                        stockAfter = targetStock,
                        source = "MANUAL_UPDATE",
                        userId = userUid,
                        createdBy = kasirNama,
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                    saveStokAdjustmentUseCase(movement).collect { /* fire and forget */ }

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
