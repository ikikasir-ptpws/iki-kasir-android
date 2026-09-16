package com.ptpws.ikikasir.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ptpws.ikikasir.feature.kategori.data.local.dao.KategoriDao
import com.ptpws.ikikasir.feature.kategori.data.local.entity.KategoriEntity
import com.ptpws.ikikasir.feature.manajemenstok.data.local.dao.StokAdjustmentDao
import com.ptpws.ikikasir.feature.manajemenstok.data.local.entity.StockMovementEntity
import com.ptpws.ikikasir.feature.produk.data.local.dao.ProdukDao
import com.ptpws.ikikasir.feature.produk.data.local.entity.ProdukEntity

@Database(
    entities = [
        KategoriEntity::class,
        ProdukEntity::class,
        StockMovementEntity::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(TimestampConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val kategoriDao: KategoriDao
    abstract val produkDao: ProdukDao
    abstract val stokAdjustmentDao: StokAdjustmentDao

    companion object {
        const val DATABASE_NAME = "ikikasir_db"
    }
}