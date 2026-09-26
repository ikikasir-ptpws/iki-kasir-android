package com.ptpws.ikikasir.feature.penjualan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.penjualan.domain.model.CartItem
import com.ptpws.ikikasir.feature.penjualan.domain.model.PenjualanTransaksi
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "transactionId")
    val transactionId: String,

    @ColumnInfo(name = "transactionNumber")
    val transactionNumber: String,

    @ColumnInfo(name = "itemsJson")
    val itemsJson: String,

    @ColumnInfo(name = "subtotal")
    val subtotal: Double,

    @ColumnInfo(name = "ppnAmount")
    val ppnAmount: Double = 0.0,

    @ColumnInfo(name = "discount")
    val discount: Double,

    @ColumnInfo(name = "total")
    val total: Double,

    @ColumnInfo(name = "paymentMethod")
    val paymentMethod: String,

    @ColumnInfo(name = "paymentAmount")
    val paymentAmount: Double,

    @ColumnInfo(name = "change")
    val change: Double,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "createdBy")
    val createdBy: String = "",

    @ColumnInfo(name = "customerName")
    val customerName: String = "",

    @ColumnInfo(name = "tableNumber")
    val tableNumber: String = "",

    @ColumnInfo(name = "queueSequence")
    val queueSequence: Int = 1,

    @ColumnInfo(name = "createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false
) {
    fun toDomain(): PenjualanTransaksi {
        val cartItems = mutableListOf<CartItem>()
        try {
            val jsonArray = JSONArray(itemsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val pId = obj.optString("productId", "")
                val pName = obj.optString("productName", "")
                val price = obj.optDouble("price", 0.0)
                val qty = obj.optInt("quantity", 1)
                val imgUrl = obj.optString("imageUrl", "")
                cartItems.add(
                    CartItem(
                        produk = Produk(
                            id = pId,
                            name = pName,
                            sellingPrice = price,
                            imageUrl = imgUrl
                        ),
                        quantity = qty
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return PenjualanTransaksi(
            transactionId = transactionId,
            transactionNumber = transactionNumber,
            items = cartItems,
            subtotal = subtotal,
            ppnAmount = ppnAmount,
            discount = discount,
            total = total,
            paymentMethod = paymentMethod,
            paymentAmount = paymentAmount,
            change = change,
            status = status,
            notes = notes,
            createdBy = createdBy,
            customerName = customerName,
            tableNumber = tableNumber,
            queueSequence = queueSequence,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }
}

fun PenjualanTransaksi.toEntity(isSynced: Boolean = false): TransactionEntity {
    val jsonArray = JSONArray()
    items.forEach { cartItem ->
        val obj = JSONObject().apply {
            put("productId", cartItem.produk.id)
            put("productName", cartItem.produk.name)
            put("price", cartItem.produk.sellingPrice)
            put("quantity", cartItem.quantity)
            put("totalPrice", cartItem.totalPrice)
            put("imageUrl", cartItem.produk.imageUrl)
        }
        jsonArray.put(obj)
    }

    return TransactionEntity(
        transactionId = transactionId,
        transactionNumber = transactionNumber,
        itemsJson = jsonArray.toString(),
        subtotal = subtotal,
        ppnAmount = ppnAmount,
        discount = discount,
        total = total,
        paymentMethod = paymentMethod,
        paymentAmount = paymentAmount,
        change = change,
        status = status,
        notes = notes,
        createdBy = createdBy,
        customerName = customerName,
        tableNumber = tableNumber,
        queueSequence = queueSequence,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )
}
