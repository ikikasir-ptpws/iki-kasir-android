package com.ptpws.ikikasir.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ptpws.ikikasir.feature.kategori.data.local.dao.KategoriDao
import com.ptpws.ikikasir.feature.kategori.data.local.entity.KategoriEntity

@Database(
    entities = [
        KategoriEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val kategoriDao: KategoriDao

    companion object {
        const val DATABASE_NAME = "ikikasir_db"
    }
}
