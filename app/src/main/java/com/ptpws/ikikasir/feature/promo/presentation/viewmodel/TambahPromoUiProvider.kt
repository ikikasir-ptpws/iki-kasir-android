package com.ptpws.ikikasir.feature.promo.presentation.viewmodel

import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak state & aksi untuk TambahPromoUiProvider.
 * Dipisah dari ViewModel supaya screen bisa di-preview tanpa Hilt/Firebase/Room.
 */
interface TambahPromoUiProvider {
    val state: StateFlow<TambahPromoFormState>
    fun loadPromoForEdit(promoId: String)
    fun refreshProdukCatalog()
    fun onNamaPromoChange(nama: String)
    fun onTipePromoChange(tipe: String)
    fun onDiskonTypeChange(type: String)
    fun onNilaiDiskonChange(nilai: String)
    fun onTanggalMulaiChange(tanggal: String)
    fun onTanggalBerakhirChange(tanggal: String)
    fun setSelectedProducts(products: List<Produk>)
    fun removeProduct(productId: String)
    fun simpanPromo()
    fun clearMessage()
}

/**
 * Adapter Hilt: meneruskan kontrak di atas ke TambahPromoViewModel asli.
 */
class HiltTambahPromoUiProvider(private val viewModel: TambahPromoViewModel) : TambahPromoUiProvider {
    override val state: StateFlow<TambahPromoFormState> get() = viewModel.state
    override fun loadPromoForEdit(promoId: String) = viewModel.loadPromoForEdit(promoId)
    override fun refreshProdukCatalog() = viewModel.refreshProdukCatalog()
    override fun onNamaPromoChange(nama: String) = viewModel.onNamaPromoChange(nama)
    override fun onTipePromoChange(tipe: String) = viewModel.onTipePromoChange(tipe)
    override fun onDiskonTypeChange(type: String) = viewModel.onDiskonTypeChange(type)
    override fun onNilaiDiskonChange(nilai: String) = viewModel.onNilaiDiskonChange(nilai)
    override fun onTanggalMulaiChange(tanggal: String) = viewModel.onTanggalMulaiChange(tanggal)
    override fun onTanggalBerakhirChange(tanggal: String) = viewModel.onTanggalBerakhirChange(tanggal)
    override fun setSelectedProducts(products: List<Produk>) = viewModel.setSelectedProducts(products)
    override fun removeProduct(productId: String) = viewModel.removeProduct(productId)
    override fun simpanPromo() = viewModel.simpanPromo()
    override fun clearMessage() = viewModel.clearMessage()
}
