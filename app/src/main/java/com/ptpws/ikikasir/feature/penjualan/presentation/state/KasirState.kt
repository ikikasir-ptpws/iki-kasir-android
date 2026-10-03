package com.ptpws.ikikasir.feature.penjualan.presentation.state

import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.getEffectiveSubtotal
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.model.isAvailableOn

data class KasirState(
    val searchQuery: String = "",
    val catalogSearchQuery: String = "",
    val selectedCategoryId: String? = null,
    val kategoriList: List<Kategori> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val produkKatalog: List<Produk> = emptyList(),
    val activePromos: List<Promo> = emptyList(),
    val selectedPromo: Promo? = null,
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

    val rawSubtotal: Double
        get() = if (taxSetting.isActive && taxSetting.percentage > 0 && taxSetting.type == TaxSetting.TAX_TYPE_INCLUSIVE) {
            cartItems.sumOf { it.getEffectiveSubtotal(taxSetting) }
        } else {
            cartItems.sumOf { it.totalPrice }
        }

    // Auto-detect applicable promo if none explicitly selected
    val effectivePromo: Promo?
        get() {
            if (selectedPromo != null) {
                return selectedPromo.takeIf { promo ->
                    promo.isAvailableOn() && activePromos.any { it.id == promo.id }
                }
            }
            if (cartItems.isEmpty() || activePromos.isEmpty()) return null
            val cartProductIds = cartItems.map { it.produk.id }.toSet()
            return activePromos.firstOrNull { promo ->
                promo.isAvailableOn() && (promo.items.isEmpty() || promo.items.any { it.productId in cartProductIds })
            }
        }

    val promoDiscountAmount: Double
        get() {
            val promo = effectivePromo ?: return 0.0
            if (cartItems.isEmpty()) return 0.0

            val eligibleSubtotal = if (promo.items.isEmpty()) {
                rawSubtotal
            } else {
                val promoProductIds = promo.items.map { it.productId }.toSet()
                if (taxSetting.isActive && taxSetting.percentage > 0 && taxSetting.type == TaxSetting.TAX_TYPE_INCLUSIVE) {
                    cartItems.filter { it.produk.id in promoProductIds }.sumOf { it.getEffectiveSubtotal(taxSetting) }
                } else {
                    cartItems.filter { it.produk.id in promoProductIds }.sumOf { it.totalPrice }
                }
            }

            if (eligibleSubtotal <= 0) return 0.0

            val discount = if (promo.diskonType.equals("%", ignoreCase = true)) {
                eligibleSubtotal * (promo.nilaiDiskon / 100.0)
            } else {
                promo.nilaiDiskon
            }

            return discount.coerceAtMost(rawSubtotal)
        }

    val subtotal: Double
        get() = (rawSubtotal - promoDiscountAmount).coerceAtLeast(0.0)

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
