package com.ptpws.ikikasir.feature.penjualan.presentation.state

import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class KasirState(
    val searchQuery: String = "",
    val catalogSearchQuery: String = "",
    val selectedCategoryId: String? = null,
    val kategoriList: List<Kategori> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val produkKatalog: List<Produk> = emptyList(),
    val showProductCatalogDialog: Boolean = false,
    val orderNote: String = "",
    val showOrderNoteDialog: Boolean = false,
    val taxSetting: TaxSetting = TaxSetting(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
) {
    val totalItemCount: Int
        get() = cartItems.sumOf { it.quantity }

    val subtotal: Double
        get() = cartItems.sumOf { it.totalPrice }

    val ppnAmount: Double
        get() {
            if (!taxSetting.isActive || taxSetting.percentage <= 0) return 0.0
            return if (taxSetting.type == TaxSetting.TAX_TYPE_EXCLUSIVE) {
                subtotal * (taxSetting.percentage / 100.0)
            } else {
                subtotal - (subtotal / (1 + taxSetting.percentage / 100.0))
            }
        }

    val grandTotal: Double
        get() {
            if (taxSetting.isActive && taxSetting.percentage > 0 && taxSetting.type == TaxSetting.TAX_TYPE_EXCLUSIVE) {
                return subtotal + ppnAmount
            }
            return subtotal
        }

    val ppnLabel: String
        get() {
            if (!taxSetting.isActive || taxSetting.percentage <= 0) {
                return "Tanpa PPN"
            }
            val formattedPercent = if (taxSetting.percentage % 1.0 == 0.0) "${taxSetting.percentage.toInt()}%" else "${taxSetting.percentage}%"
            return if (taxSetting.type == TaxSetting.TAX_TYPE_EXCLUSIVE) {
                "Belum Termasuk PPN $formattedPercent"
            } else {
                "Termasuk PPN $formattedPercent"
            }
        }

    fun getItemQuantity(produkId: String): Int {
        return cartItems.find { it.produk.id == produkId }?.quantity ?: 0
    }
}
