package com.ptpws.ikikasir.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ptpws.ikikasir.feature.kategori.data.local.dao.KategoriDao
import com.ptpws.ikikasir.feature.kategori.data.local.entity.KategoriEntity
import com.ptpws.ikikasir.feature.produk.data.local.dao.ProdukDao
import com.ptpws.ikikasir.feature.produk.data.local.entity.ProdukEntity

@Database(
    entities = [
        KategoriEntity::class,
        ProdukEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val kategoriDao: KategoriDao
    abstract val produkDao: ProdukDao

    companion object {
        const val DATABASE_NAME = "ikikasir_db"
    }
}