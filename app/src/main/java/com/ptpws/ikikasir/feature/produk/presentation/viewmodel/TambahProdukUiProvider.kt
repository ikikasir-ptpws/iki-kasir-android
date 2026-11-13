package com.ptpws.ikikasir.feature.produk.presentation.viewmodel

import com.ptpws.ikikasir.feature.produk.presentation.state.ProdukFormState
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk TambahProdukUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface TambahProdukUiProvider {
    val formState: StateFlow<ProdukFormState>
    fun onNameChange(name: String)
    fun onCostPriceChange(costPrice: String)
    fun onSellingPriceChange(sellingPrice: String)
    fun onStockChange(stock: String)
    fun onLowStockThresholdChange(lowStockThreshold: String)
    fun onCategoryIdChange(categoryId: String)
    fun onImageUrlChange(imageUrl: String)
    fun onBarcodeChange(barcode: String)
    fun onVisibilityChange(isVisible: Boolean)
    fun simpanProduk()
    fun resetSuccess()
    fun cetakBarcode(productName: String, barcode: String, onSuccess: () -> Unit = {})
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke TambahProdukViewModel asli.
 */
class HiltTambahProdukUiProvider(private val viewModel: TambahProdukViewModel) : TambahProdukUiProvider {
    override val formState: StateFlow<ProdukFormState> get() = viewModel.formState
    override fun onNameChange(name: String) = viewModel.onNameChange(name)
    override fun onCostPriceChange(costPrice: String) = viewModel.onCostPriceChange(costPrice)
    override fun onSellingPriceChange(sellingPrice: String) = viewModel.onSellingPriceChange(sellingPrice)
    override fun onStockChange(stock: String) = viewModel.onStockChange(stock)
    override fun onLowStockThresholdChange(lowStockThreshold: String) = viewModel.onLowStockThresholdChange(lowStockThreshold)
    override fun onCategoryIdChange(categoryId: String) = viewModel.onCategoryIdChange(categoryId)
    override fun onImageUrlChange(imageUrl: String) = viewModel.onImageUrlChange(imageUrl)
    override fun onBarcodeChange(barcode: String) = viewModel.onBarcodeChange(barcode)
    override fun onVisibilityChange(isVisible: Boolean) = viewModel.onVisibilityChange(isVisible)
    override fun simpanProduk() = viewModel.simpanProduk()
    override fun resetSuccess() = viewModel.resetSuccess()
    override fun cetakBarcode(productName: String, barcode: String, onSuccess: () -> Unit) = viewModel.cetakBarcode(productName, barcode, onSuccess)
}
