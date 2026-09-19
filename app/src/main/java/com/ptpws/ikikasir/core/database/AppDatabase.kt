package com.ptpws.ikikasir.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ptpws.ikikasir.feature.antrean.data.local.dao.AntreanDao
import com.ptpws.ikikasir.feature.antrean.data.local.dao.QueueHistoryDao
import com.ptpws.ikikasir.feature.antrean.data.local.entity.AntreanEntity
import com.ptpws.ikikasir.feature.antrean.data.local.entity.QueueHistoryEntity
import com.ptpws.ikikasir.feature.kategori.data.local.dao.KategoriDao
import com.ptpws.ikikasir.feature.kategori.data.local.entity.KategoriEntity
import com.ptpws.ikikasir.feature.manajemenstok.data.local.dao.StokAdjustmentDao
import com.ptpws.ikikasir.feature.manajemenstok.data.local.entity.StockMovementEntity
import com.ptpws.ikikasir.feature.penjualan.data.local.dao.TransactionDao
import com.ptpws.ikikasir.feature.penjualan.data.local.entity.TransactionEntity
import com.ptpws.ikikasir.feature.produk.data.local.dao.ProdukDao
import com.ptpws.ikikasir.feature.produk.data.local.entity.ProdukEntity

@Database(
    entities = [
        KategoriEntity::class,
        ProdukEntity::class,
        StockMovementEntity::class,
        TransactionEntity::class,
        AntreanEntity::class,
        QueueHistoryEntity::class
    ],
    version = 15,
    exportSchema = false
)
@TypeConverters(TimestampConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val kategoriDao: KategoriDao
    abstract val produkDao: ProdukDao
    abstract val stokAdjustmentDao: StokAdjustmentDao
    abstract val transactionDao: TransactionDao
    abstract val antreanDao: AntreanDao
    abstract val queueHistoryDao: QueueHistoryDao

    companion object {
        const val DATABASE_NAME = "ikikasir_db"
    }
}