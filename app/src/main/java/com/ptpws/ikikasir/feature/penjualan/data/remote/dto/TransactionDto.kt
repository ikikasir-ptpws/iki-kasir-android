package com.ptpws.ikikasir.feature.penjualan.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.ptpws.ikikasir.feature.penjualan.data.local.entity.TransactionEntity
import com.ptpws.ikikasir.feature.penjualan.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.produk.domain.model.Produk

/**
 * Firestore DTO untuk koleksi "transactions".
 * Field mapping sesuai schema:
 *  - transactionNumber: String
 *  - items: Array<Map>
 *  - subtotal: Number
 *  - discount: Number
 *  - total: Number
 *  - paymentMethod: String
 *  - paymentAmount: Number
 *  - change: Number
 *  - status: String
 *  - notes: String
 *  - createdBy: String
 *  - createdAt: Timestamp
 *  - updatedAt: Timestamp
 */
data class TransactionDto(
    @get:Exclude @set:Exclude
    var transactionId: String = "",

    @get:PropertyName("transactionNumber") @set:PropertyName("transactionNumber")
    var transactionNumber: String = "",

    @get:PropertyName("items") @set:PropertyName("items")
    var items: List<Map<String, Any>> = emptyList(),

    @get:PropertyName("subtotal") @set:PropertyName("subtotal")
    var subtotal: Double = 0.0,

    @get:PropertyName("discount") @set:PropertyName("discount")
    var discount: Double = 0.0,

    @get:PropertyName("total") @set:PropertyName("total")
    var total: Double = 0.0,

    @get:PropertyName("paymentMethod") @set:PropertyName("paymentMethod")
    var paymentMethod: String = "Tunai",

    @get:PropertyName("paymentAmount") @set:PropertyName("paymentAmount")
    var paymentAmount: Double = 0.0,

    @get:PropertyName("change") @set:PropertyName("change")
    var change: Double = 0.0,

    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = "COMPLETED",

    @get:PropertyName("notes") @set:PropertyName("notes")
    var notes: String = "",

    @get:PropertyName("createdBy") @set:PropertyName("createdBy")
    var createdBy: String = "",

    @get:PropertyName("customerName") @set:PropertyName("customerName")
    var customerName: String = "",

    @get:PropertyName("queueSequence") @set:PropertyName("queueSequence")
    var queueSequence: Int = 1,

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    @get:ServerTimestamp
    var createdAt: Timestamp? = null,

    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    @get:ServerTimestamp
    var updatedAt: Timestamp? = null
) {
    fun toEntity(): TransactionEntity {
        val cartItems = items.map { map ->
            val pId = map["productId"]?.toString() ?: ""
            val pName = map["productName"]?.toString() ?: ""
            val price = (map["price"] as? Number)?.toDouble() ?: 0.0
            val qty = (map["quantity"] as? Number)?.toInt() ?: 1
            val imgUrl = map["imageUrl"]?.toString() ?: map["image"]?.toString() ?: ""
            CartItem(
                produk = Produk(
                    id = pId,
                    name = pName,
                    sellingPrice = price,
                    imageUrl = imgUrl
                ),
                quantity = qty
            )
        }

        return PenjualanTransaksi(
            transactionId = transactionId,
            transactionNumber = transactionNumber,
            items = cartItems,
            subtotal = subtotal,
            discount = discount,
            total = total,
            paymentMethod = paymentMethod,
            paymentAmount = paymentAmount,
            change = change,
            status = status,
            notes = notes,
            createdBy = createdBy,
            customerName = customerName,
            queueSequence = queueSequence,
            createdAt = createdAt ?: Timestamp.now(),
            updatedAt = updatedAt ?: Timestamp.now(),
            isSynced = true
        ).toEntity(isSynced = true)
    }
}

fun PenjualanTransaksi.toDto(): TransactionDto {
    val itemsMapList = items.map { cartItem ->
        mapOf(
            "productId" to cartItem.produk.id,
            "productName" to cartItem.produk.name,
            "price" to cartItem.produk.sellingPrice,
            "quantity" to cartItem.quantity,
            "totalPrice" to cartItem.totalPrice,
            "imageUrl" to cartItem.produk.imageUrl
        )
    }

    return TransactionDto(
        transactionId = transactionId,
        transactionNumber = transactionNumber,
        items = itemsMapList,
        subtotal = subtotal,
        discount = discount,
        total = total,
        paymentMethod = paymentMethod,
        paymentAmount = paymentAmount,
        change = change,
        status = status,
        notes = notes,
        createdBy = createdBy,
        customerName = customerName,
        queueSequence = queueSequence,
        createdAt = null,
        updatedAt = null
    )
}
