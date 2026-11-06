package com.ptpws.ikikasir.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ptpws.ikikasir.core.database.AppDatabase
import com.ptpws.ikikasir.feature.antrean.data.local.dao.AntreanDao
import com.ptpws.ikikasir.feature.antrean.data.local.dao.QueueHistoryDao
import com.ptpws.ikikasir.feature.kategori.data.local.dao.KategoriDao
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.manajemenstok.data.local.dao.StokAdjustmentDao
import com.ptpws.ikikasir.feature.penjualan.data.local.dao.TransactionDao
import com.ptpws.ikikasir.feature.produk.data.local.dao.ProdukDao
import com.ptpws.ikikasir.feature.role.data.local.dao.RoleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val MIGRATION_37_38 = object : Migration(37, 38) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE nota_settings ADD COLUMN storePhone TEXT NOT NULL DEFAULT ''"
        )
    }
}

private val MIGRATION_38_39 = object : Migration(38, 39) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE categories_new (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                iconUrl TEXT NOT NULL,
                isVisibleInCashier INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isSynced INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO categories_new (
                id, name, description, iconUrl, isVisibleInCashier,
                createdAt, updatedAt, isSynced, isDeleted
            )
            SELECT
                id, name, description, iconUrl, isVisibleInCashier,
                createdAt, updatedAt, isSynced, isDeleted
            FROM categories
            """.trimIndent()
        )
        database.execSQL("DROP TABLE categories")
        database.execSQL("ALTER TABLE categories_new RENAME TO categories")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_37_38, MIGRATION_38_39)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideKategoriDao(
        database: AppDatabase
    ): KategoriDao {
        return database.kategoriDao
    }

    @Provides
    @Singleton
    fun provideProdukDao(
        database: AppDatabase
    ): ProdukDao {
        return database.produkDao
    }

    @Provides
    @Singleton
    fun provideStokAdjustmentDao(
        database: AppDatabase
    ): StokAdjustmentDao {
        return database.stokAdjustmentDao
    }

    @Provides
    @Singleton
    fun provideTransactionDao(
        database: AppDatabase
    ): TransactionDao {
        return database.transactionDao
    }

    @Provides
    @Singleton
    fun provideAntreanDao(
        database: AppDatabase
    ): AntreanDao {
        return database.antreanDao
    }

    @Provides
    @Singleton
    fun provideQueueHistoryDao(
        database: AppDatabase
    ): QueueHistoryDao {
        return database.queueHistoryDao
    }

    @Provides
    @Singleton
    fun provideUserDao(
        database: AppDatabase
    ): UserDao {
        return database.userDao
    }

    @Provides
    @Singleton
    fun provideRoleDao(
        database: AppDatabase
    ): RoleDao {
        return database.roleDao
    }

    @Provides
    @Singleton
    fun providePromoDao(
        database: AppDatabase
    ): com.ptpws.ikikasir.feature.promo.data.local.dao.PromoDao {
        return database.promoDao
    }

    @Provides
    @Singleton
    fun provideAuditLogDao(
        database: AppDatabase
    ): com.ptpws.ikikasir.feature.auditlog.data.local.dao.AuditLogDao {
        return database.auditLogDao
    }
}