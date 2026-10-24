package com.ptpws.ikikasir.feature.manajemenpengguna.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
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

            // Buat akun langsung di Firebase Authentication menggunakan password input
            createAuthUserIfOnline(email = user.email, plainPassword = plainPassword)
            try {
                Log.d(TAG, "Online: Mengirim data ke Firestore collection 'users' dengan ID '${user.id}' (tanpa password)...")
                remoteDataSource.saveUser(user.toDto())
                Log.d(TAG, "Online: Berhasil simpan ke Firestore! Menyimpan ke Room DB (isSynced = true, tanpa password)")
                localDao.insertOrUpdate(user.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e(TAG, "Gagal simpan ke Firestore (disimpan ke Room DB dengan isSynced = false). Error: ${e.message}", e)
                localDao.insertOrUpdate(user.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            Log.d(TAG, "Offline: Menyimpan ke Room DB (isSynced = false, tanpa password)")
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

    override suspend fun updateUser(
        user: User,
        plainPassword: String?,
        currentPassword: String?,
        oldEmail: String?
    ): Flow<Result<Unit>> = flow {
        val isOnline = networkMonitor.isConnected()

        if (isOnline) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("deleted_accounts")
                    .document(user.email.trim().lowercase())
                    .delete()
                    .await()
            } catch (_: Exception) {}

            val existingLocal = localDao.getUserById(user.id)
            val resolvedOldEmail = oldEmail?.trim()?.ifBlank { null }
                ?: existingLocal?.email?.trim()?.ifBlank { null }
                ?: user.email.trim()
            val isEmailChanged = !resolvedOldEmail.equals(user.email.trim(), ignoreCase = true)
            val isPasswordChanged = !plainPassword.isNullOrBlank()
            val oldPassword = currentPassword?.trim()?.takeIf { it.isNotBlank() }

            if (isPasswordChanged || isEmailChanged) {
                val newPass = plainPassword?.trim()?.takeIf { it.isNotBlank() }
                val currentAuth = FirebaseAuth.getInstance().currentUser
                val isCurrentLoggedInUser = currentAuth?.email?.trim()?.equals(resolvedOldEmail, ignoreCase = true) == true

                var authUpdated = false
                var authError: Exception? = null

                // 1. Jika akun yang diedit adalah akun yang sedang aktif login di perangkat ini
                if (isCurrentLoggedInUser && currentAuth != null) {
                    try {
                        val reauthPass = oldPassword ?: newPass
                        if (!reauthPass.isNullOrBlank()) {
                            try {
                                val credential = EmailAuthProvider.getCredential(resolvedOldEmail, reauthPass)
                                currentAuth.reauthenticate(credential).await()
                            } catch (reauthEx: Exception) {
                                Log.w(TAG, "Reauth current user gagal: ${reauthEx.message}")
                            }
                        }

                        if (isEmailChanged) {
                            currentAuth.updateEmail(user.email.trim()).await()
                            Log.d(TAG, "Email akun aktif berhasil diupdate dari $resolvedOldEmail ke ${user.email} di Firebase Auth")
                        }

                        if (newPass != null) {
                            currentAuth.updatePassword(newPass).await()
                            Log.d(TAG, "Password akun aktif ($resolvedOldEmail) berhasil diupdate di Firebase Auth")
                        }

                        authUpdated = true
                    } catch (e: Exception) {
                        Log.e(TAG, "Gagal update akun aktif di Firebase Auth: ${e.message}", e)
                        authError = e
                    }
                }

                // 2. Jika bukan akun aktif, atau update akun via secondary auth
                if (!authUpdated) {
                    val secondaryAuth = getSecondaryAuth()
                    val authPasswordToUse = oldPassword ?: newPass

                    try {
                        if (!authPasswordToUse.isNullOrBlank()) {
                            // Coba login ke akun lama via secondaryAuth untuk update email/password
                            try {
                                secondaryAuth.signInWithEmailAndPassword(resolvedOldEmail, authPasswordToUse).await()
                                val secUser = secondaryAuth.currentUser
                                if (secUser != null) {
                                    if (isEmailChanged) {
                                        secUser.updateEmail(user.email.trim()).await()
                                        Log.d(TAG, "Secondary Auth: Berhasil update email dari $resolvedOldEmail ke ${user.email}")
                                    }
                                    if (newPass != null) {
                                        secUser.updatePassword(newPass).await()
                                        Log.d(TAG, "Secondary Auth: Berhasil update password untuk ${user.email}")
                                    }
                                    authUpdated = true
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Secondary Auth: Gagal sign in dengan resolvedOldEmail ($resolvedOldEmail): ${e.message}")
                                authError = e
                            } finally {
                                secondaryAuth.signOut()
                            }
                        }

                        // Jika belum berhasil dan newPass/oldPassword tersedia, coba buat akun baru di Firebase Auth jika belum ada
                        if (!authUpdated && !authPasswordToUse.isNullOrBlank()) {
                            try {
                                val passForNew = newPass ?: authPasswordToUse
                                secondaryAuth.createUserWithEmailAndPassword(user.email.trim(), passForNew).await()
                                authUpdated = true
                                Log.d(TAG, "Secondary Auth: Akun ${user.email} berhasil didaftarkan baru di Firebase Auth")
                            } catch (collisionEx: Exception) {
                                if (collisionEx is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
                                    collisionEx.message?.contains("already in use", ignoreCase = true) == true) {
                                    // Akun baru sudah ada di Firebase Auth, coba login dan update jika ada password baru
                                    if (newPass != null && oldPassword != null) {
                                        try {
                                            secondaryAuth.signInWithEmailAndPassword(user.email.trim(), oldPassword).await()
                                            secondaryAuth.currentUser?.updatePassword(newPass)?.await()
                                            authUpdated = true
                                        } catch (e: Exception) {
                                            Log.w(TAG, "Secondary Auth: Gagal update password akun yang sudah ada: ${e.message}")
                                            authError = e
                                        } finally {
                                            secondaryAuth.signOut()
                                        }
                                    } else {
                                        authUpdated = true
                                    }
                                } else {
                                    authError = collisionEx
                                }
                            } finally {
                                secondaryAuth.signOut()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Secondary Auth error saat update user: ${e.message}", e)
                        authError = e
                    }
                }

                if (!authUpdated) {
                    val rawMsg = authError?.message.orEmpty().lowercase()
                    val errorMsg = when {
                        rawMsg.contains("blocked all requests") ||
                        rawMsg.contains("too-many-requests") ||
                        rawMsg.contains("unusual activity") ->
                            "Terlalu banyak percobaan gagal dari perangkat ini. Demi keamanan, silakan tunggu beberapa menit sebelum mencoba lagi, atau gunakan opsi 'Lupa kata sandi?' untuk mengirim link reset ke email."

                        authError is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
                        rawMsg.contains("wrong-password") ||
                        rawMsg.contains("invalid-credential") ||
                        rawMsg.contains("password is invalid") ->
                            "Kata sandi saat ini yang Anda masukkan salah. Silakan periksa kembali kata sandi saat ini, atau gunakan opsi 'Lupa kata sandi?' untuk mereset via email."

                        authError is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ||
                        rawMsg.contains("requires-recent-login") ||
                        rawMsg.contains("recent authentication") ->
                            "Demi keamanan, silakan masukkan kata sandi saat ini yang benar untuk mengonfirmasi perubahan data akun."

                        rawMsg.contains("weak-password") ||
                        rawMsg.contains("password should be at least") ->
                            "Kata sandi baru terlalu lemah. Harap gunakan minimal 6 karakter kombinasi huruf dan angka."

                        rawMsg.contains("network") ||
                        rawMsg.contains("timeout") ||
                        rawMsg.contains("unable to resolve host") ->
                            "Koneksi internet bermasalah. Periksa koneksi data atau Wi-Fi Anda lalu coba lagi."

                        authError is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
                        rawMsg.contains("already in use") ||
                        rawMsg.contains("email-already-in-use") ->
                            "Alamat email ini sudah digunakan oleh akun lain. Gunakan alamat email yang berbeda."

                        authError is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                        rawMsg.contains("user-not-found") ||
                        rawMsg.contains("no user record") ->
                            "Akun ini belum terdaftar di sistem login. Pastikan kata sandi saat ini atau kata sandi baru telah diisi dengan benar."

                        else ->
                            if (isEmailChanged) {
                                "Gagal memperbarui email akun login. Pastikan kata sandi saat ini sudah benar, atau isi kata sandi baru untuk akun ini."
                            } else {
                                "Gagal memperbarui kata sandi. Pastikan kata sandi saat ini sudah benar, atau gunakan tombol 'Lupa kata sandi?' untuk mengirim instruksi reset ke email pengguna."
                            }
                    }
                    emit(Result.failure(Exception(errorMsg)))
                    return@flow
                }

                // Jika email berhasil diubah, catat resolvedOldEmail di deleted_accounts agar sesi/login lama dibersihkan
                if (isEmailChanged && resolvedOldEmail.isNotBlank()) {
                    try {
                        FirebaseFirestore.getInstance()
                            .collection("deleted_accounts")
                            .document(resolvedOldEmail.lowercase())
                            .set(mapOf(
                                "email" to resolvedOldEmail.lowercase(),
                                "deletedAt" to Timestamp.now(),
                                "userId" to user.id,
                                "reason" to "email_changed_to_${user.email.trim().lowercase()}"
                            )).await()
                        Log.d(TAG, "Berhasil mencatat $resolvedOldEmail ke deleted_accounts karena email diubah ke ${user.email}")
                    } catch (e: Exception) {
                        Log.w(TAG, "Gagal mencatat oldEmail ke deleted_accounts: ${e.message}")
                    }
                }
            }

            // Simpan ke Firestore & Room DB (tanpa password)
            val updatedUserToSave = user

            try {
                Log.d(TAG, "Online: Update user di Firestore (tanpa password)...")
                remoteDataSource.updateUser(updatedUserToSave.toDto())
                Log.d(TAG, "Online: Firestore update sukses, update Room DB dengan isSynced = true")
                localDao.insertOrUpdate(updatedUserToSave.toEntity(isSynced = true))
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.w(TAG, "Gagal update Firestore online, fallback simpan lokal: ${e.message}")
                localDao.insertOrUpdate(updatedUserToSave.toEntity(isSynced = false))
                emit(Result.success(Unit))
            }
        } else {
            // OFFLINE
            if (!plainPassword.isNullOrBlank() || (!oldEmail.isNullOrBlank() && !oldEmail.equals(user.email.trim(), ignoreCase = true))) {
                emit(Result.failure(Exception("Mengubah email atau kata sandi memerlukan koneksi internet untuk memperbarui akun autentikasi.")))
                return@flow
            }
            val updatedUserToSave = user
            Log.d(TAG, "Offline: Update user di Room DB dengan isSynced = false")
            localDao.insertOrUpdate(updatedUserToSave.toEntity(isSynced = false))
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
