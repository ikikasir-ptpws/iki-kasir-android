package com.ptpws.ikikasir.feature.kategori.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.kategori.data.local.dao.KategoriDao
import com.ptpws.ikikasir.feature.kategori.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.kategori.data.remote.datasource.KategoriRemoteDataSource
import com.ptpws.ikikasir.feature.kategori.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.kategori.domain.model.Kategori
import com.ptpws.ikikasir.feature.kategori.domain.repository.KategoriRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "KategoriRepository"

@Singleton
class KategoriRepositoryImpl @Inject constructor(
    private val localDao: KategoriDao,
    private val remoteDataSource: KategoriRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : KategoriRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Automatically sync pending items when network connectivity is restored
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network is online, triggering auto-sync...")
                    try {
                        syncInternal()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during auto-sync: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getKategoriList(): Flow<List<Kategori>> {
        // If online, refresh local cache from remote in background
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllKategori()
                    if (remoteList.isNotEmpty()) {
                        localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch remote kategori in background: ${e.message}")
                }
            }
        }

        // Return local database as Single Source of Truth
        return localDao.getAllKategoriFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getKategoriById(id: String): Flow<Kategori?> {
        return localDao.getKategoriByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertKategori(kategori: Kategori): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        Log.d(TAG, "insertKategori triggered: nama='${kategori.nama}', isOnline=$isOnline")

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Mengirim data ke Firestore collection 'categories' with ID '${kategori.id}'...")
                remoteDataSource.saveKategori(kategori.toDto())
                Log.d(TAG, "Online: Sukses simpan ke Firestore! Menyimpan ke Room DB (isSynced = true)")
                localDao.insertOrUpdate(kategori.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Gagal simpan ke Firestore (akan disimpan ke Room DB lokal dengan isSynced = false). Error: ${e.message}", e)
                localDao.insertOrUpdate(kategori.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Menyimpan ke Room DB lokal (isSynced = false)")
            localDao.insertOrUpdate(kategori.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun updateKategori(kategori: Kategori): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Updating kategori in Firestore...")
                remoteDataSource.updateKategori(kategori.toDto())
                Log.d(TAG, "Online: Firestore update success, updating Room DB with isSynced = true")
                localDao.insertOrUpdate(kategori.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update in Firestore online, falling back to local DB: ${e.message}")
                localDao.insertOrUpdate(kategori.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Updating kategori in Room DB with isSynced = false")
            localDao.insertOrUpdate(kategori.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun deleteKategori(id: String): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Deleting kategori from Firestore...")
                remoteDataSource.deleteKategori(id)
                Log.d(TAG, "Online: Firestore delete success, deleting permanently from Room DB")
                localDao.deletePermanently(id)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete from Firestore online, marking as deleted locally: ${e.message}")
                localDao.markAsDeleted(id)
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Marking kategori as deleted in Room DB")
            localDao.markAsDeleted(id)
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingKategori(): Flow<Result<Unit>> = flow {
        try {
            if (!networkMonitor.isConnected()) {
                Log.d(TAG, "Cannot sync: Device is offline")
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
        val unsyncedItems = localDao.getUnsyncedKategori()
        Log.d(TAG, "Found ${unsyncedItems.size} unsynced kategori items")

        for (item in unsyncedItems) {
            if (item.isDeleted) {
                Log.d(TAG, "Syncing delete for ID: ${item.id}")
                try {
                    remoteDataSource.deleteKategori(item.id)
                    localDao.deletePermanently(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync delete for ${item.id}: ${e.message}")
                }
            } else {
                Log.d(TAG, "Syncing upsert for ID: ${item.id} (${item.nama})")
                try {
                    remoteDataSource.saveKategori(item.toDomain().toDto())
                    localDao.markAsSynced(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync upsert for ${item.id}: ${e.message}")
                }
            }
        }

        // After pushing local changes, pull latest changes from Firestore
        try {
            val remoteList = remoteDataSource.getAllKategori()
            if (remoteList.isNotEmpty()) {
                localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull remote categories during sync: ${e.message}")
        }
    }
}
