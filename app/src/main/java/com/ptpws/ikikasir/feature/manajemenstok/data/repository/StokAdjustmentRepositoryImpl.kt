package com.ptpws.ikikasir.feature.manajemenstok.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.manajemenstok.data.local.dao.StokAdjustmentDao
import com.ptpws.ikikasir.feature.manajemenstok.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.manajemenstok.data.remote.datasource.StokAdjustmentRemoteDataSource
import com.ptpws.ikikasir.feature.manajemenstok.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.MovementType
import com.ptpws.ikikasir.feature.manajemenstok.domain.model.StockMovement
import com.ptpws.ikikasir.feature.manajemenstok.domain.repository.StockMovementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StockMovementRepo"

@Singleton
class StokAdjustmentRepositoryImpl @Inject constructor(
    private val localDao: StokAdjustmentDao,
    private val remoteDataSource: StokAdjustmentRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : StockMovementRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Auto-sync pending records when connectivity is restored
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network online, auto-syncing pending stock movements...")
                    try {
                        syncInternal()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during auto-sync: ", e)
                    }
                }
            }
        }
    }

    override fun getAllMovements(): Flow<List<StockMovement>> {
        // Background refresh from Firestore if online
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllMovements()
                    if (remoteList.isNotEmpty()) {
                        localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch remote stock movements: ", e)
                }
            }
        }
        // Room as Single Source of Truth
        return localDao.getAllMovementsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMovementsByProductId(productId: String): Flow<List<StockMovement>> {
        return localDao.getMovementsByProductIdFlow(productId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveMovement(movement: StockMovement): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        Log.d(TAG, "saveMovement: movementId='${movement.movementId}', productId='${movement.productId}', type=${movement.type.name}, isOnline=$isOnline")

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Updating stock movement in Firestore (stock_movements) for document ID '${movement.movementId}'...")
                remoteDataSource.saveMovement(movement.toDto())
                Log.d(TAG, "Online: Firestore save success, updating Room DB (isSynced=true)")
                localDao.insertOrUpdate(movement.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save to Firestore, saving locally (isSynced=false): ", e)
                localDao.insertOrUpdate(movement.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Saving stock movement to Room (isSynced=false)")
            localDao.insertOrUpdate(movement.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingMovements(): Flow<Result<Unit>> = flow {
        try {
            if (!networkMonitor.isConnected()) {
                Log.d(TAG, "Cannot sync: Device is offline")
                emit(Result.failure(Exception("Tidak ada koneksi internet")))
                return@flow
            }
            syncInternal()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ", e)
            emit(Result.failure(e))
        }
    }

    private suspend fun syncInternal() {
        val unsyncedItems = localDao.getUnsyncedMovements()
        Log.d(TAG, "Found ${unsyncedItems.size} unsynced stock movement items")

        for (item in unsyncedItems) {
            try {
                remoteDataSource.saveMovement(item.toDomain().toDto())
                localDao.markAsSynced(item.movementId)
                Log.d(TAG, "Synced movementId: ${item.movementId}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync movementId ${item.movementId}: ", e)
            }
        }

        // Pull latest from Firestore after pushing
        try {
            val remoteList = remoteDataSource.getAllMovements()
            if (remoteList.isNotEmpty()) {
                localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull remote stock movements: ", e)
        }
    }
}
