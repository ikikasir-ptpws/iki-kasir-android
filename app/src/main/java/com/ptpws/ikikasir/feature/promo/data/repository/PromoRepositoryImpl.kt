package com.ptpws.ikikasir.feature.promo.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.promo.data.local.dao.PromoDao
import com.ptpws.ikikasir.feature.promo.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.promo.data.remote.datasource.PromoRemoteDataSource
import com.ptpws.ikikasir.feature.promo.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.promo.domain.model.Promo
import com.ptpws.ikikasir.feature.promo.domain.repository.PromoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PromoRepository"

@Singleton
class PromoRepositoryImpl @Inject constructor(
    private val localDao: PromoDao,
    private val remoteDataSource: PromoRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : PromoRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Auto-sync when connectivity changes to online
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network is online, triggering auto-sync for promos...")
                    try {
                        syncInternal()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during promo auto-sync: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getPromoList(): Flow<List<Promo>> {
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllPromos()
                    if (remoteList.isNotEmpty()) {
                        localDao.insertOrUpdateAll(remoteList.map { it.toDomain().toEntity(isSynced = true) })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch remote promos: ${e.message}")
                }
            }
        }
        return localDao.getAllPromosFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActivePromos(): Flow<List<Promo>> {
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllPromos()
                    if (remoteList.isNotEmpty()) {
                        localDao.insertOrUpdateAll(remoteList.map { it.toDomain().toEntity(isSynced = true) })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch remote active promos: ${e.message}")
                }
            }
        }
        return localDao.getActivePromosFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPromoById(id: String): Flow<Promo?> {
        return localDao.getPromoByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertPromo(promo: Promo): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        val promoDto = promo.toDto()

        if (isOnline) {
            try {
                remoteDataSource.savePromo(promoDto)
                localDao.insertOrUpdate(promoDto.toDomain().toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save promo to Firestore: ${e.message}")
                localDao.insertOrUpdate(promo.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            localDao.insertOrUpdate(promo.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun updatePromo(promo: Promo): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                remoteDataSource.updatePromo(promo.toDto())
                localDao.insertOrUpdate(promo.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update promo in Firestore: ${e.message}")
                localDao.insertOrUpdate(promo.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            localDao.insertOrUpdate(promo.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun deletePromo(id: String): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                remoteDataSource.deletePromo(id)
                localDao.deletePermanently(id)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete promo in Firestore: ${e.message}")
                localDao.markAsDeleted(id)
                emit(Result.success(Unit))
            }
        } else {
            localDao.markAsDeleted(id)
            emit(Result.success(Unit))
        }
    }

    override suspend fun togglePromoStatus(id: String, isActive: Boolean): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                remoteDataSource.toggleStatus(id, isActive)
                localDao.toggleStatus(id, isActive)
                localDao.markAsSynced(id)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle status in Firestore: ${e.message}")
                localDao.toggleStatus(id, isActive)
                emit(Result.success(Unit))
            }
        } else {
            localDao.toggleStatus(id, isActive)
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingPromos(): Flow<Result<Unit>> = flow {
        try {
            if (!networkMonitor.isConnected()) {
                emit(Result.failure(Exception("Tidak ada koneksi internet")))
                return@flow
            }
            syncInternal()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private suspend fun syncInternal() {
        val unsyncedItems = localDao.getUnsyncedPromos()
        for (item in unsyncedItems) {
            if (item.isDeleted) {
                try {
                    remoteDataSource.deletePromo(item.id)
                    localDao.deletePermanently(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync delete for promo ${item.id}: ${e.message}")
                }
            } else {
                try {
                    remoteDataSource.savePromo(item.toDomain().toDto())
                    localDao.markAsSynced(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync promo ${item.id}: ${e.message}")
                }
            }
        }

        try {
            val remoteList = remoteDataSource.getAllPromos()
            if (remoteList.isNotEmpty()) {
                localDao.insertOrUpdateAll(remoteList.map { it.toDomain().toEntity(isSynced = true) })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull remote promos during sync: ${e.message}")
        }
    }
}
