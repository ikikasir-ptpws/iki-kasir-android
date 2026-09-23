package com.ptpws.ikikasir.feature.penjualan.domain.model

import com.ptpws.ikikasir.feature.produk.domain.model.Produk

data class CartItem(
    val produk: Produk,
    val quantity: Int = 1,
    val note: String = ""
) {
    val notes: String get() = note

    val totalPrice: Double
        get() = produk.price * quantity

    val name: String get() = produk.name
    val price: Double get() = produk.price
    val subtotal: Double get() = totalPrice
}
