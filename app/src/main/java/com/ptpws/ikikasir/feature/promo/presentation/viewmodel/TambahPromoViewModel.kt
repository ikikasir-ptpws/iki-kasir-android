package com.ptpws.ikikasir.feature.promo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.usecase.GetProdukUseCase
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.model.PromoProductItem
import com.ptpws.ikikasir.feature.promo.domain.usecase.GetPromoByIdUseCase
import com.ptpws.ikikasir.feature.promo.domain.usecase.InsertPromoUseCase
import com.ptpws.ikikasir.feature.promo.domain.usecase.UpdatePromoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TambahPromoFormState(
    val promoId: String? = null,
    val isEditMode: Boolean = false,
    val namaPromo: String = "",
    val tipePromo: String = "",
    val selectedProducts: List<Produk> = emptyList(),
    val availableProducts: List<Produk> = emptyList(),
    val diskonType: String = "Rp", // "Rp" or "%"
    val nilaiDiskon: String = "",
    val tanggalMulai: String = "",
    val tanggalBerakhir: String = "",
    val isLoading: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TambahPromoViewModel @Inject constructor(
    private val getProdukUseCase: GetProdukUseCase,
    private val insertPromoUseCase: InsertPromoUseCase,
    private val updatePromoUseCase: UpdatePromoUseCase,
    private val getPromoByIdUseCase: GetPromoByIdUseCase,
    private val logActivityUseCase: LogActivityUseCase
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

    fun loadPromoForEdit(promoId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val existingPromo = getPromoByIdUseCase(promoId).firstOrNull()
            if (existingPromo != null) {
                val availableProds = _state.value.availableProducts.ifEmpty {
                    getProdukUseCase().firstOrNull() ?: emptyList()
                }

                // Map promo.items back to Produk objects from catalog if available, or create transient Produk
                val matchedProducts = existingPromo.items.map { item ->
                    availableProds.find { it.id == item.productId } ?: Produk(
                        id = item.productId,
                        name = item.productName,
                        sellingPrice = item.price,
                        imageUrl = item.imageUrl
                    )
                }

                val formattedDiskon = if (existingPromo.discountValue % 1.0 == 0.0) {
                    existingPromo.discountValue.toLong().toString()
                } else {
                    existingPromo.discountValue.toString()
                }

                _state.update {
                    it.copy(
                        promoId = existingPromo.id,
                        isEditMode = true,
                        namaPromo = existingPromo.name,
                        tipePromo = existingPromo.promoType,
                        selectedProducts = matchedProducts,
                        diskonType = existingPromo.discountType,
                        nilaiDiskon = formattedDiskon,
                        tanggalMulai = existingPromo.startDate,
                        tanggalBerakhir = existingPromo.endDate,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
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

        val isEdit = form.isEditMode && !form.promoId.isNullOrBlank()
        val promoId = form.promoId ?: UUID.randomUUID().toString()

        val targetPromo = Promo(
            id = promoId,
            name = form.namaPromo,
            promoType = form.tipePromo,
            items = promoItems,
            discountType = form.diskonType,
            discountValue = rawDiskon,
            startDate = form.tanggalMulai,
            endDate = form.tanggalBerakhir,
            isActive = true,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            isSynced = false
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val flow = if (isEdit) updatePromoUseCase(targetPromo) else insertPromoUseCase(targetPromo)
            flow.collect { result ->
                result.onSuccess {
                    val msg = if (isEdit) "Promo berhasil diperbarui!" else "Promo berhasil disimpan!"
                    val actionName = if (isEdit) "UPDATE" else "CREATE"
                    val actionTitle = if (isEdit) "Perubahan Promo: ${form.namaPromo}" else "Promo Baru: ${form.namaPromo}"
                    logActivityUseCase(
                        title = actionTitle,
                        description = "Promo ${form.namaPromo} (${form.diskonType} ${form.nilaiDiskon}) telah tersimpan.",
                        category = "PROMO",
                        action = actionName,
                        isWarning = false
                    )
                    _state.update { it.copy(isLoading = false, isSavedSuccess = true, userMessage = msg) }
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
