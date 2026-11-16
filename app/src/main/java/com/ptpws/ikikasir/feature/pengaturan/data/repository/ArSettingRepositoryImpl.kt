package com.ptpws.ikikasir.feature.pengaturan.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.pengaturan.data.local.dao.ArSettingDao
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.ArSettingEntity
import com.ptpws.ikikasir.feature.pengaturan.data.preferences.ArSettingPreferences
import com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource.ArSettingRemoteDataSource
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.pengaturan.domain.model.ArSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.ArSettingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ArSettingRepo"

@Singleton
class ArSettingRepositoryImpl @Inject constructor(
    private val dao: ArSettingDao,
    private val remoteDataSource: ArSettingRemoteDataSource,
    private val preferences: ArSettingPreferences,
    private val networkMonitor: NetworkMonitor
) : ArSettingRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Auto-sync when network connectivity is restored
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network restored, auto-syncing AR setting...")
                    try { syncInternal() } catch (e: Exception) {
                        Log.e(TAG, "Auto-sync error: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getSetting(): Flow<ArSetting> {
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remote = remoteDataSource.getSetting()
                    if (remote != null && dao.getSetting()?.isSynced != false) {
                        dao.insertOrUpdate(
                            ArSettingEntity(
                                id = remote.id,
                                isArEnabled = remote.isArEnabled,
                                updatedAt = remote.updatedAt,
                                isSynced = true
                            )
                        )
                        preferences.setArEnabled(remote.isArEnabled)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed fetching remote AR setting: ${e.message}")
                }
            }
        }
        return dao.getSettingFlow().map { entity ->
            entity?.toDomain() ?: ArSetting()
        }
    }

    override suspend fun saveSetting(setting: ArSetting): Result<Unit> {
        val isOnline = networkMonitor.isConnected()
        return try {
            if (isOnline) {
                remoteDataSource.saveSetting(setting.toDto())
                dao.insertOrUpdate(ArSettingEntity.fromDomain(setting, isSynced = true))
            } else {
                dao.insertOrUpdate(ArSettingEntity.fromDomain(setting, isSynced = false))
            }
            preferences.setArEnabled(setting.isArEnabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed remote save, saving locally: ${e.message}")
            dao.insertOrUpdate(ArSettingEntity.fromDomain(setting, isSynced = false))
            preferences.setArEnabled(setting.isArEnabled)
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
            if (remote != null && dao.getUnsyncedSettings().isEmpty()) {
                dao.insertOrUpdate(
                    ArSettingEntity(
                        id = remote.id,
                        isArEnabled = remote.isArEnabled,
                        updatedAt = remote.updatedAt,
                        isSynced = true
                    )
                )
                preferences.setArEnabled(remote.isArEnabled)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed pulling remote AR setting: ${e.message}")
        }
    }
}
