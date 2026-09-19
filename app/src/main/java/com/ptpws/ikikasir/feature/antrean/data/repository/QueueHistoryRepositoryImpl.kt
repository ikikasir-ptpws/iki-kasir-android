package com.ptpws.ikikasir.feature.antrean.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.antrean.data.local.dao.QueueHistoryDao
import com.ptpws.ikikasir.feature.antrean.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.antrean.data.remote.datasource.QueueHistoryRemoteDataSource
import com.ptpws.ikikasir.feature.antrean.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.antrean.domain.model.QueueHistory
import com.ptpws.ikikasir.feature.antrean.domain.repository.QueueHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "QueueHistoryRepo"

@Singleton
class QueueHistoryRepositoryImpl @Inject constructor(
    private val localDao: QueueHistoryDao,
    private val remoteDataSource: QueueHistoryRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : QueueHistoryRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Auto-sync pending history entries when network is restored
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network online, auto-syncing queueHistory...")
                    try { syncInternal() } catch (e: Exception) {
                        Log.e(TAG, "Error auto-syncing queueHistory: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getHistoryList(): Flow<List<QueueHistory>> {
        // Fetch remote cache if online
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllHistory()
                    if (remoteList.isNotEmpty()) {
                        localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch remote queueHistory: ${e.message}")
                }
            }
        }

        // Room is Single Source of Truth
        return localDao.getAllHistoryFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHistoryById(id: String): Flow<QueueHistory?> {
        return localDao.getHistoryByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertHistory(history: QueueHistory): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        Log.d(TAG, "insertHistory: transactionId='${history.transactionId}', isOnline=$isOnline")

        if (isOnline) {
            try {
                remoteDataSource.saveHistory(history.toDto())
                localDao.insertOrUpdate(history.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed Firestore save, saving offline: ${e.message}", e)
                localDao.insertOrUpdate(history.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: saving history locally to Room DB")
            localDao.insertOrUpdate(history.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncHistory(): Flow<Result<Unit>> = flow {
        try {
            syncInternal()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private suspend fun syncInternal() {
        if (!networkMonitor.isConnected()) return
        val unsyncedList = localDao.getUnsyncedHistory()
        Log.d(TAG, "syncInternal: found ${unsyncedList.size} unsynced items")
        for (item in unsyncedList) {
            try {
                remoteDataSource.saveHistory(item.toDomain().toDto())
                localDao.markAsSynced(item.id)
                Log.d(TAG, "Synced item ID: ${item.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed syncing item ID: ${item.id} - ${e.message}")
            }
        }
    }
}
