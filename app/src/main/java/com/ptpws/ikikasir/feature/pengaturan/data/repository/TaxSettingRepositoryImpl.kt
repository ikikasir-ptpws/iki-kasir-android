package com.ptpws.ikikasir.feature.pengaturan.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.pengaturan.data.local.dao.TaxSettingDao
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource.TaxSettingRemoteDataSource
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TaxSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.TaxSettingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TaxSettingRepo"

@Singleton
class TaxSettingRepositoryImpl @Inject constructor(
    private val localDao: TaxSettingDao,
    private val remoteDataSource: TaxSettingRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : TaxSettingRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Auto-sync when network is restored
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network restored, auto-syncing tax setting...")
                    try { syncInternal() } catch (e: Exception) {
                        Log.e(TAG, "Auto-sync error: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getTaxSetting(): Flow<TaxSetting> {
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remote = remoteDataSource.getTaxSetting()
                    if (remote != null) {
                        localDao.insertOrUpdate(remote.toEntity())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed fetching remote tax setting: ${e.message}")
                }
            }
        }
        return localDao.getTaxSettingFlow().map { entity ->
            entity?.toDomain() ?: TaxSetting()
        }
    }

    override suspend fun saveTaxSetting(setting: TaxSetting): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        if (isOnline) {
            try {
                remoteDataSource.saveTaxSetting(setting.toDto())
                localDao.insertOrUpdate(setting.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Failed remote save, saving locally: ${e.message}")
                localDao.insertOrUpdate(setting.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            localDao.insertOrUpdate(setting.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingTaxSetting(): Flow<Result<Unit>> = flow {
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
        val unsyncedList = localDao.getUnsyncedTaxSettings()
        for (item in unsyncedList) {
            try {
                remoteDataSource.saveTaxSetting(item.toDomain().toDto())
                localDao.markAsSynced(item.id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed syncing item ${item.id}: ${e.message}")
            }
        }

        try {
            val remote = remoteDataSource.getTaxSetting()
            if (remote != null) {
                localDao.insertOrUpdate(remote.toEntity())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed pulling remote tax setting: ${e.message}")
        }
    }
}
