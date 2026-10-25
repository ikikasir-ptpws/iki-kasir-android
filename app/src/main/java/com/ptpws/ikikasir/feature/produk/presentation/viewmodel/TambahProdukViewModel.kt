package com.ptpws.ikikasir.feature.produk.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptpws.ikikasir.commond.GlobalCrudResultDialog
import com.ptpws.ikikasir.commond.GlobalUploadProgress
import com.ptpws.ikikasir.core.storage.domain.usecase.DeleteStorageImageUseCase
import com.ptpws.ikikasir.core.storage.domain.usecase.UploadProductImageUseCase
import com.ptpws.ikikasir.core.storage.domain.usecase.QueuePendingImageUseCase
import com.ptpws.ikikasir.core.storage.domain.model.PendingImageTarget
import com.ptpws.ikikasir.core.storage.domain.model.isLocalImageUri
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.usecase.InsertProdukUseCase
import com.ptpws.ikikasir.feature.produk.domain.usecase.PrintProductBarcodeUseCase
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
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.domain.usecase.GetKategoriUseCase

@HiltViewModel
class TambahProdukViewModel @Inject constructor(
    private val insertProdukUseCase: InsertProdukUseCase,
    private val updateProdukUseCase: UpdateProdukUseCase,
    private val printProductBarcodeUseCase: PrintProductBarcodeUseCase,
    private val logActivityUseCase: LogActivityUseCase,
    private val getKategoriUseCase: GetKategoriUseCase,
    private val uploadProductImageUseCase: UploadProductImageUseCase,
    private val deleteStorageImageUseCase: DeleteStorageImageUseCase,
    private val queuePendingImageUseCase: QueuePendingImageUseCase,
    private val networkMonitor: NetworkMonitor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(ProdukFormState())
    val formState: StateFlow<ProdukFormState> = _formState.asStateFlow()

    // Menyimpan URI lokal sementara sebelum diupload ke Storage
    private var pendingImageUri: Uri? = null
    // URL gambar lama untuk dihapus dari Storage jika ganti gambar (saat edit)
    private var oldImageUrl: String = ""

    private var registeredCategories: List<Kategori> = emptyList()

    init {
        viewModelScope.launch {
            getKategoriUseCase().collect { list ->
                registeredCategories = list
                val currentCat = _formState.value.categoryId
                if (currentCat.isNotBlank()) {
                    val match = list.find { 
                        it.id.equals(currentCat, ignoreCase = true) || 
                        it.name.equals(currentCat, ignoreCase = true) 
                    }
                    if (match != null && match.id != currentCat) {
                        _formState.update { it.copy(categoryId = match.id) }
                    }
                }
            }
        }

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
            oldImageUrl = savedStateHandle.get<String>("produkImageUrl").orEmpty()
        }
    }

    fun initFromProduk(produk: Produk) {
        // Simpan URL gambar lama untuk keperluan hapus dari Storage jika diganti
        oldImageUrl = produk.imageUrl
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

    /**
     * Dipanggil dari UI ketika user memilih gambar dari kamera/galeri.
     * Menyimpan URI lokal sementara; upload ke Firebase Storage dilakukan saat simpan.
     */
    fun onLocalImageSelected(uri: Uri) {
        pendingImageUri = uri
        // Tampilkan preview menggunakan URI lokal sementara
        _formState.update { it.copy(imageUrl = uri.toString()) }
    }

    /**
     * Upload gambar ke Firebase Storage.
     * Mengembalikan download URL atau null jika gagal.
     * Dipanggil secara internal dari simpanProduk().
     */
    private suspend fun uploadImageIfNeeded(
        productId: String,
        onProgress: ((Float) -> Unit)? = null
    ): Result<String>? {
        val uri = pendingImageUri ?: return null
        return uploadProductImageUseCase(
            localFilePathOrUri = uri.toString(),
            productId = productId,
            onProgress = onProgress
        )
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

    fun cetakBarcode(productName: String, barcode: String) {
        viewModelScope.launch {
            try {
                printProductBarcodeUseCase(productName, barcode)
                GlobalCrudResultDialog.success("Barcode dan QR berhasil dikirim ke printer.")
            } catch (error: Exception) {
                GlobalCrudResultDialog.failure(
                    error.message ?: "Gagal mencetak barcode. Periksa koneksi printer Bluetooth."
                )
            }
        }
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

        if (currentState.categoryId.isBlank()) {
            val message = "Kategori produk wajib dipilih dari kategori yang terdaftar"
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(errorMessage = message) }
            return
        }

        val matchedCategory = registeredCategories.find { 
            it.id.equals(currentState.categoryId, ignoreCase = true) || 
            it.name.equals(currentState.categoryId, ignoreCase = true) 
        }

        if (matchedCategory == null && registeredCategories.isNotEmpty()) {
            val message = "Kategori tidak valid. Silakan pilih kategori yang terdaftar."
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(errorMessage = message) }
            return
        }

        val sellingPriceVal = currentState.sellingPrice.toDoubleOrNull() ?: 0.0
        if (sellingPriceVal <= 0.0) {
            val message = "Harga jual harus lebih dari Rp 0"
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(errorMessage = message) }
            return
        }

        val stockVal = currentState.stock.toIntOrNull() ?: 0
        if (stockVal < 0) {
            val message = "Stok produk tidak boleh bernilai negatif"
            GlobalCrudResultDialog.failure(message)
            _formState.update { it.copy(errorMessage = message) }
            return
        }

        val finalCategoryId = matchedCategory?.id ?: currentState.categoryId

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMessage = null) }

            val baseProduk = currentState.toProduk().copy(categoryId = finalCategoryId)

            // Upload gambar ke Firebase Storage jika ada gambar baru yang dipilih
            var finalImageUrl = currentState.imageUrl
            val localImageUri = pendingImageUri
            if (localImageUri != null) {
                if (networkMonitor.isConnected()) {
                    GlobalUploadProgress.showProductImageUpload(progress = 0f)
                    val uploadResult = uploadImageIfNeeded(
                        productId = baseProduk.id,
                        onProgress = { progress ->
                            GlobalUploadProgress.updateProgress(progress, "Mengunggah foto produk...")
                        }
                    )
                    GlobalUploadProgress.dismiss()
                    val downloadUrl = uploadResult?.getOrNull()
                    if (downloadUrl == null) {
                        val error = uploadResult?.exceptionOrNull()?.message ?: "Upload foto produk gagal."
                        GlobalCrudResultDialog.failure(error)
                        _formState.update { it.copy(isLoading = false, errorMessage = error) }
                        return@launch
                    }
                    finalImageUrl = downloadUrl
                } else {
                    queuePendingImageUseCase(
                        localFilePathOrUri = localImageUri.toString(),
                        target = PendingImageTarget.PRODUCT,
                        documentId = baseProduk.id
                    )
                    finalImageUrl = localImageUri.toString()
                }
            }

            val produk = baseProduk.copy(imageUrl = finalImageUrl)

            val flow = if (currentState.isEditMode) {
                updateProdukUseCase(produk)
            } else {
                insertProdukUseCase(produk)
            }

            flow.collect { result ->
                if (result.isSuccess) {
                    pendingImageUri = null
                    if (oldImageUrl.isNotBlank() && oldImageUrl != produk.imageUrl &&
                        !isLocalImageUri(produk.imageUrl)
                    ) {
                        deleteStorageImageUseCase(oldImageUrl)
                    }
                    oldImageUrl = produk.imageUrl
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
                    if (pendingImageUri != null && finalImageUrl != currentState.imageUrl) {
                        deleteStorageImageUseCase(finalImageUrl)
                    }
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