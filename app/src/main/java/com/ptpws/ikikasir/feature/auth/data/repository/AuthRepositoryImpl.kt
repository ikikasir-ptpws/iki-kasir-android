package com.ptpws.ikikasir.feature.auth.data.repository

import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.ptpws.ikikasir.feature.auth.domain.repository.AuthRepository
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.datasource.UserRemoteDataSource
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto.toDto
import com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao,
    private val remoteDataSource: UserRemoteDataSource
) : AuthRepository {

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Flow<Result<AuthResult>> = flow {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        try {
            // 1. Login ke Firebase Auth terlebih dahulu
            val result = firebaseAuth.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()
            val currentAuthUser = firebaseAuth.currentUser

            // 2. Cek apakah akun ini terdaftar di Firestore collection 'deleted_accounts'
            val deletedDoc = try {
                FirebaseFirestore.getInstance()
                    .collection("deleted_accounts")
                    .document(cleanEmail.lowercase())
                    .get()
                    .await()
            } catch (e: Exception) {
                null
            }

            val isDeletedAccount = if (deletedDoc != null && deletedDoc.exists()) {
                val deletedAtMillis = deletedDoc.getTimestamp("deletedAt")?.toDate()?.time
                    ?: deletedDoc.getLong("deletedAt")
                    ?: 0L
                val creationMillis = currentAuthUser?.metadata?.creationTimestamp ?: 0L

                if (creationMillis > deletedAtMillis) {
                    // Akun ini dibuat baru di Firebase Auth SETELAH waktu penghapusan!
                    // Berarti ini akun baru yang dibuat admin di Firebase Console.
                    // Hapus data lama di deleted_accounts agar bersih
                    try {
                        deletedDoc.reference.delete().await()
                    } catch (_: Exception) {}
                    false
                } else {
                    // Akun ini dibuat SEBELUM waktu penghapusan (akun lama yang memang telah dihapus admin)
                    true
                }
            } else {
                false
            }

            if (isDeletedAccount) {
                // Akun ini adalah akun lama yang telah dihapus oleh Admin di aplikasi
                // Hapus akun dari Firebase Auth secara permanen agar hilang dari Firebase Auth Console
                try {
                    currentAuthUser?.delete()?.await()
                    Log.d("AuthRepository", "Akun terhapus $cleanEmail berhasil dihapus permanen dari Firebase Auth")
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Gagal menghapus user dari Firebase Auth saat login: ${e.message}")
                }

                try {
                    deletedDoc?.reference?.delete()?.await()
                } catch (_: Exception) {}

                firebaseAuth.signOut()
                emit(Result.failure(Exception("Akun Anda telah dihapus dari sistem. Akses ditolak.")))
                return@flow
            }

            // 3. Verifikasi keberadaan akun di database aplikasi (Firestore / Room DB)
            val localUser = userDao.getUserByEmail(cleanEmail)
            val remoteUser = try {
                remoteDataSource.getAllUsers().find { it.email.equals(cleanEmail, ignoreCase = true) }
            } catch (e: Exception) {
                null
            }

            // Simpan ke cache lokal jika data ada di remote tapi belum di lokal Room,
            // atau jika akun dibuat langsung lewat Firebase Auth Console (belum ada di Firestore/Room)
            val activeUser: User? = localUser?.toDomain() ?: remoteUser?.let {
                userDao.insertOrUpdate(it.toEntity())
                it.toDomain()
            } ?: run {
                // Akun baru dibuat langsung di Firebase Auth Console
                val currentAuthUser = firebaseAuth.currentUser
                val newUserId = currentAuthUser?.uid ?: java.util.UUID.randomUUID().toString()
                val newFullName = currentAuthUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: cleanEmail.substringBefore("@").ifBlank { "User" }
                val newPhotoUrl = currentAuthUser?.photoUrl?.toString().orEmpty()

                val newUser = User(
                    id = newUserId,
                    fullName = newFullName,
                    email = cleanEmail,
                    roleId = "Admin",
                    isActive = true,
                    photoUrl = newPhotoUrl,
                    createdAt = com.google.firebase.Timestamp.now(),
                    updatedAt = com.google.firebase.Timestamp.now(),
                    isSynced = true
                )
                try {
                    remoteDataSource.saveUser(newUser.toDto())
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Gagal auto-save user baru Firebase Auth ke Firestore: ${e.message}")
                }
                userDao.insertOrUpdate(newUser.toEntity(isSynced = true))
                newUser
            }

            if (activeUser != null && !activeUser.isActive) {
                firebaseAuth.signOut()
                emit(Result.failure(Exception("Akun Anda telah dinonaktifkan oleh Admin. Akses ditolak.")))
                return@flow
            }

            emit(Result.success(result))
        } catch (authException: Exception) {
            // Firebase Auth gagal
            val msg = when {
                authException.message?.contains("no user record", ignoreCase = true) == true ||
                authException.message?.contains("user-not-found", ignoreCase = true) == true -> {
                    // Jika akun dihapus langsung di Firebase Auth Console, bersihkan juga dari database lokal dan Firestore
                    try {
                        val local = userDao.getUserByEmail(cleanEmail)
                        if (local != null) {
                            userDao.deletePermanently(local.id)
                            remoteDataSource.deleteUser(local.id)
                        } else {
                            val remote = remoteDataSource.getAllUsers().find { it.email.equals(cleanEmail, ignoreCase = true) }
                            if (remote != null) {
                                remoteDataSource.deleteUser(remote.id)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("AuthRepository", "Gagal membersihkan data user yang dihapus di Auth Console: ${e.message}")
                    }
                    "Akun tidak terdaftar atau telah dihapus dari sistem. Akses ditolak."
                }
                authException.message?.contains("password is invalid", ignoreCase = true) == true ||
                authException.message?.contains("wrong-password", ignoreCase = true) == true ||
                authException.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ->
                    "Email atau kata sandi salah. Silakan periksa kembali."
                authException.message?.contains("network", ignoreCase = true) == true ||
                authException.message?.contains("timeout", ignoreCase = true) == true ->
                    "Koneksi internet bermasalah. Coba lagi."
                authException.message?.contains("too many requests", ignoreCase = true) == true ->
                    "Terlalu banyak percobaan login. Silakan tunggu beberapa saat."
                else -> authException.message ?: "Login gagal. Coba lagi."
            }
            emit(Result.failure(Exception(msg)))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = flow {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            emit(Result.failure(Exception("Email tidak boleh kosong.")))
            return@flow
        }

        // 1. Verifikasi keberadaan email di database lokal (Room DB)
        val localUser = userDao.getUserByEmail(cleanEmail)
        var userExistsInDb = localUser != null

        // 2. Jika tidak ditemukan lokal, cek di remote (Firestore)
        if (!userExistsInDb) {
            try {
                val remoteUsers = remoteDataSource.getAllUsers()
                val foundRemote = remoteUsers.find { it.email.equals(cleanEmail, ignoreCase = true) }
                if (foundRemote != null) {
                    userDao.insertOrUpdate(foundRemote.toEntity())
                    userExistsInDb = true
                }
            } catch (e: Exception) {
                // Ignore remote fetch error if checking
            }
        }

        if (!userExistsInDb) {
            emit(Result.failure(Exception("Email '$cleanEmail' TIDAK TERDAFTAR di aplikasi IKI Kasir!\n\nPastikan Anda memasukkan email yang terdaftar untuk akun IKI Kasir Anda (bukan email Google lain). Jika ini akun baru, minta System Administrator untuk mendaftarkannya terlebih dahulu.")))
            return@flow
        }

        // 3. Kirim email reset password via Firebase Auth
        try {
            firebaseAuth.sendPasswordResetEmail(cleanEmail).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("no user record", ignoreCase = true) == true ||
                e.message?.contains("user-not-found", ignoreCase = true) == true ->
                    "Akun '$cleanEmail' terdaftar di aplikasi tetapi belum dibuatkan kredensial Firebase Auth. Minta Admin untuk mereset kata sandi Anda."
                e.message?.contains("invalid-email", ignoreCase = true) == true ->
                    "Format email tidak valid."
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Koneksi internet bermasalah. Coba lagi."
                else -> e.message ?: "Gagal mengirimkan instruksi lupa kata sandi."
            }
            emit(Result.failure(Exception(msg)))
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: error("Sesi akun berakhir. Silakan masuk kembali.")
        val email = user.email
            ?: error("Akun ini tidak menggunakan email dan kata sandi. Gunakan metode reset akun.")
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
        Unit
    }.recoverCatching { error ->
        val message = when {
            error.message?.contains("wrong-password", ignoreCase = true) == true ||
                error.message?.contains("invalid-credential", ignoreCase = true) == true ->
                "Kata sandi saat ini tidak sesuai."
            error is FirebaseAuthRecentLoginRequiredException ||
                error.message?.contains("requires-recent-login", ignoreCase = true) == true ||
                error.message?.contains("requires recent authentication", ignoreCase = true) == true ->
                "Sesi keamanan kedaluwarsa. Silakan keluar lalu masuk kembali."
            error.message?.contains("network", ignoreCase = true) == true ->
                "Koneksi internet bermasalah. Periksa koneksi lalu coba lagi."
            error.message?.contains("weak-password", ignoreCase = true) == true ->
                "Kata sandi baru terlalu lemah. Gunakan minimal 6 karakter."
            else -> error.message ?: "Gagal mengubah kata sandi."
        }
        throw IllegalStateException(message, error)
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
