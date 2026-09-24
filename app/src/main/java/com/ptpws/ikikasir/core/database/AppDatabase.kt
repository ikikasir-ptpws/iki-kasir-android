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
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.UserEntity
import com.ptpws.ikikasir.feature.manajemenstok.data.local.dao.StokAdjustmentDao
import com.ptpws.ikikasir.feature.manajemenstok.data.local.entity.StockMovementEntity
import com.ptpws.ikikasir.feature.pengaturan.data.local.dao.NotaSettingDao
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.NotaSettingEntity
import com.ptpws.ikikasir.feature.penjualan.data.local.dao.TransactionDao
import com.ptpws.ikikasir.feature.penjualan.data.local.entity.TransactionEntity
import com.ptpws.ikikasir.feature.produk.data.local.dao.ProdukDao
import com.ptpws.ikikasir.feature.produk.data.local.entity.ProdukEntity
import com.ptpws.ikikasir.feature.role.data.local.dao.RoleDao
import com.ptpws.ikikasir.feature.role.data.local.entity.RoleEntity

import com.ptpws.ikikasir.feature.pengaturan.data.local.dao.TaxSettingDao
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.TaxSettingEntity

@Database(
    entities = [
        KategoriEntity::class,
        ProdukEntity::class,
        StockMovementEntity::class,
        TransactionEntity::class,
        AntreanEntity::class,
        QueueHistoryEntity::class,
        UserEntity::class,
        RoleEntity::class,
        NotaSettingEntity::class,
        TaxSettingEntity::class
    ],
    version = 21,
    exportSchema = false
)
@TypeConverters(TimestampConverter::class, MapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val kategoriDao: KategoriDao
    abstract val produkDao: ProdukDao
    abstract val stokAdjustmentDao: StokAdjustmentDao
    abstract val transactionDao: TransactionDao
    abstract val antreanDao: AntreanDao
    abstract val queueHistoryDao: QueueHistoryDao
    abstract val userDao: UserDao
    abstract val roleDao: RoleDao
    abstract val notaSettingDao: NotaSettingDao
    abstract val taxSettingDao: TaxSettingDao

    companion object {
        const val DATABASE_NAME = "ikikasir_db"
    }
}