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
import com.ptpws.ikikasir.core.storage.data.local.PendingImageDao
import com.ptpws.ikikasir.feature.catatankeuangan.data.local.dao.CatatanKeuanganDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private val MIGRATION_36_37 = object : Migration(36, 37) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pending_images (
                    id TEXT NOT NULL,
                    localPath TEXT NOT NULL,
                    storageFolder TEXT NOT NULL,
                    collection TEXT NOT NULL,
                    documentId TEXT NOT NULL,
                    firestoreField TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_38_39 = object : Migration(38, 39) {
        override fun migrate(database: SupportSQLiteDatabase) {
            repairCatatanKeuanganTable(database)
        }
    }

    private val MIGRATION_39_40 = object : Migration(39, 40) {
        override fun migrate(database: SupportSQLiteDatabase) {
            repairCatatanKeuanganTable(database)
        }
    }

    private val MIGRATION_40_41 = object : Migration(40, 41) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE `catatan_keuangan` RENAME TO `catatan_keuangan_with_category`")
            database.execSQL(
                """
                CREATE TABLE `catatan_keuangan` (
                    `id` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `amount` REAL NOT NULL,
                    `date` TEXT NOT NULL,
                    `note` TEXT NOT NULL,
                    `paymentMethod` TEXT NOT NULL,
                    `proofImageUrl` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `isSynced` INTEGER NOT NULL,
                    `isDeleted` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                INSERT INTO `catatan_keuangan` (
                    `id`, `title`, `type`, `amount`, `date`, `note`, `paymentMethod`,
                    `proofImageUrl`, `createdAt`, `updatedAt`, `isSynced`, `isDeleted`
                )
                SELECT
                    `id`, `title`, `type`, `amount`, `date`, `note`, `paymentMethod`,
                    `proofImageUrl`, `createdAt`, `updatedAt`, `isSynced`, `isDeleted`
                FROM `catatan_keuangan_with_category`
                """.trimIndent()
            )
            database.execSQL("DROP TABLE `catatan_keuangan_with_category`")
        }
    }

    private fun repairCatatanKeuanganTable(database: SupportSQLiteDatabase) {
        val expectedColumns = listOf(
            "id", "title", "type", "amount", "category", "date", "note",
            "paymentMethod", "proofImageUrl", "createdAt", "updatedAt", "isSynced", "isDeleted"
        )
        val existingColumns = mutableSetOf<String>()
        database.query("PRAGMA table_info(`catatan_keuangan`)").use { cursor ->
            while (cursor.moveToNext()) {
                existingColumns += cursor.getString(1)
            }
        }

        if (existingColumns.containsAll(expectedColumns) && existingColumns.size == expectedColumns.size) {
            return
        }

        if (existingColumns.isEmpty()) {
            createCatatanKeuanganTable(database)
            return
        }

        val repairableMissingColumns = setOf("paymentMethod", "proofImageUrl")
        val missingColumns = expectedColumns.filterNot(existingColumns::contains)
        check(missingColumns.all(repairableMissingColumns::contains)) {
            "Cannot safely repair catatan_keuangan table; missing columns: $missingColumns"
        }

        database.execSQL("ALTER TABLE `catatan_keuangan` RENAME TO `catatan_keuangan_legacy`")
        createCatatanKeuanganTable(database)

        val selectExpressions = expectedColumns.map { column ->
            when {
                column in existingColumns -> "`$column`"
                column == "paymentMethod" -> "'Tunai'"
                else -> "''"
            }
        }
        database.execSQL(
            """
            INSERT INTO `catatan_keuangan` (${expectedColumns.joinToString(", ") { "`$it`" }})
            SELECT ${selectExpressions.joinToString(", ")}
            FROM `catatan_keuangan_legacy`
            """.trimIndent()
        )
        database.execSQL("DROP TABLE `catatan_keuangan_legacy`")
    }

    private fun createCatatanKeuanganTable(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `catatan_keuangan` (
                `id` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `amount` REAL NOT NULL,
                `category` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `note` TEXT NOT NULL,
                `paymentMethod` TEXT NOT NULL,
                `proofImageUrl` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isSynced` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }

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
            .addMigrations(MIGRATION_36_37)
            .addMigrations(MIGRATION_38_39)
            .addMigrations(MIGRATION_39_40)
            .addMigrations(MIGRATION_40_41)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePendingImageDao(database: AppDatabase): PendingImageDao = database.pendingImageDao

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

    @Provides
    @Singleton
    fun provideCatatanKeuanganDao(
        database: AppDatabase
    ): CatatanKeuanganDao {
        return database.catatanKeuanganDao
    }
}