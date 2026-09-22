package com.ptpws.ikikasir.feature.manajemenpengguna.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.datasource.UserRemoteDataSource
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserRepository"

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localDao: UserDao,
    private val remoteDataSource: UserRemoteDataSource,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context
) : UserRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Otomatis sinkronisasi saat koneksi jaringan kembali online
        repositoryScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Koneksi online terdeteksi, menjalankan auto-sync user...")
                    try {
                        syncInternal()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saat auto-sync user: ${e.message}", e)
                    }
                }
            }
        }
    }

    override fun getUserList(): Flow<List<User>> {
        // Jika online, update cache Room dari Firestore di background
        if (networkMonitor.isConnected()) {
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllUsers()
                    if (remoteList.isNotEmpty()) {
                        localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal mengambil data user dari remote background: ${e.message}")
                }
            }
        }

        // Kembalikan data Room DB sebagai Single Source of Truth
        return localDao.getAllUsersFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUserById(id: String): Flow<User?> {
        return localDao.getUserByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertUser(user: User): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        Log.d(TAG, "insertUser dipanggil: fullName='${user.fullName}', isOnline=$isOnline")

        if (isOnline) {
            createAuthUserIfOnline(user)
            try {
                Log.d(TAG, "Online: Mengirim data ke Firestore collection 'users' dengan ID '${user.id}'...")
                remoteDataSource.saveUser(user.toDto())
                Log.d(TAG, "Online: Berhasil simpan ke Firestore! Menyimpan ke Room DB (isSynced = true)")
                localDao.insertOrUpdate(user.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Gagal simpan ke Firestore (disimpan ke Room DB dengan isSynced = false). Error: ${e.message}", e)
                localDao.insertOrUpdate(user.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Menyimpan ke Room DB (isSynced = false)")
            localDao.insertOrUpdate(user.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    private suspend fun createAuthUserIfOnline(user: User) {
        if (user.email.isBlank() || user.password.isBlank()) return
        try {
            val appName = "SecondaryAuthApp"
            val secondaryApp = try {
                FirebaseApp.getInstance(appName)
            } catch (e: Exception) {
                val options = FirebaseApp.getInstance().options
                FirebaseApp.initializeApp(context, options, appName)
            }
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
            try {
                secondaryAuth.createUserWithEmailAndPassword(user.email.trim(), user.password).await()
                Log.d(TAG, "Secondary Auth: Akun ${user.email} berhasil terdaftar di Firebase Auth")
                secondaryAuth.signOut()
            } catch (e: Exception) {
                Log.w(TAG, "Secondary Auth: Terjadi kesalahan / sudah terdaftar ${user.email}: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Secondary Auth exception: ${e.message}", e)
        }
    }

    override suspend fun updateUser(user: User): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Update user di Firestore...")
                remoteDataSource.updateUser(user.toDto())
                Log.d(TAG, "Online: Firestore update sukses, update Room DB dengan isSynced = true")
                localDao.insertOrUpdate(user.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Gagal update Firestore online, fallback simpan lokal: ${e.message}")
                localDao.insertOrUpdate(user.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Update user di Room DB dengan isSynced = false")
            localDao.insertOrUpdate(user.toEntity(isSynced = false))
            emit(Result.success(Unit))
        }
    }

    override suspend fun deleteUser(id: String): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Hapus user dari Firestore...")
                remoteDataSource.deleteUser(id)
                Log.d(TAG, "Online: Firestore hapus sukses, hapus permanen dari Room DB")
                localDao.deletePermanently(id)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Gagal hapus Firestore online, tandai isDeleted lokal: ${e.message}")
                localDao.markAsDeleted(id)
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Tandai user isDeleted di Room DB")
            localDao.markAsDeleted(id)
            emit(Result.success(Unit))
        }
    }

    override suspend fun syncPendingUsers(): Flow<Result<Unit>> = flow {
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
        val unsyncedItems = localDao.getUnsyncedUsers()
        Log.d(TAG, "Ditemukan ${unsyncedItems.size} user belum tersinkronisasi")

        for (item in unsyncedItems) {
            if (item.isDeleted) {
                Log.d(TAG, "Sinkronisasi hapus untuk ID: ${item.id}")
                try {
                    remoteDataSource.deleteUser(item.id)
                    localDao.deletePermanently(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal sinkronisasi hapus untuk ${item.id}: ${e.message}")
                }
            } else {
                Log.d(TAG, "Sinkronisasi upsert untuk ID: ${item.id} (${item.fullName})")
                try {
                    remoteDataSource.saveUser(item.toDomain().toDto())
                    localDao.markAsSynced(item.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal sinkronisasi upsert untuk ${item.id}: ${e.message}")
                }
            }
        }

        // Setelah push data lokal, tarik data terbaru dari Firestore
        try {
            val remoteList = remoteDataSource.getAllUsers()
            if (remoteList.isNotEmpty()) {
                localDao.insertOrUpdateAll(remoteList.map { it.toEntity() })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menarik data remote users saat sync: ${e.message}")
        }
    }
}
