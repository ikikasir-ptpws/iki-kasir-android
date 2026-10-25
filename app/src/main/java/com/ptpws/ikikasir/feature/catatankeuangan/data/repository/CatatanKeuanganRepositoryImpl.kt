package com.ptpws.ikikasir.feature.catatankeuangan.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.catatankeuangan.data.local.dao.CatatanKeuanganDao
import com.ptpws.ikikasir.feature.catatankeuangan.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.catatankeuangan.data.remote.datasource.CatatanKeuanganRemoteDataSource
import com.ptpws.ikikasir.feature.catatankeuangan.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.catatankeuangan.domain.model.CatatanKeuangan
import com.ptpws.ikikasir.feature.catatankeuangan.domain.repository.CatatanKeuanganRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CatatanKeuanganRepo"

@Singleton
class CatatanKeuanganRepositoryImpl @Inject constructor(
    private val localDao: CatatanKeuanganDao,
    private val remoteDataSource: CatatanKeuanganRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : CatatanKeuanganRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    try {
                        syncInternal()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error auto-sync: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getCatatanList(): Flow<List<CatatanKeuangan>> {
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllCatatan()
                    if (remoteList.isNotEmpty()) {
                        localDao.insertOrUpdateAll(remoteList.map { it.toDomain().toEntity(isSynced = true) })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed fetch remote: ${e.message}")
                }
            }
        }
        return localDao.getAllCatatanFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCatatanById(id: String): Flow<CatatanKeuangan?> {
        return localDao.getCatatanByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertCatatan(catatan: CatatanKeuangan): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        val dto = catatan.toDto()

        if (isOnline) {
            try {
                remoteDataSource.saveCatatan(dto)
                localDao.insertOrUpdate(catatan.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed save remote: ${e.message}")
                localDao.insertOrUpdate(catatan.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            localDao.insertOrUpdate(catatan.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun updateCatatan(catatan: CatatanKeuangan): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        val dto = catatan.toDto()

        if (isOnline) {
            try {
                remoteDataSource.updateCatatan(dto)
                localDao.insertOrUpdate(catatan.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed update remote: ${e.message}")
                localDao.insertOrUpdate(catatan.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            localDao.insertOrUpdate(catatan.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun deleteCatatan(id: String): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                remoteDataSource.deleteCatatan(id)
                localDao.deletePermanently(id)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed delete remote: ${e.message}")
                localDao.markAsDeleted(id)
                emit(Result.success(Unit))
            }
        } else {
            localDao.markAsDeleted(id)
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingCatatan(): Flow<Result<Unit>> = flow {
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
        val unsyncedItems = localDao.getUnsyncedCatatan()
        for (item in unsyncedItems) {
            if (item.isDeleted) {
                try {
                    remoteDataSource.deleteCatatan(item.id)
                    localDao.deletePermanently(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed sync delete ${item.id}: ${e.message}")
                }
            } else {
                try {
                    remoteDataSource.saveCatatan(item.toDomain().toDto())
                    localDao.markAsSynced(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed sync save ${item.id}: ${e.message}")
                }
            }
        }

        try {
            val remoteList = remoteDataSource.getAllCatatan()
            if (remoteList.isNotEmpty()) {
                localDao.insertOrUpdateAll(remoteList.map { it.toDomain().toEntity(isSynced = true) })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed pull remote during sync: ${e.message}")
        }
    }
}
