package com.ptpws.ikikasir.feature.produk.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.core.util.FirebaseStorageHelper
import com.ptpws.ikikasir.feature.produk.data.local.dao.ProdukDao
import com.ptpws.ikikasir.feature.produk.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.produk.data.remote.datasource.ProdukRemoteDataSource
import com.ptpws.ikikasir.feature.produk.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.produk.domain.model.Produk
import com.ptpws.ikikasir.feature.produk.domain.repository.ProdukRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ProdukRepository"

@Singleton
class ProdukRepositoryImpl @Inject constructor(
    private val localDao: ProdukDao,
    private val remoteDataSource: ProdukRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : ProdukRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Automatically sync pending items when network connectivity is restored
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Network is online, triggering auto-sync for produk...")
                    try {
                        syncInternal()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during auto-sync: ", e)
                    }
                }
            }
        }

        // Real-time Firestore synchronization for products
        observeRemoteProduk()
    }

    private fun observeRemoteProduk() {
        repositoryScope.launch {
            while (true) {
                try {
                    remoteDataSource.getProdukFlow()
                        .collect { remoteList ->
                            Log.d(TAG, "Realtime Firestore produk update received: ${remoteList.size} items")
                            val unsyncedIds = localDao.getUnsyncedProduk().map { it.id }.toSet()
                            val entities = remoteList
                                .filterNot { it.id in unsyncedIds }
                                .map { it.toEntity() }
                            localDao.insertOrUpdateAll(entities)
                            val remoteIds = remoteList.map { it.id }
                            if (remoteIds.isNotEmpty()) {
                                localDao.deleteSyncedNotInRemote(remoteIds)
                            } else {
                                localDao.deleteAllSynced()
                            }
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Realtime Firestore produk stream error: ${e.message}", e)
                }
                delay(4000)
            }
        }
    }

    override suspend fun refreshProduk() {
        if (networkMonitor.isConnected()) {
            try {
                val remoteList = remoteDataSource.getAllProduk()
                if (remoteList.isNotEmpty()) {
                    val unsyncedIds = localDao.getUnsyncedProduk().map { it.id }.toSet()
                    localDao.insertOrUpdateAll(
                        remoteList.filterNot { it.id in unsyncedIds }.map { it.toEntity() }
                    )
                    localDao.deleteSyncedNotInRemote(remoteList.map { it.id })
                } else {
                    localDao.deleteAllSynced()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh remote produk: ${e.message}")
            }
        }
    }

    override fun getProdukList(): Flow<List<Produk>> {
        // If online, refresh local cache from remote in background
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                refreshProduk()
            }
        }

        // Return local database as Single Source of Truth
        return localDao.getAllProdukFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getProdukByCategoryId(categoryId: String): Flow<List<Produk>> {
        return localDao.getProdukByCategoryIdFlow(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getProdukById(id: String): Flow<Produk?> {
        return localDao.getProdukByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertProduk(produk: Produk): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        localDao.insertOrUpdate(produk.toEntity(isSynced = false))

        if (isOnline) {
            var produkToSave = produk
            try {
                produkToSave = uploadLocalImage(produk)
                remoteDataSource.saveProduk(produkToSave.toDto())
                localDao.insertOrUpdate(produkToSave.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Produk disimpan lokal dan akan disinkronkan saat koneksi tersedia.", e)
                localDao.insertOrUpdate(produkToSave.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Menyimpan ke Room DB lokal (isSynced = false)")
            localDao.insertOrUpdate(produk.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun updateProduk(produk: Produk): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        localDao.insertOrUpdate(produk.toEntity(isSynced = false))

        if (isOnline) {
            var produkToSave = produk
            try {
                produkToSave = uploadLocalImage(produk)
                remoteDataSource.updateProduk(produkToSave.toDto())
                localDao.insertOrUpdate(produkToSave.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Produk diperbarui lokal dan akan disinkronkan saat koneksi tersedia.", e)
                localDao.insertOrUpdate(produkToSave.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Updating produk in Room DB with isSynced = false")
            localDao.insertOrUpdate(produk.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun deleteProduk(id: String): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Deleting produk from Firestore...")
                remoteDataSource.deleteProduk(id)
                Log.d(TAG, "Online: Firestore delete success, deleting permanently from Room DB")
                localDao.deletePermanently(id)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete from Firestore online, marking as deleted locally: ")
                localDao.markAsDeleted(id)
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Marking produk as deleted in Room DB")
            localDao.markAsDeleted(id)
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingProduk(): Flow<Result<Unit>> = flow {
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
        val unsyncedItems = localDao.getUnsyncedProduk()
        Log.d(TAG, "Found  unsynced produk items")

        for (item in unsyncedItems) {
            if (item.isDeleted) {
                Log.d(TAG, "Syncing delete for ID: ")
                try {
                    remoteDataSource.deleteProduk(item.id)
                    localDao.deletePermanently(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync delete for : ")
                }
            } else {
                try {
                    val produkWithUploadedImage = uploadLocalImage(item.toDomain())
                    remoteDataSource.saveProduk(produkWithUploadedImage.toDto())
                    localDao.insertOrUpdate(produkWithUploadedImage.toEntity(isSynced = true))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync product ${item.id}: ${e.message}", e)
                }
            }
        }

        // After pushing local changes, pull latest changes from Firestore
        try {
            val remoteList = remoteDataSource.getAllProduk()
            if (remoteList.isNotEmpty()) {
                val unsyncedIds = localDao.getUnsyncedProduk().map { it.id }.toSet()
                localDao.insertOrUpdateAll(
                    remoteList.filterNot { it.id in unsyncedIds }.map { it.toEntity() }
                )
                localDao.deleteSyncedNotInRemote(remoteList.map { it.id })
            } else {
                localDao.deleteAllSynced()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull remote products during sync: ${e.message}")
        }
    }

    private suspend fun uploadLocalImage(produk: Produk): Produk {
        val downloadUrl = FirebaseStorageHelper.uploadLocalImage(
            imageReference = produk.imageUrl,
            folderName = "product_images"
        )
        return produk.copy(imageUrl = downloadUrl)
    }
}