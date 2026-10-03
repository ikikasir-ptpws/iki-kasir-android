package com.ptpws.ikikasir.feature.auth.domain.repository

import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isUserLoggedIn(): Boolean
    suspend fun signInWithEmailAndPassword(email: String, password: String): Flow<Result<AuthResult>>
    suspend fun sendPasswordResetEmail(email: String): Flow<Result<Unit>>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    fun signOut()
}
