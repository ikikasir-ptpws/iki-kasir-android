package com.ptpws.ikikasir.feature.pengaturan.data.repository

import android.content.Context
import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.pengaturan.data.local.dao.NotaSettingDao
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.NotaSettingPreferences
import com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource.NotaSettingRemoteDataSource
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.pengaturan.domain.model.NotaSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.NotaSettingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "NotaSettingRepo"

@Singleton
class NotaSettingRepositoryImpl @Inject constructor(
    private val localDao: NotaSettingDao,
    private val remoteDataSource: NotaSettingRemoteDataSource,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context
) : NotaSettingRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val prefsFallback = NotaSettingPreferences(context)

    init {
        // Auto-sync when online
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network online, triggering auto-sync for NotaSetting...")
                    try { syncInternal() } catch (e: Exception) {
                        Log.e(TAG, "Auto-sync error: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getNotaSetting(): Flow<NotaSetting> {
        // Background refresh from Firestore if online
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remote = remoteDataSource.getNotaSetting()
                    if (remote != null) {
                        localDao.insertOrUpdate(remote.toDomain().toEntity(isSynced = true))
                        // Also update SharedPreferences for fast legacy reads
                        prefsFallback.saveSetting(remote.toDomain())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed fetching remote NotaSetting: ${e.message}")
                }
            }
        }

        // Return Room DB Flow as Single Source of Truth
        return localDao.getNotaSettingFlow().map { entity ->
            if (entity != null) {
                entity.toDomain()
            } else {
                // If local Room DB is empty, migrate from SharedPreferences or use default
                val defaultSetting = prefsFallback.getSetting()
                repositoryScope.launch {
                    localDao.insertOrUpdate(defaultSetting.toEntity(isSynced = false))
                }
                defaultSetting
            }
        }
    }

    override suspend fun saveNotaSetting(setting: NotaSetting): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        Log.d(TAG, "saveNotaSetting: namaToko='${setting.namaToko}', isOnline=$isOnline")

        // Sync legacy SharedPreferences
        prefsFallback.saveSetting(setting)

        if (isOnline) {
            try {
                remoteDataSource.saveNotaSetting(setting.toDto())
                localDao.insertOrUpdate(setting.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Firestore save failed, saving to Room DB offline: ${e.message}")
                localDao.insertOrUpdate(setting.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            localDao.insertOrUpdate(setting.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingNotaSetting(): Flow<Result<Unit>> = flow {
        try {
            if (!networkMonitor.isConnected()) {
                emit(Result.failure(Exception("Device is offline")))
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
        val unsyncedList = localDao.getUnsyncedNotaSettings()
        for (item in unsyncedList) {
            try {
                remoteDataSource.saveNotaSetting(item.toDomain().toDto())
                localDao.markAsSynced(item.id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed syncing item ${item.id}: ${e.message}")
            }
        }
        try {
            val remote = remoteDataSource.getNotaSetting()
            if (remote != null) {
                localDao.insertOrUpdate(remote.toDomain().toEntity(isSynced = true))
                prefsFallback.saveSetting(remote.toDomain())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed pulling remote setting during sync: ${e.message}")
        }
    }
}
