package com.ptpws.ikikasir.feature.auth.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.feature.auth.domain.repository.AuthRepository
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.entity.toEntity
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.datasource.UserRemoteDataSource
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.dto.toDto
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
            // Login ke Firebase Auth
            val result = firebaseAuth.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()

            // Verifikasi status keaktifan akun
            val localUser = userDao.getUserByEmail(cleanEmail)
            val isActive = if (localUser != null) {
                localUser.isActive
            } else {
                try {
                    val remoteUser = remoteDataSource.getAllUsers()
                        .find { it.email.equals(cleanEmail, ignoreCase = true) }
                    if (remoteUser != null) {
                        userDao.insertOrUpdate(remoteUser.toEntity())
                        remoteUser.isActive
                    } else {
                        // User terdaftar langsung di Firebase Auth (bukan via app)
                        // Auto-simpan profilnya ke Firestore & Room DB
                        val currentAuthUser = firebaseAuth.currentUser
                        val newUserId = currentAuthUser?.uid ?: java.util.UUID.randomUUID().toString()
                        val newFullName = currentAuthUser?.displayName?.takeIf { it.isNotBlank() }
                            ?: cleanEmail.substringBefore("@").ifBlank { "User" }
                        val newPhotoUrl = currentAuthUser?.photoUrl?.toString().orEmpty()

                        val newUser = com.ptpws.ikikasir.feature.manajemenpengguna.domain.model.User(
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
                            android.util.Log.w("AuthRepository", "Gagal auto-save ke Firestore: ${e.message}")
                        }
                        userDao.insertOrUpdate(newUser.toEntity(isSynced = true))
                        true
                    }
                } catch (e: Exception) {
                    true
                }
            }

            if (!isActive) {
                firebaseAuth.signOut()
                emit(Result.failure(Exception("Akun Anda telah dinonaktifkan oleh Admin. Akses ditolak.")))
            } else {
                emit(Result.success(result))
            }
        } catch (authException: Exception) {
            // Firebase Auth gagal — sampaikan error langsung (tidak ada fallback password tersimpan)
            val msg = when {
                authException.message?.contains("no user record", ignoreCase = true) == true ||
                authException.message?.contains("user-not-found", ignoreCase = true) == true ->
                    "Email tidak terdaftar. Periksa kembali atau hubungi Admin."
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

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
