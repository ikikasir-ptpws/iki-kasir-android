package com.ptpws.ikikasir.feature.manajemenpengguna.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.core.network.NetworkMonitor
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.UserEntity
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.datasource.UserRemoteDataSource
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto.UserDto
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private var realtimeSyncJob: Job? = null
    private var isReconciling = false
    private var lastReconcileTime = 0L

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
                    startRealtimeListener()
                } else {
                    stopRealtimeListener()
                }
            }
        }

        if (networkMonitor.isConnected()) {
            startRealtimeListener()
        }
    }

    private fun startRealtimeListener() {
        if (realtimeSyncJob?.isActive == true) return
        realtimeSyncJob = repositoryScope.launch {
            Log.d(TAG, "Memulai realtime listener Firestore collection 'users'...")
            while (true) {
                try {
                    remoteDataSource.getUserFlow().collect { remoteList ->
                        Log.d(TAG, "Update realtime diterima dari Firestore: ${remoteList.size} users")
                        processRemoteUsers(remoteList)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Realtime listener Firestore error, mencoba kembali dalam 5 detik: ${e.message}")
                    delay(5000)
                }
            }
        }
    }

    private fun stopRealtimeListener() {
        realtimeSyncJob?.cancel()
        realtimeSyncJob = null
    }

    private suspend fun processRemoteUsers(remoteList: List<UserDto>, forceReconcile: Boolean = false) {
        try {
            val remoteIds = remoteList.map { it.id }.toSet()
            val currentLocal: List<UserEntity> = localDao.getAllUsers()

            // 1. Sinkronisasi penghapusan: jika user ada di Room lokal (dan isSynced) tapi sudah tidak ada di Firestore,
            // berarti user telah dihapus di Firestore. Hapus segera dari Room DB!
            currentLocal.forEach { localUser ->
                if (localUser.isSynced && !remoteIds.contains(localUser.id)) {
                    Log.d(TAG, "Realtime: User ${localUser.fullName} (${localUser.id}) dihapus di remote, menghapus dari Room DB")
                    localDao.deletePermanently(localUser.id)
                }
            }

            // 2. Simpan atau perbarui user dari Firestore ke Room DB
            if (remoteList.isNotEmpty()) {
                val entitiesToUpdate = remoteList.map { it.toEntity() }
                localDao.insertOrUpdateAll(entitiesToUpdate)
            }

            // 3. Rekonsiliasi dengan Firebase Authentication
            reconcileWithFirebaseAuth(remoteList, force = forceReconcile)
        } catch (e: Exception) {
            Log.e(TAG, "Error saat memproses data remote users: ${e.message}", e)
        }
    }

    private fun getSecondaryAuth(): FirebaseAuth {
        val appName = "SecondaryAuthApp"
        val secondaryApp = try {
            FirebaseApp.getInstance(appName)
        } catch (e: Exception) {
            val options = FirebaseApp.getInstance().options
            FirebaseApp.initializeApp(context, options, appName)
        }
        return FirebaseAuth.getInstance(secondaryApp)
    }

    private suspend fun reconcileWithFirebaseAuth(users: List<UserDto>, force: Boolean = false) {
        if (!networkMonitor.isConnected()) return
        if (isReconciling) return
        val now = System.currentTimeMillis()
        if (!force && (now - lastReconcileTime < 10000L)) return

        val currentAuth = FirebaseAuth.getInstance().currentUser ?: return
        val currentEmail = currentAuth.email?.trim() ?: return
        if (currentEmail.isBlank()) return

        isReconciling = true
        lastReconcileTime = now
        try {
            val secondaryAuth = getSecondaryAuth()

            // Periksa setiap akun di daftar user (selain akun yang sedang aktif login)
            for (user in users) {
                val userEmail = user.email.trim()
                if (userEmail.isBlank()) continue
                if (userEmail.equals(currentEmail, ignoreCase = true)) continue

                var existsInAuth = false

                // 1. Cek via fetchSignInMethodsForEmail terlebih dahulu
                try {
                    val result = FirebaseAuth.getInstance().fetchSignInMethodsForEmail(userEmail).await()
                    if (!result.signInMethods.isNullOrEmpty()) {
                        existsInAuth = true
                    }
                } catch (e: Exception) {
                    if (e is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                        e.message?.contains("no user record", ignoreCase = true) == true ||
                        e.message?.contains("user-not-found", ignoreCase = true) == true) {
                        existsInAuth = false
                    }
                }

                // 2. Jika fetchSignInMethodsForEmail kosong (misal karena Email Enumeration Protection aktif di Firebase Console),
                // uji dengan collision check menggunakan secondaryAuth
                if (!existsInAuth) {
                    try {
                        val testPass = "TempCheckSync!@#99"
                        val createResult = secondaryAuth.createUserWithEmailAndPassword(userEmail, testPass).await()
                        // Jika berhasil dibuat, berarti sebelumnya akun ini TIDAK ADA di Firebase Auth!
                        existsInAuth = false
                        try {
                            createResult.user?.delete()?.await()
                        } catch (delEx: Exception) {
                            Log.w(TAG, "Gagal delete temp user: ${delEx.message}")
                        }
                        try {
                            secondaryAuth.signOut()
                        } catch (_: Exception) {}
                    } catch (e: Exception) {
                        if (e is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
                            e.message?.contains("already in use", ignoreCase = true) == true) {
                            // Akun memang sudah ada di Firebase Auth (aktif)
                            existsInAuth = true
                        } else {
                            Log.w(TAG, "Cek auth status untuk $userEmail error: ${e.message}")
                            if (e.message?.contains("badly formatted", ignoreCase = true) == true) {
                                existsInAuth = false
                            } else {
                                // Jaga data jika hanya kendala jaringan sementara
                                existsInAuth = true
                            }
                        }
                    }
                }

                // 3. Jika terkonfirmasi akun TIDAK ADA di Firebase Auth (karena telah dihapus lewat Auth Console):
                if (!existsInAuth) {
                    Log.d(TAG, "Akun $userEmail (${user.id}) terbukti TIDAK ADA di Firebase Auth. Menghapus otomatis dari Firestore dan Room DB...")
                    try {
                        remoteDataSource.deleteUser(user.id)
                        Log.d(TAG, "Berhasil menghapus dokumen Firestore 'users/${user.id}' ($userEmail)")
                    } catch (e: Exception) {
                        Log.w(TAG, "Gagal hapus Firestore user $userEmail: ${e.message}")
                    }

                    localDao.deletePermanently(user.id)

                    try {
                        FirebaseFirestore.getInstance()
                            .collection("deleted_accounts")
                            .document(userEmail.lowercase())
                            .delete()
                            .await()
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saat rekonsiliasi Firebase Auth: ${e.message}", e)
        } finally {
            isReconciling = false
        }
    }

    override fun getUserList(): Flow<List<User>> {
        // Jika online, pastikan realtime listener aktif dan perbarui cache Room dari Firestore
        if (networkMonitor.isConnected()) {
            startRealtimeListener()
            repositoryScope.launch {
                try {
                    val remoteList = remoteDataSource.getAllUsers()
                    processRemoteUsers(remoteList)
                } catch (e: Exception) {
                    Log.w(TAG, "Gagal mengambil data user dari remote background: ${e.message}")
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

    override suspend fun insertUser(user: User, plainPassword: String): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()
        Log.d(TAG, "insertUser dipanggil: fullName='${user.fullName}', isOnline=$isOnline")

        if (isOnline) {
            // Jika email ini sebelumnya pernah tercatat di deleted_accounts, hapus catatannya agar bisa login normal
            try {
                FirebaseFirestore.getInstance()
                    .collection("deleted_accounts")
                    .document(user.email.trim().lowercase())
                    .delete()
                    .await()
            } catch (_: Exception) {}

            // Buat Firebase Auth account menggunakan password dari input form (tidak disimpan ke DB)
            createAuthUserIfOnline(email = user.email, plainPassword = plainPassword)
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

    private suspend fun createAuthUserIfOnline(email: String, plainPassword: String) {
        if (email.isBlank() || plainPassword.isBlank()) return
        try {
            val secondaryAuth = getSecondaryAuth()
            try {
                secondaryAuth.createUserWithEmailAndPassword(email.trim(), plainPassword).await()
                Log.d(TAG, "Secondary Auth: Akun $email berhasil terdaftar di Firebase Auth")
                secondaryAuth.signOut()
            } catch (e: Exception) {
                Log.w(TAG, "Secondary Auth: Terjadi kesalahan / sudah terdaftar $email: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Secondary Auth exception: ${e.message}", e)
        }
    }

    override suspend fun updateUser(user: User, plainPassword: String?): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("deleted_accounts")
                    .document(user.email.trim().lowercase())
                    .delete()
                    .await()
            } catch (_: Exception) {}

            if (!plainPassword.isNullOrBlank()) {
                val currentAuth = FirebaseAuth.getInstance().currentUser
                if (currentAuth?.email?.trim()?.equals(user.email.trim(), ignoreCase = true) == true) {
                    try {
                        currentAuth.updatePassword(plainPassword).await()
                        Log.d(TAG, "Password akun aktif berhasil diupdate di Firebase Auth")
                    } catch (e: Exception) {
                        Log.w(TAG, "Gagal update password akun aktif di Firebase Auth: ${e.message}")
                    }
                } else {
                    createAuthUserIfOnline(email = user.email, plainPassword = plainPassword)
                }
            }
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

        val targetUser = try {
            localDao.getUserById(id)?.toDomain() ?: remoteDataSource.getUserById(id)?.toDomain()
        } catch (e: Exception) {
            null
        }
        val targetEmail = targetUser?.email?.trim()?.lowercase()

        if (isOnline) {
            try {
                Log.d(TAG, "Online: Hapus user dari Firestore collection 'users'...")
                remoteDataSource.deleteUser(id)
                Log.d(TAG, "Online: Firestore hapus sukses, hapus permanen dari Room DB")
                localDao.deletePermanently(id)

                // Jika user yang dihapus adalah user yang sedang aktif login di perangkat ini
                val currentAuth = FirebaseAuth.getInstance().currentUser
                if (targetEmail != null && currentAuth?.email?.trim()?.lowercase() == targetEmail) {
                    try {
                        currentAuth.delete().await()
                        Log.d(TAG, "Current auth user berhasil dihapus dari Firebase Auth")
                    } catch (e: Exception) {
                        Log.w(TAG, "Gagal hapus current auth user: ${e.message}")
                    }
                }

                // Catat ke Firestore collection 'deleted_accounts' agar saat login berikutnya akun otomatis dihapus dari Firebase Auth
                if (!targetEmail.isNullOrBlank()) {
                    try {
                        FirebaseFirestore.getInstance()
                            .collection("deleted_accounts")
                            .document(targetEmail)
                            .set(mapOf(
                                "email" to targetEmail,
                                "deletedAt" to Timestamp.now(),
                                "userId" to id
                            )).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Gagal catat ke deleted_accounts: ${e.message}")
                    }
                }

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

        // Setelah push data lokal, tarik data terbaru dari Firestore dan jalankan rekonsiliasi penuh
        try {
            val remoteList = remoteDataSource.getAllUsers()
            processRemoteUsers(remoteList, forceReconcile = true)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menarik data remote users saat sync: ${e.message}")
        }
    }
}
