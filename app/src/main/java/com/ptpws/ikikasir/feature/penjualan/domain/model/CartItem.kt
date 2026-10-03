package com.ptpws.ikikasir.feature.penjualan.domain.model

import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class CartItem(
    val produk: Produk,
    val quantity: Int = 1,
    val note: String = "",
    val customPrice: Double? = null
) {
    val notes: String get() = note

    val price: Double
        get() = customPrice ?: produk.sellingPrice

    val totalPrice: Double
        get() = price * quantity

    val name: String get() = produk.name
    val subtotal: Double get() = totalPrice
}

fun CartItem.getEffectivePrice(taxSetting: TaxSetting): Double {
    if (customPrice != null) return customPrice
    if (taxSetting.isActive && taxSetting.percentage > 0 && taxSetting.type == TaxSetting.TAX_TYPE_INCLUSIVE) {
        return kotlin.math.round(produk.sellingPrice * (1.0 + taxSetting.percentage / 100.0))
    }
    return produk.sellingPrice
}

fun CartItem.getEffectiveSubtotal(taxSetting: TaxSetting): Double {
    return getEffectivePrice(taxSetting) * quantity
}

fun Produk.getEffectivePrice(taxSetting: TaxSetting): Double {
    if (taxSetting.isActive && taxSetting.percentage > 0 && taxSetting.type == TaxSetting.TAX_TYPE_INCLUSIVE) {
        return kotlin.math.round(sellingPrice * (1.0 + taxSetting.percentage / 100.0))
    }
    return sellingPrice
}

