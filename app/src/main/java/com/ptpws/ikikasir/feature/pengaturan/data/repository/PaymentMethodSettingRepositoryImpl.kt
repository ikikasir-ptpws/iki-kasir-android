package com.ptpws.ikikasir.feature.pengaturan.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.pengaturan.data.local.dao.PaymentMethodSettingDao
import com.ptpws.ikikasir.feature.pengaturan.data.local.entity.PaymentMethodSettingEntity
import com.ptpws.ikikasir.feature.pengaturan.data.remote.datasource.PaymentMethodSettingRemoteDataSource
import com.ptpws.ikikasir.feature.pengaturan.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.pengaturan.domain.model.PaymentMethodSetting
import com.ptpws.ikikasir.feature.pengaturan.domain.repository.PaymentMethodSettingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PaymentMethodSettingRepo"

@Singleton
class PaymentMethodSettingRepositoryImpl @Inject constructor(
    private val localDao: PaymentMethodSettingDao,
    private val remoteDataSource: PaymentMethodSettingRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : PaymentMethodSettingRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Auto-sync when network connectivity is restored
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network restored, auto-syncing payment method setting...")
                    try { syncInternal() } catch (e: Exception) {
                        Log.e(TAG, "Auto-sync error: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getSetting(): Flow<PaymentMethodSetting> {
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remote = remoteDataSource.getSetting()
                    if (remote != null) {
                        localDao.insertOrUpdate(
                            PaymentMethodSettingEntity.fromDomain(remote.toDomain(), isSynced = true)
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed fetching remote payment method setting: ${e.message}")
                }
            }
        }
        return localDao.getSettingFlow().map { entity ->
            entity?.toDomain() ?: PaymentMethodSetting()
        }
    }

    override suspend fun saveSetting(setting: PaymentMethodSetting): Result<Unit> {
        val isOnline = networkMonitor.isConnected()
        return try {
            if (isOnline) {
                remoteDataSource.saveSetting(setting.toDto())
                localDao.insertOrUpdate(PaymentMethodSettingEntity.fromDomain(setting, isSynced = true))
            } else {
                localDao.insertOrUpdate(PaymentMethodSettingEntity.fromDomain(setting, isSynced = false))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed remote save, saving locally: ${e.message}")
            localDao.insertOrUpdate(PaymentMethodSettingEntity.fromDomain(setting, isSynced = false))
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
        val unsyncedList = localDao.getUnsyncedSettings()
        for (item in unsyncedList) {
            try {
                remoteDataSource.saveSetting(item.toDomain().toDto())
                localDao.markAsSynced(item.id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed syncing item ${item.id}: ${e.message}")
            }
        }

        try {
            val remote = remoteDataSource.getSetting()
            if (remote != null) {
                localDao.insertOrUpdate(
                    PaymentMethodSettingEntity.fromDomain(remote.toDomain(), isSynced = true)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed pulling remote payment method setting: ${e.message}")
        }
    }
}
