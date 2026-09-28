package com.ptpws.ikikasir.feature.promo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.usecase.GetProdukUseCase
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.model.PromoProductItem
import com.ptpws.ikikasir.feature.promo.domain.usecase.InsertPromoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TambahPromoFormState(
    val namaPromo: String = "",
    val tipePromo: String = "",
    val selectedProducts: List<Produk> = emptyList(),
    val availableProducts: List<Produk> = emptyList(),
    val diskonType: String = "Rp", // "Rp" or "%"
    val nilaiDiskon: String = "",
    val tanggalMulai: String = "",
    val tanggalBerakhir: String = "",
    val deskripsiPromo: String = "",
    val isLoading: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TambahPromoViewModel @Inject constructor(
    private val getProdukUseCase: GetProdukUseCase,
    private val insertPromoUseCase: InsertPromoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TambahPromoFormState())
    val state: StateFlow<TambahPromoFormState> = _state.asStateFlow()

    init {
        loadProdukCatalog()
    }

    private fun loadProdukCatalog() {
        viewModelScope.launch {
            getProdukUseCase().collect { products ->
                _state.update { currentState ->
                    currentState.copy(availableProducts = products)
                }
            }
        }
    }

    fun onNamaPromoChange(nama: String) {
        _state.update { it.copy(namaPromo = nama) }
    }

    fun onTipePromoChange(tipe: String) {
        _state.update { it.copy(tipePromo = tipe) }
    }

    fun onDiskonTypeChange(type: String) {
        _state.update { it.copy(diskonType = type) }
    }

    fun onNilaiDiskonChange(nilai: String) {
        _state.update { it.copy(nilaiDiskon = nilai) }
    }

    fun onTanggalMulaiChange(tanggal: String) {
        _state.update { it.copy(tanggalMulai = tanggal) }
    }

    fun onTanggalBerakhirChange(tanggal: String) {
        _state.update { it.copy(tanggalBerakhir = tanggal) }
    }

    fun onDeskripsiChange(deskripsi: String) {
        _state.update { it.copy(deskripsiPromo = deskripsi) }
    }

    fun setSelectedProducts(products: List<Produk>) {
        _state.update { it.copy(selectedProducts = products) }
    }

    fun removeProduct(productId: String) {
        _state.update { currentState ->
            currentState.copy(selectedProducts = currentState.selectedProducts.filter { it.id != productId })
        }
    }

    fun simpanPromo() {
        val form = _state.value
        if (form.namaPromo.isBlank()) {
            _state.update { it.copy(errorMessage = "Nama promo tidak boleh kosong") }
            return
        }

        val rawDiskon = form.nilaiDiskon.replace(".", "").replace(",", "").toDoubleOrNull() ?: 0.0

        val promoItems = form.selectedProducts.map { produk ->
            PromoProductItem(
                productId = produk.id,
                productName = produk.name,
                price = produk.sellingPrice,
                imageUrl = produk.imageUrl
            )
        }

        val newPromo = Promo(
            id = UUID.randomUUID().toString(),
            nama = form.namaPromo,
            tipePromo = form.tipePromo,
            items = promoItems,
            diskonType = form.diskonType,
            nilaiDiskon = rawDiskon,
            tanggalMulai = form.tanggalMulai,
            tanggalBerakhir = form.tanggalBerakhir,
            deskripsi = form.deskripsiPromo,
            isActive = true,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            isSynced = false
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            insertPromoUseCase(newPromo).collect { result ->
                result.onSuccess {
                    _state.update { it.copy(isLoading = false, isSavedSuccess = true, userMessage = "Promo berhasil disimpan!") }
                }.onFailure { err ->
                    _state.update { it.copy(isLoading = false, errorMessage = "Gagal menyimpan promo: ${err.message}") }
                }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(userMessage = null, errorMessage = null) }
    }
}
