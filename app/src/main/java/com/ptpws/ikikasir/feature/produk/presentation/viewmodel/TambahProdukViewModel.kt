package com.ptpws.ikikasir.feature.produk.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.usecase.InsertProdukUseCase
import com.ptpws.ikikasir.feature.produk.domain.usecase.UpdateProdukUseCase
import com.ptpws.ikikasir.feature.produk.presentation.state.ProdukFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.ptpws.ikikasir.feature.auditlog.domain.usecase.LogActivityUseCase

@HiltViewModel
class TambahProdukViewModel @Inject constructor(
    private val insertProdukUseCase: InsertProdukUseCase,
    private val updateProdukUseCase: UpdateProdukUseCase,
    private val logActivityUseCase: LogActivityUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(ProdukFormState())
    val formState: StateFlow<ProdukFormState> = _formState.asStateFlow()

    init {
        val produkId = savedStateHandle.get<String>("produkId")
        if (!produkId.isNullOrBlank()) {
            val costPriceStr = savedStateHandle.get<String>("produkCostPrice") ?: ""
            val sellingPriceStr = savedStateHandle.get<String>("produkSellingPrice") 
                ?: savedStateHandle.get<String>("produkPrice") ?: ""

            _formState.update {
                it.copy(
                    id = produkId,
                    name = savedStateHandle.get<String>("produkName") ?: "",
                    costPrice = costPriceStr,
                    sellingPrice = sellingPriceStr,
                    stock = savedStateHandle.get<String>("produkStock") ?: "",
                    lowStockThreshold = savedStateHandle.get<String>("produkLowStockThreshold") ?: "5",
                    categoryId = savedStateHandle.get<String>("produkCategoryId") ?: "",
                    imageUrl = savedStateHandle.get<String>("produkImageUrl") ?: "",
                    discount = savedStateHandle.get<String>("produkDiscount") ?: "",
                    discountType = savedStateHandle.get<String>("produkDiscountType") ?: "PERCENT",
                    barcode = savedStateHandle.get<String>("produkBarcode") ?: "",
                    isVisibleInCashier = savedStateHandle.get<Boolean>("produkIsVisibleInCashier") ?: true,
                    isEditMode = true
                )
            }
        }
    }

    fun initFromProduk(produk: Produk) {
        _formState.update {
            it.copy(
                id = produk.id,
                name = produk.name,
                costPrice = produk.costPrice.toString(),
                sellingPrice = produk.sellingPrice.toString(),
                stock = produk.stock.toString(),
                lowStockThreshold = produk.lowStockThreshold.toString(),
                categoryId = produk.categoryId,
                imageUrl = produk.imageUrl,
                discount = produk.discount.toString(),
                discountType = produk.discountType,
                barcode = produk.barcode,
                isVisibleInCashier = produk.isVisibleInCashier,
                isEditMode = true
            )
        }
    }

    fun onNameChange(name: String) {
        _formState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onCostPriceChange(costPrice: String) {
        _formState.update { it.copy(costPrice = costPrice, errorMessage = null) }
    }

    fun onSellingPriceChange(sellingPrice: String) {
        _formState.update { it.copy(sellingPrice = sellingPrice, errorMessage = null) }
    }

    fun onPriceChange(price: String) {
        onSellingPriceChange(price)
    }

    fun onStockChange(stock: String) {
        _formState.update { it.copy(stock = stock) }
    }

    fun onLowStockThresholdChange(lowStockThreshold: String) {
        _formState.update { it.copy(lowStockThreshold = lowStockThreshold) }
    }

    fun onCategoryIdChange(categoryId: String) {
        _formState.update { it.copy(categoryId = categoryId) }
    }

    fun onImageUrlChange(imageUrl: String) {
        _formState.update { it.copy(imageUrl = imageUrl) }
    }

    fun onDiscountChange(discount: String) {
        _formState.update { it.copy(discount = discount) }
    }

    fun onDiscountTypeChange(discountType: String) {
        _formState.update { it.copy(discountType = discountType) }
    }

    fun onBarcodeChange(barcode: String) {
        _formState.update { it.copy(barcode = barcode) }
    }

    fun onVisibilityChange(isVisible: Boolean) {
        _formState.update { it.copy(isVisibleInCashier = isVisible) }
    }

    fun simpanProduk() {
        val currentState = _formState.value
        val nameTrimmed = currentState.name.trim()

        if (nameTrimmed.isBlank()) {
            val message = "Nama produk tidak boleh kosong"
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(errorMessage = message) }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMessage = null) }

            val produk = currentState.toProduk()

            val flow = if (currentState.isEditMode) {
                updateProdukUseCase(produk)
            } else {
                insertProdukUseCase(produk)
            }

            flow.collect { result ->
                if (result.isSuccess) {
                    val isEdit = currentState.isEditMode
                    GlobalCrudResultDialog.success(
                        if (isEdit) "Produk \"${produk.name}\" berhasil diperbarui."
                        else "Produk \"${produk.name}\" berhasil ditambahkan."
                    )
                    val actionTitle = if (isEdit) "Perubahan Harga: ${produk.name}" else "Produk Baru: ${produk.name}"
                    val actionDesc = if (isEdit) {
                        "Harga atau informasi produk ${produk.name} berhasil diperbarui."
                    } else {
                        "Produk baru ${produk.name} ditambahkan dengan harga Rp ${produk.sellingPrice}."
                    }
                    logActivityUseCase(
                        title = actionTitle,
                        description = actionDesc,
                        category = "PRICE",
                        action = if (isEdit) "UPDATE" else "CREATE",
                        isWarning = false
                    )

                    _formState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Gagal menyimpan produk"
                    GlobalCrudResultDialog.failure(message)
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = message
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