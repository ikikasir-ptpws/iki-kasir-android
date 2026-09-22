package com.ptpws.ikikasir.feature.role.data.repository

import android.util.Log
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.role.data.local.dao.RoleDao
import com.ptpws.ikikasir.feature.role.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.role.data.remote.datasource.RoleRemoteDataSource
import com.ptpws.ikikasir.feature.role.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.role.domain.model.Role
import com.ptpws.ikikasir.feature.role.domain.repository.RoleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RoleRepository"

@Singleton
class RoleRepositoryImpl @Inject constructor(
    private val localDao: RoleDao,
    private val remoteDataSource: RoleRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : RoleRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Otomatis sinkronisasi saat koneksi jaringan kembali online
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Koneksi online terdeteksi, menjalankan auto-sync role...")
                    try {
                        syncInternal()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saat auto-sync role: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getRoleList(): Flow<List<Role>> {
        // Jika online, update cache Room dari Firestore di background
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllRoles()
                    if (remoteList.isNotEmpty()) {
                        localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal mengambil data role dari remote background: ${e.message}")
                }
            }
        }

        // Kembalikan data Room DB sebagai Single Source of Truth
        return localDao.getAllRolesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRoleById(id: String): Flow<Role?> {
        return localDao.getRoleByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertRole(role: Role): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        Log.d(TAG, "insertRole dipanggil: name='${role.name}', isOnline=$isOnline")

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Mengirim data ke Firestore collection 'roles' dengan ID '${role.id}'...")
                remoteDataSource.saveRole(role.toDto())
                Log.d(TAG, "Online: Berhasil simpan ke Firestore! Menyimpan ke Room DB (isSynced = true)")
                localDao.insertOrUpdate(role.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Gagal simpan ke Firestore (disimpan ke Room DB dengan isSynced = false). Error: ${e.message}", e)
                localDao.insertOrUpdate(role.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Menyimpan ke Room DB (isSynced = false)")
            localDao.insertOrUpdate(role.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun updateRole(role: Role): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Update role di Firestore...")
                remoteDataSource.updateRole(role.toDto())
                Log.d(TAG, "Online: Firestore update sukses, update Room DB dengan isSynced = true")
                localDao.insertOrUpdate(role.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Gagal update Firestore online, fallback simpan lokal: ${e.message}")
                localDao.insertOrUpdate(role.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Update role di Room DB dengan isSynced = false")
            localDao.insertOrUpdate(role.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun deleteRole(id: String): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Hapus role dari Firestore...")
                remoteDataSource.deleteRole(id)
                Log.d(TAG, "Online: Firestore hapus sukses, hapus permanen dari Room DB")
                localDao.deletePermanently(id)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Gagal hapus Firestore online, tandai isDeleted lokal: ${e.message}")
                localDao.markAsDeleted(id)
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Tandai role isDeleted di Room DB")
            localDao.markAsDeleted(id)
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingRoles(): Flow<Result<Unit>> = flow {
        try {
            if (!networkMonitor.isConnected()) {
                Log.d(TAG, "Gagal sync: Perangkat offline")
                emit(Result.failure(Exception("Tidak ada koneksi internet")))
                return@flow
            }

            syncInternal()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e(TAG, "Sync gagal: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    private suspend fun syncInternal() {
        val unsyncedItems = localDao.getUnsyncedRoles()
        Log.d(TAG, "Ditemukan ${unsyncedItems.size} role belum tersinkronisasi")

        for (item in unsyncedItems) {
            if (item.isDeleted) {
                Log.d(TAG, "Sinkronisasi hapus untuk ID: ${item.id}")
                try {
                    remoteDataSource.deleteRole(item.id)
                    localDao.deletePermanently(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal sinkronisasi hapus untuk ${item.id}: ${e.message}")
                }
            } else {
                Log.d(TAG, "Sinkronisasi upsert untuk ID: ${item.id} (${item.name})")
                try {
                    remoteDataSource.saveRole(item.toDomain().toDto())
                    localDao.markAsSynced(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal sinkronisasi upsert untuk ${item.id}: ${e.message}")
                }
            }
        }

        // Setelah push data lokal, tarik data terbaru dari Firestore
        try {
            val remoteList = remoteDataSource.getAllRoles()
            if (remoteList.isNotEmpty()) {
                localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menarik data remote roles saat sync: ${e.message}")
        }
    }
}
