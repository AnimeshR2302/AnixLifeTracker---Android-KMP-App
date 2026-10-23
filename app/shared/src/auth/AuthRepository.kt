package com.anix.android.anixlifetracker.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    fun observeAuthState(): StateFlow<AuthState>
    suspend fun getCurrentSession(): UserSession?
    suspend fun signUp(email: String, password: String, displayName: String? = null): AuthResult
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signOut(): AuthResult
    suspend fun sendPasswordResetEmail(email: String): AuthResult
    suspend fun restoreSession(): AuthResult
}
