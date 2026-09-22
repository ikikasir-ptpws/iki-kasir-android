package com.ptpws.ikikasir.feature.auth.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.ptpws.ikikasir.feature.auth.domain.repository.AuthRepository
import com.ptpws.ikikasir.feature.manajemenpengguna.data.local.dao.UserDao
import com.ptpws.ikikasir.feature.manajemenpengguna.data.remote.datasource.UserRemoteDataSource
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
            // 1. Coba login langsung ke Firebase Auth
            val result = firebaseAuth.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()

            // Verifikasi status keaktifan akun pengguna
            val localUser = userDao.getUserByEmail(cleanEmail)
            val isActive = if (localUser != null) {
                localUser.isActive
            } else {
                try {
                    val remoteUser = remoteDataSource.getAllUsers().find { it.email.equals(cleanEmail, ignoreCase = true) }
                    remoteUser?.isActive ?: true
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
            // 2. Jika Firebase Auth gagal (misal user dibuat di Tambah Pengguna belum memiliki kredensial Firebase Auth), cek data lokal Room DB & Firestore
            try {
                var matchedUserPassword = ""
                var matchedUserIsActive = true
                var foundUser = false

                val localUser = userDao.getUserByEmail(cleanEmail)
                if (localUser != null) {
                    foundUser = true
                    matchedUserPassword = localUser.password
                    matchedUserIsActive = localUser.isActive
                } else {
                    val remoteUser = remoteDataSource.getAllUsers().find { it.email.equals(cleanEmail, ignoreCase = true) }
                    if (remoteUser != null) {
                        foundUser = true
                        matchedUserPassword = remoteUser.password
                        matchedUserIsActive = remoteUser.isActive
                    }
                }

                if (foundUser) {
                    if (!matchedUserIsActive) {
                        emit(Result.failure(Exception("Akun Anda telah dinonaktifkan oleh Admin. Akses ditolak.")))
                        return@flow
                    }

                    if (matchedUserPassword.isBlank() || matchedUserPassword == cleanPassword) {
                        // Daftarkan kredensial baru ke Firebase Auth secara otomatis agar selanjutnya dapat login langsung
                        try {
                            val newAuthResult = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, cleanPassword).await()
                            emit(Result.success(newAuthResult))
                            return@flow
                        } catch (createErr: Exception) {
                            // Jika sudah ada di Auth tapi password beda atau error lain, coba login ulang
                            try {
                                val retryAuthResult = firebaseAuth.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()
                                emit(Result.success(retryAuthResult))
                                return@flow
                            } catch (retryErr: Exception) {
                                emit(Result.failure(Exception(retryErr.message ?: "Kredensial login tidak valid.")))
                                return@flow
                            }
                        }
                    } else {
                        emit(Result.failure(Exception("Email atau kata sandi salah. Silakan periksa kembali.")))
                        return@flow
                    }
                }
            } catch (dbErr: Exception) {
                // Failover to original exception if DB check fails
            }

            emit(Result.failure(authException))
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
