package com.ptpws.ikikasir.feature.antrean.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.antrean.data.local.dao.AntreanDao
import com.ptpws.ikikasir.feature.antrean.data.local.dao.QueueHistoryDao
import com.ptpws.ikikasir.feature.antrean.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.antrean.data.remote.datasource.AntreanRemoteDataSource
import com.ptpws.ikikasir.feature.antrean.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.antrean.domain.model.Antrean
import com.ptpws.ikikasir.feature.antrean.domain.repository.AntreanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

import com.ptpws.ikikasir.feature.penjualan.data.local.dao.TransactionDao
import java.util.Calendar

private const val TAG = "AntreanRepository"

@Singleton
class AntreanRepositoryImpl @Inject constructor(
    private val localDao: AntreanDao,
    private val queueHistoryDao: QueueHistoryDao,
    private val transactionDao: TransactionDao,
    private val remoteDataSource: AntreanRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : AntreanRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Auto-sync saat koneksi kembali
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network restored, triggering auto-sync...")
                    try { syncInternal() } catch (e: Exception) {
                        Log.e(TAG, "Auto-sync error: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getAntreanList(): Flow<List<Antrean>> {
        // Refresh cache dari remote di background jika online
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllAntrean()
                    if (remoteList.isNotEmpty()) {
                        val validIds = remoteList.map { it.id }
                        val validTxIds = remoteList.map { it.transactionId }
                        localDao.deleteSyncedNotInRemote(validIds, validTxIds)
                        localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
                    } else {
                        localDao.deleteAllSynced()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch remote antrean: ${e.message}")
                }
            }
        }
        // Room sebagai Single Source of Truth
        return localDao.getAllAntreanFlow().map { entities ->
            entities.map { it.toDomain() }
                .distinctBy { if (it.transactionId.isNotBlank()) it.transactionId else it.id }
        }
    }

    override fun getAntreanById(id: String): Flow<Antrean?> {
        return localDao.getAntreanByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertAntrean(antrean: Antrean): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        Log.d(TAG, "insertAntrean: transactionId='${antrean.transactionId}', isOnline=$isOnline")

        if (isOnline) {
            try {
                remoteDataSource.saveAntrean(antrean.toDto())
                localDao.insertOrUpdate(antrean.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Firestore insert failed, saving offline: ${e.message}")
                localDao.insertOrUpdate(antrean.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: saving antrean locally")
            localDao.insertOrUpdate(antrean.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun updateAntreanStatus(id: String, status: String): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        val now = Timestamp.now()

        if (isOnline) {
            try {
                remoteDataSource.updateAntreanStatus(id, status, now)
                localDao.updateStatus(id, status)
                localDao.markAsSynced(id)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Firestore status update failed, saving offline: ${e.message}")
                localDao.updateStatus(id, status)
                emit(Result.success(Unit))
            }
        } else {
            localDao.updateStatus(id, status)
            emit(Result.success(Unit))
        }
    }

    override suspend fun deleteAntrean(id: String): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                remoteDataSource.deleteAntrean(id)
                localDao.deletePermanentlyByIdOrTxId(id)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Firestore delete failed, marking locally: ${e.message}")
                localDao.markAsDeleted(id)
                emit(Result.success(Unit))
            }
        } else {
            localDao.markAsDeleted(id)
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingAntrean(): Flow<Result<Unit>> = flow {
        try {
            if (!networkMonitor.isConnected()) {
                emit(Result.failure(Exception("Tidak ada koneksi internet")))
                return@flow
            }
            syncInternal()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    private suspend fun syncInternal() {
        val unsyncedItems = localDao.getUnsyncedAntrean()
        Log.d(TAG, "Found ${unsyncedItems.size} unsynced antrean items")

        for (item in unsyncedItems) {
            if (item.isDeleted) {
                try {
                    remoteDataSource.deleteAntrean(item.id)
                    localDao.deletePermanently(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync delete for ${item.id}: ${e.message}")
                }
            } else {
                try {
                    remoteDataSource.saveAntrean(item.toDomain().toDto())
                    localDao.markAsSynced(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync upsert for ${item.id}: ${e.message}")
                }
            }
        }

        // Pull latest dari Firestore setelah push
        try {
            val remoteList = remoteDataSource.getAllAntrean()
            if (remoteList.isNotEmpty()) {
                localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull remote antrean during sync: ${e.message}")
        }
    }

    override suspend fun getNextQueueSequence(): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDayMillis = cal.timeInMillis

        val maxQueuesToday = localDao.getMaxQueueSequenceToday(startOfDayMillis)
        val maxHistoryToday = queueHistoryDao.getMaxQueueSequenceToday(startOfDayMillis)
        val maxTxToday = transactionDao.getMaxQueueSequenceToday(startOfDayMillis)
        val todayMax = maxOf(maxQueuesToday, maxHistoryToday, maxTxToday)

        return todayMax + 1
    }
}
