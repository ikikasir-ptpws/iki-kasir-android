package com.ptpws.ikikasir.feature.pengaturan.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.pengaturan.data.local.dao.TableSettingDao
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.TableSettingEntity
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.TableSettingPreferences
import com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource.TableSettingRemoteDataSource
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.pengaturan.domain.model.TableSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.TableSettingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TableSettingRepo"

@Singleton
class TableSettingRepositoryImpl @Inject constructor(
    private val dao: TableSettingDao,
    private val remoteDataSource: TableSettingRemoteDataSource,
    private val preferences: TableSettingPreferences,
    private val networkMonitor: NetworkMonitor
) : TableSettingRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Auto-sync when network connectivity is restored
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network restored, auto-syncing table setting...")
                    try { syncInternal() } catch (e: Exception) {
                        Log.e(TAG, "Auto-sync error: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getSetting(): Flow<TableSetting> {
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remote = remoteDataSource.getSetting()
                    if (remote != null) {
                        dao.insertOrUpdate(
                            TableSettingEntity(
                                id = remote.id,
                                isTableEnabled = remote.isTableEnabled,
                                updatedAt = remote.updatedAt,
                                isSynced = true
                            )
                        )
                        preferences.setTableEnabled(remote.isTableEnabled)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed fetching remote table setting: ${e.message}")
                }
            }
        }
        return dao.getSettingFlow().map { entity ->
            entity?.toDomain() ?: TableSetting()
        }
    }

    override suspend fun saveSetting(setting: TableSetting): Result<Unit> {
        val isOnline = networkMonitor.isConnected()
        return try {
            if (isOnline) {
                remoteDataSource.saveSetting(setting.toDto())
                dao.insertOrUpdate(TableSettingEntity.fromDomain(setting, isSynced = true))
            } else {
                dao.insertOrUpdate(TableSettingEntity.fromDomain(setting, isSynced = false))
            }
            preferences.setTableEnabled(setting.isTableEnabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed remote save, saving locally: ${e.message}")
            dao.insertOrUpdate(TableSettingEntity.fromDomain(setting, isSynced = false))
            preferences.setTableEnabled(setting.isTableEnabled)
            Result.success(Unit)
        }
    }

    override suspend fun syncPendingSetting(): Result<Unit> {
        return try {
            if (!networkMonitor.isConnected()) {
                return Result.failure(Exception("Tidak ada koneksi internet"))
            }
            syncInternal()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncInternal() {
        val unsyncedList = dao.getUnsyncedSettings()
        for (item in unsyncedList) {
            try {
                remoteDataSource.saveSetting(item.toDomain().toDto())
                dao.markAsSynced(item.id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed syncing item ${item.id}: ${e.message}")
            }
        }

        try {
            val remote = remoteDataSource.getSetting()
            if (remote != null) {
                dao.insertOrUpdate(
                    TableSettingEntity(
                        id = remote.id,
                        isTableEnabled = remote.isTableEnabled,
                        updatedAt = remote.updatedAt,
                        isSynced = true
                    )
                )
                preferences.setTableEnabled(remote.isTableEnabled)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed pulling remote table setting: ${e.message}")
        }
    }
}
