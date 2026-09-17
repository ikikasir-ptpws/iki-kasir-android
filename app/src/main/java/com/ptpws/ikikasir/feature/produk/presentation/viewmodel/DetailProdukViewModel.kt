package com.ptpws.ikikasir.feature.produk.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.feature.kategori.domain.usecase.GetKategoriUseCase
import com.ptpws.ikikasir.feature.penjualan.domain.usecase.GetAllTransaksiUseCase
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.usecase.DeleteProdukUseCase
import com.ptpws.ikikasir.feature.produk.domain.usecase.GetProdukUseCase
import com.ptpws.ikikasir.feature.produk.domain.usecase.UpdateProdukUseCase
import com.ptpws.ikikasir.feature.produk.presentation.state.DetailProdukState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DetailProdukViewModel @Inject constructor(
    private val getProdukUseCase: GetProdukUseCase,
    private val updateProdukUseCase: UpdateProdukUseCase,
    private val deleteProdukUseCase: DeleteProdukUseCase,
    private val getKategoriUseCase: GetKategoriUseCase,
    private val getAllTransaksiUseCase: GetAllTransaksiUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(DetailProdukState())
    val state: StateFlow<DetailProdukState> = _state.asStateFlow()

    private var activeProdukId: String? = null

    init {
        val produkId = savedStateHandle.get<String>("produkId")
        if (!produkId.isNullOrBlank()) {
            loadProduk(produkId)
        }
    }

    fun loadProduk(produkId: String) {
        if (activeProdukId == produkId) return
        activeProdukId = produkId

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getProdukUseCase.getById(produkId).collect { produk ->
                if (produk != null) {
                    _state.update { current ->
                        current.copy(
                            produk = produk,
                            isLoading = false
                        )
                    }
                    loadCategoryName(produk.categoryId)
                } else {
                    _state.update { current ->
                        current.copy(isLoading = false)
                    }
                }
            }
        }

        observeSalesSummary(produkId)
    }

    private fun observeSalesSummary(produkId: String) {
        viewModelScope.launch {
            getAllTransaksiUseCase().collect { transactions ->
                val startOfTodayMillis = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val thirtyDaysAgoMillis = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -30)
                }.timeInMillis

                var todayQty = 0
                var thirtyDaysQty = 0

                transactions.forEach { tx ->
                    if (!tx.status.equals("CANCELLED", ignoreCase = true) && !tx.status.equals("BATAL", ignoreCase = true)) {
                        val txMillis = tx.createdAt.toDate().time
                        tx.items.forEach { cartItem ->
                            if (cartItem.produk.id == produkId) {
                                if (txMillis >= startOfTodayMillis) {
                                    todayQty += cartItem.quantity
                                }
                                if (txMillis >= thirtyDaysAgoMillis) {
                                    thirtyDaysQty += cartItem.quantity
                                }
                            }
                        }
                    }
                }

                _state.update { current ->
                    current.copy(
                        terjualHariIni = todayQty,
                        total30HariTerakhir = thirtyDaysQty
                    )
                }
            }
        }
    }

    private fun loadCategoryName(categoryId: String) {
        viewModelScope.launch {
            getKategoriUseCase().collect { list ->
                val catName = list.find { it.id == categoryId }?.name ?: "Umum"
                _state.update { it.copy(categoryName = catName) }
            }
        }
    }

    fun onVisibilityToggle(isVisible: Boolean) {
        val currentProduk = _state.value.produk ?: return
        val updated = currentProduk.copy(isVisibleInCashier = isVisible)
        viewModelScope.launch {
            updateProdukUseCase(updated).collect { result ->
                if (result.isSuccess) {
                    _state.update { it.copy(produk = updated) }
                } else {
                    _state.update { it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Gagal memperbarui visibilitas") }
                }
            }
        }
    }

    fun requestDeleteDialog(show: Boolean) {
        _state.update { it.copy(showDeleteDialog = show) }
    }

    fun deleteProduk() {
        val currentProduk = _state.value.produk ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteDialog = false) }
            deleteProdukUseCase(currentProduk.id).collect { result ->
                if (result.isSuccess) {
                    _state.update { it.copy(isLoading = false, isDeleted = true) }
                } else {
                    _state.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Gagal menghapus produk") }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
