package com.ptpws.ikikasir.feature.penjualan.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ptpws.ikikasir.feature.penjualan.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId LIMIT 1")
    fun getTransactionByIdFlow(transactionId: String): Flow<TransactionEntity?>

    @Upsert
    suspend fun insertOrUpdate(transaction: TransactionEntity)

    @Upsert
    suspend fun insertOrUpdateAll(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET isSynced = 1 WHERE transactionId = :transactionId")
    suspend fun markAsSynced(transactionId: String)

    @Query("SELECT COALESCE(MAX(queueSequence), 0) FROM transactions")
    suspend fun getMaxQueueSequence(): Int

    @Query("SELECT COALESCE(MAX(queueSequence), 0) FROM transactions WHERE createdAt >= :startOfDayMillis")
    suspend fun getMaxQueueSequenceToday(startOfDayMillis: Long): Int

    @Query("SELECT MAX(createdAt) FROM transactions")
    suspend fun getLatestTransactionTimestamp(): Long?
}
