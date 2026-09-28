package com.ptpws.ikikasir.feature.promo.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.model.PromoProductItem
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "promos")
data class PromoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "nama")
    val nama: String,

    @ColumnInfo(name = "tipePromo")
    val tipePromo: String,

    @ColumnInfo(name = "itemsJson")
    val itemsJson: String = "[]", // JSON array of {productId, productName, price, imageUrl}

    @ColumnInfo(name = "diskonType")
    val diskonType: String = "Rp",

    @ColumnInfo(name = "nilaiDiskon")
    val nilaiDiskon: Double = 0.0,

    @ColumnInfo(name = "tanggalMulai")
    val tanggalMulai: String = "",

    @ColumnInfo(name = "tanggalBerakhir")
    val tanggalBerakhir: String = "",

    @ColumnInfo(name = "deskripsi")
    val deskripsi: String = "",

    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = true,

    @ColumnInfo(name = "isDeleted")
    val isDeleted: Boolean = false
) {
    fun toDomain(): Promo {
        val itemsList = mutableListOf<PromoProductItem>()
        try {
            val arr = JSONArray(itemsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                itemsList.add(
                    PromoProductItem(
                        productId = obj.optString("productId", ""),
                        productName = obj.optString("productName", ""),
                        price = obj.optDouble("price", 0.0),
                        imageUrl = obj.optString("imageUrl", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Promo(
            id = id,
            nama = nama,
            tipePromo = tipePromo,
            items = itemsList,
            diskonType = diskonType,
            nilaiDiskon = nilaiDiskon,
            tanggalMulai = tanggalMulai,
            tanggalBerakhir = tanggalBerakhir,
            deskripsi = deskripsi,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSynced = isSynced
        )
    }
}

fun Promo.toEntity(isSynced: Boolean = true, isDeleted: Boolean = false): PromoEntity {
    val arr = JSONArray()
    items.forEach { item ->
        arr.put(JSONObject().apply {
            put("productId", item.productId)
            put("productName", item.productName)
            put("price", item.price)
            put("imageUrl", item.imageUrl)
        })
    }
    return PromoEntity(
        id = id,
        nama = nama,
        tipePromo = tipePromo,
        itemsJson = arr.toString(),
        diskonType = diskonType,
        nilaiDiskon = nilaiDiskon,
        tanggalMulai = tanggalMulai,
        tanggalBerakhir = tanggalBerakhir,
        deskripsi = deskripsi,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}
